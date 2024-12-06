package cn.yanhu.imchat.manager

import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.imchat.api.imChatRxApi
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.GsonUtils
import com.hyphenate.EMCallBack
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMUserInfo


/**
 * @author: zhengjun
 * created: 2024/2/2
 * desc:
 */
object ImUserManager {


    fun getSelfUserInfo():UserDetailInfo{
        return  GsonUtils.fromJson(AppCacheManager.userInfo ,UserDetailInfo::class.java)
    }

    fun updateUserInfo(key: EMUserInfo.EMUserInfoType, value: String) {
        EMClient.getInstance().userInfoManager().updateOwnInfoByAttribute(
            key,
            value,
            object : EMValueCallBack<String?> {
                override fun onSuccess(value: String?) {}
                override fun onError(error: Int, errorMsg: String) {}
            })
    }

    fun updateUserInfo(userInfo: EMUserInfo) {
        EMClient.getInstance().userInfoManager()
            .updateOwnInfo(userInfo, object : EMValueCallBack<String?> {
                override fun onSuccess(value: String?) {}
                override fun onError(error: Int, errorMsg: String) {}
            })
    }


    fun setUserBlack(isChecked: Boolean, userId: String) {
        if (isChecked) {
            request({ imChatRxApi.blockUser(userId)},object : OnRequestResultListener<String>{
                override fun onSuccess(data: BaseBean<String>) {
                    showToast("用户已被拉黑")
                }
            })
            EMClient.getInstance().contactManager().asyncAddUserToBlackList(userId,true,object : EMCallBack{
                override fun onSuccess() {
                }
                override fun onError(p0: Int, p1: String?) {
                }
            })
        } else {
            request({ imChatRxApi.cancelBlock(userId)},object : OnRequestResultListener<String>{
                override fun onSuccess(data: BaseBean<String>) {
                }
            })
            EMClient.getInstance().contactManager().asyncRemoveUserFromBlackList(userId,object : EMCallBack{
                override fun onSuccess() {
                }
                override fun onError(p0: Int, p1: String?) {
                }
            })
        }
    }


    /**
     * 判断用户会话是否置顶
     * @param accId 用户帐号
     * @return true已经置顶；false未置顶
     */
    fun isStickTop(conversationId: String): Boolean {
        val conversation = EMClient.getInstance().chatManager().getConversation(conversationId)
        return conversation.isPinned
    }


    /**
     * 设置消息置顶/取消置顶
     */
    fun setStickTop(isChecked: Boolean, conversationId: String) {
        EMClient.getInstance().chatManager()
            .asyncPinConversation(conversationId, isChecked, object : EMCallBack {
                override fun onSuccess() {}
                override fun onError(code: Int, error: String) {}
            })
    }
}