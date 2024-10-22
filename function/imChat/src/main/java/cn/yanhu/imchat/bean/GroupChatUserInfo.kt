package cn.yanhu.imchat.bean

import android.text.TextUtils
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.manager.AppCacheManager
import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2024/3/25
 * desc:
 */
class GroupChatUserInfo : BaseUserInfo(),Serializable {
    var nobleAvatarFrame: String? = ""
    var reportUrl: String? = ""
    var viewUserGroupChatIdentity: Int = 50
    var nowUserGender: Int = 1
    var nowUserGroupChatIdentity: Int = 1
    fun isShowSkip(): Boolean {
        if (TextUtils.isEmpty(reportUrl)){
            return false
        }
        if (viewUserGroupChatIdentity == GroupBean.ROLE_ADMIN || viewUserGroupChatIdentity == GroupBean.ROLE_APP_ADMIN || viewUserGroupChatIdentity == GroupBean.ROLE_OWNER) {
            if (userId.toString() == AppCacheManager.userId) {
                return false
            }
            if (nowUserGroupChatIdentity == GroupBean.ROLE_OWNER) {
                return true
            }
            return false
        } else {
            if (nowUserGroupChatIdentity == GroupBean.ROLE_ADMIN || nowUserGroupChatIdentity == GroupBean.ROLE_APP_ADMIN || nowUserGroupChatIdentity == GroupBean.ROLE_OWNER) {
                return true
            }
            return false
        }

    }
}