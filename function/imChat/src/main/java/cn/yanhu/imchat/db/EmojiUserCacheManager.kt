package cn.yanhu.imchat.db

import android.content.ContentValues
import com.hyphenate.easeui.domain.EaseEmojicon
import org.litepal.LitePal

/**
 * @author: zhengjun
 * created: 2023/11/21
 * desc:
 */
object EmojiUserCacheManager {
    @JvmStatic
    fun getRecentEmojiList(): List<EaseEmojicon>? {
        return try {
            LitePal.limit(7).order("cacheTime DESC").find(
                EaseEmojicon::class.java
            )
        } catch (e: Exception) {
            null
        }
    }

    @JvmStatic
    fun getRecentEmoji(iconPath: String): EaseEmojicon? {
        val find = LitePal.limit(1).where(
            "iconPath=?",
            iconPath
        ).find(
            EaseEmojicon::class.java
        )
        if (find.size > 0) {
            return find[0]
        }
        return null
    }

    @JvmStatic
    fun saveEmojiInfo(easeEmojicon: EaseEmojicon) {
        easeEmojicon.cacheTime = System.currentTimeMillis()
        val currentCacheInfo = getRecentEmoji(easeEmojicon.iconPath)
        if (currentCacheInfo == null) {
            easeEmojicon.save()
        } else {
            val values = ContentValues()
            values.put("cacheTime", easeEmojicon.cacheTime)
            LitePal.update(EaseEmojicon::class.java, values, currentCacheInfo.id)
        }
    }
}