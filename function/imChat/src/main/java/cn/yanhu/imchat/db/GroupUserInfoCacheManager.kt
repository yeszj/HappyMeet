package cn.yanhu.imchat.db

import android.content.ContentValues
import cn.yanhu.imchat.bean.GroupUserInfo
import org.litepal.LitePal

/**
 * @author: zhengjun
 * created: 2023/11/21
 */
object GroupUserInfoCacheManager {
    @JvmStatic
    fun getGroupUserInfoByUserId(userId:String): GroupUserInfo? {
        try {
            val remarkCacheInfo =
                LitePal.where(
                    "userId=?",
                    userId
                ).limit(1).find(
                    GroupUserInfo::class.java
                )
            if (remarkCacheInfo.size > 0) {
                return remarkCacheInfo[0]
            }
        }catch (e:Exception){
            return null
        }
        return null
    }

    @JvmStatic
    fun saveUserInfo(groupUserInfo: GroupUserInfo){
        val currentCacheInfo = getGroupUserInfoByUserId(groupUserInfo.userId.toString())
        if (currentCacheInfo==null){
            groupUserInfo.save()
        }else{
            val values = ContentValues()
            values.put("hometown",groupUserInfo.hometown)
            values.put("province",groupUserInfo.province)
            values.put("portrait",groupUserInfo.portrait)
            values.put("age",groupUserInfo.age)
            //values.put("nobleLevel",groupUserInfo.nobleLevel)
            values.put("nickName",groupUserInfo.nickName)
            values.put("nobleAvatarFrame",groupUserInfo.avatarFrame)
            LitePal.update(GroupUserInfo::class.java, values, currentCacheInfo.id)
        }
    }
}