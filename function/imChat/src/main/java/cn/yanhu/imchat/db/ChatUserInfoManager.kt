package cn.yanhu.imchat.db

import cn.yanhu.commonres.bean.UserDetailInfo
import org.litepal.LitePal

/**
 * @author: zhengjun
 * created: 2023/11/21
 * desc:
 */
object ChatUserInfoManager {


    @JvmStatic
    fun getChatUserList(): List<UserDetailInfo>? {
        return try {
            LitePal.order("updateTime DESC").find(
                UserDetailInfo::class.java
            )
        } catch (e: Exception) {
            null
        }
    }

    fun updateIsFriend(userId: String, isFiend: Boolean) {
        val dataBean =
            getUserInfo(userId)
        dataBean?.apply {
            this.isFriend = isFiend
            if (isFiend) {
                saveUserInfo(this)
            } else {
                this.setToDefault("isFriend")
                saveUserInfo(this)
            }
        }
    }

    @JvmStatic
    fun getUserInfo(userId: String): UserDetailInfo? {
        return try {
            val list = LitePal.where("userId = ?", userId).limit(1).find(
                UserDetailInfo::class.java
            )
            if (list != null && list.size > 0) {
                return list[0]
            } else {
                return null
            }
        } catch (e: Exception) {
            null
        }
    }

    @JvmStatic
    fun saveUserInfo(userInfo: UserDetailInfo?) {
        if (userInfo == null) {
            return
        }
        val currentCacheInfo = getUserInfo(userInfo.userId)
        if (currentCacheInfo == null) {
            userInfo.save()
        } else {
            if (!userInfo.isFriend) {
                userInfo.setToDefault("isFriend")
            }
            if (!userInfo.isAuth) {
                userInfo.setToDefault("isAuth")
            }
            userInfo.update(currentCacheInfo.id)
        }
    }
}