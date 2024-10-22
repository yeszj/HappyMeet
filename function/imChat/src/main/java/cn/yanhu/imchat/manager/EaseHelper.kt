package cn.yanhu.imchat.manager

import android.text.TextUtils
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.db.ChatUserInfoManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.GsonUtils
import com.hyphenate.chat.EMClient
import com.hyphenate.easeui.EaseIM
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.provider.EaseUserProfileProvider

object EaseHelper {

    @JvmStatic
    fun initEaseUI() {
        EaseIM.getInstance().userProvider = object : EaseUserProfileProvider {
            override fun getUser(username: String): EaseUser? {
                if (username == AppCacheManager.userId) {
                    val userInfo = ImUserManager.getSelfUserInfo()
                    if (userInfo != null) {
                        val user = EaseUser(username)
                        user.nickname = userInfo.nickName;
                        //设置头像地址
                        user.avatar = userInfo.portrait;
                        return user
                    }
                }
                val userInfo = ChatUserInfoManager.getUserInfo(username)
                if (userInfo == null) {
                    val user = getUserInfoFromMessage(username)
                    if (user != null) return user
                    getUserInfo(username)
                    return null
                }
                //最后返回构建的 EaseUser 对象
                val user = EaseUser(username)
                //设置用户昵称
                user.nickname = userInfo.nickName

                //设置头像地址
                user.avatar = userInfo.portrait
                //最后返回构建的 EaseUser 对象
                return user
            }

            override fun getGroupUser(groupId: String, userId: String): EaseUser {
                return super.getGroupUser(groupId, userId)
            }
        }
    }

    fun getUserInfo(username: String) {
            request({ imChatRxApi.getUserInfoByUserId(username)},object : OnRequestResultListener<UserDetailInfo>{
                override fun onSuccess(data: BaseBean<UserDetailInfo>) {
                    ChatUserInfoManager.saveUserInfo(data.data)
                }
            },false)
    }

    private fun getUserInfoFromMessage(username: String): EaseUser? {
        val conversation = EMClient.getInstance().chatManager()
            .getConversation(username)
        if (conversation?.latestMessageFromOthers != null) {
            val latestMessageFromOthers = conversation.latestMessageFromOthers
            val stringAttribute =
                latestMessageFromOthers.getStringAttribute(ChatConstant.CUSTOM_SEND_USER_INFO, "")
            if (!TextUtils.isEmpty(stringAttribute)) {
                val dataBean = GsonUtils.fromJson(stringAttribute, UserDetailInfo::class.java)
                val user =  EaseUser(username)
                //设置用户昵称
                user.nickname = dataBean.nickName
                //设置头像地址
                user.avatar = dataBean.portrait
                return user
            }
        }
        return null
    }
}