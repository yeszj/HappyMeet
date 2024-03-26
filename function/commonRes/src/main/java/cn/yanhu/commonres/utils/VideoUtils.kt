package cn.yanhu.commonres.utils

import android.media.MediaMetadataRetriever
import android.text.TextUtils
import java.io.FileInputStream

/**
 * @author: zhengjun
 * created: 2023/4/12
 * desc:
 */
object VideoUtils {

    @JvmStatic
    fun isVideo(path:String?):Boolean{
        if (TextUtils.isEmpty(path)){
            return false
        }
        return path?.endsWith(".mp4") == true
    }

    fun getVideoWidthOrHeight(path: String, isHeight: Boolean): Int {
        var retriever: MediaMetadataRetriever? = null
        var inputStream: FileInputStream? = null
        var mWidthHeight = 0
        try {
            retriever = MediaMetadataRetriever();
            inputStream = FileInputStream(path)
            retriever.setDataSource(inputStream.fd);
            val bmp = retriever.frameAtTime
            bmp?.apply {
                mWidthHeight = if (!isHeight) {
                    bmp.width
                } else {
                    bmp.height
                }
            }

        } catch (e:Exception ) {
            e.printStackTrace()
        } finally {
            retriever?.release()
            inputStream?.close()
        }
        return mWidthHeight
    }
}