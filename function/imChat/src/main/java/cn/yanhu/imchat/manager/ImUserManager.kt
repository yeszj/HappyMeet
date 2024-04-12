package cn.yanhu.imchat.manager

import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.manager.AppCacheManager
import com.blankj.utilcode.util.GsonUtils
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.friend.FriendService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.StickTopSessionInfo
import com.netease.nimlib.sdk.uinfo.UserService
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo
import com.netease.yunxin.kit.chatkit.ui.common.ChatCallback
import com.netease.yunxin.kit.corekit.im.model.UserField
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback
import com.netease.yunxin.kit.corekit.im.repo.CommonRepo


/**
 * @author: zhengjun
 * created: 2024/2/2
 * desc:
 */
object ImUserManager {


    fun getSelfUserInfo():UserDetailInfo{
        val userInfo = getUserInfo(AppCacheManager.userId)
        return  GsonUtils.fromJson(userInfo.extension,UserDetailInfo::class.java)
    }

    fun getUserInfo(account:String):NimUserInfo{
        return NIMClient.getService(
            UserService::class.java
        ).getUserInfo(account)
    }

    fun updateUserInfo(field: UserField, value: Any) {
        val map: MutableMap<UserField, Any> = HashMap(1)
        map[field] = value
        CommonRepo.updateUserInfo(
            map,
            object : FetchCallback<Void> {
                override fun onSuccess(param: Void?) {
                }

                override fun onFailed(code: Int) {
                }

                override fun onException(exception: Throwable?) {
                }
            })
    }

    fun updateUserInfo(map: MutableMap<UserField, Any>) {
        CommonRepo.updateUserInfo(
            map,
            object : FetchCallback<Void> {
                override fun onSuccess(param: Void?) {
                }

                override fun onFailed(code: Int) {
                }

                override fun onException(exception: Throwable?) {
                }
            })
    }

    /**
     * 判断用户是否已被拉黑
     *
     * @param account 用戶帐号
     * @return 该用户是否在黑名单列表中
     */
    fun isInBlack(account: String): Boolean {
        return NIMClient.getService(
            FriendService::class.java
        ).isInBlackList(account)
    }

    fun setUserBlack(isChecked: Boolean, userId: String) {
        if (isChecked) {
            NIMClient.getService(FriendService::class.java).addToBlackList(userId)
                .setCallback(object : RequestCallback<Void> {
                    override fun onSuccess(result: Void?) {
                    }

                    override fun onFailed(code: Int) {
                        showToast(code.toString())
                    }

                    override fun onException(exception: Throwable?) {
                        showToast(exception?.message)
                    }
                })
        } else {
            NIMClient.getService(FriendService::class.java).removeFromBlackList(userId)
                .setCallback(object : RequestCallback<Void> {
                    override fun onSuccess(result: Void?) {
                    }
                    override fun onFailed(code: Int) {
                        showToast(code.toString())
                    }
                    override fun onException(exception: Throwable?) {
                        showToast(exception?.message)
                    }
                })
        }
    }


    /**
     * 判断用户是否需要消息提醒/免打扰
     * @param account 用户帐号
     * @return true表示需要提醒；false表示免打扰
     */
    fun isNeedMessageNotify(account: String): Boolean {
        return NIMClient.getService(FriendService::class.java).isNeedMessageNotify(account)
    }

    /**
     * 设置是否免打扰
     */
    fun setNotify(isChecked: Boolean, sessionId: String,sessionType:SessionTypeEnum = SessionTypeEnum.P2P) {
        ConversationRepo.setNotify(
            sessionId,
            sessionType,
            !isChecked,
            object : ChatCallback<Void>() {
                override fun onSuccess(param: Void?) {
                }
            })
    }


    /**
     * 判断用户会话是否置顶
     * @param accId 用户帐号
     * @return true已经置顶；false未置顶
     */
    fun isStickTop(accId: String,sessionType:SessionTypeEnum = SessionTypeEnum.P2P): Boolean {
        return ConversationRepo.isStickTop(accId, sessionType)
    }


    /**
     * 设置消息置顶/取消置顶
     */
    fun setStickTop(isChecked: Boolean, userId: String,sessionType:SessionTypeEnum = SessionTypeEnum.P2P) {
        if (isChecked) {
            ConversationRepo.addStickTop(
                userId,
                sessionType,
                "",
                object : ChatCallback<StickTopSessionInfo>() {
                    override fun onSuccess(param: StickTopSessionInfo?) {
                        ConversationRepo.notifyStickTop(
                            userId,
                            sessionType
                        )
                    }
                })
        } else {
            ConversationRepo.removeStickTop(
                userId,
                sessionType,
                "",
                object : ChatCallback<Void>() {
                    override fun onSuccess(param: Void?) {
                        ConversationRepo.notifyStickTop(
                            userId,
                            sessionType
                        )
                    }
                })
        }
    }
}