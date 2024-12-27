package cn.yanhu.commonres.manager

import android.text.TextUtils

/**
 * @author: zhengjun
 * created: 2024/1/18
 * desc:
 */
object ImageThumbUtils {

    /**
     * 根据原图地址 获取缩略图地址
     */
    @JvmStatic
    fun getThumbUrl(url: String?): String {
        if(TextUtils.isEmpty(url)){
            return ""
        }
        if (url!!.contains("_thumb")){
            return url
        }
        return if (url.endsWith(".mp4")){
            getThumbUrlByVideo(url)
        }else{
            val lastIndexOf = url.lastIndexOf(".")
            if (lastIndexOf != -1) {
                val dataAfterLastSpace = url.substring(lastIndexOf + 1)
                url.replace(".$dataAfterLastSpace", "_thumb.$dataAfterLastSpace")
            } else {
                url
            }
        }

    }

    private fun getThumbUrlByVideo(url: String): String {
        if (url.contains("_thumb")){
            return url
        }
        val lastIndexOf = url.lastIndexOf(".")
        return if (lastIndexOf != -1) {
            val dataAfterLastSpace = url.substring(lastIndexOf + 1)
            url.replace(".$dataAfterLastSpace", "_thumb.jpeg")
        } else {
            url
        }
    }
}