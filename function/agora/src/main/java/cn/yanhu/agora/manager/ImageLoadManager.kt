package cn.gxgre.forlove.manager

import android.graphics.*
import android.widget.ImageView
import com.blankj.utilcode.util.ThreadUtils
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


/**
 * @author: zhengjun
 * created: 2023/4/14
 * desc:
 */
object ImageLoadManager {

    fun loadImage(iv:ImageView,url:String){
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<Bitmap>() {
            override fun onSuccess(result: Bitmap?) {
                if (result != null) {
                    iv.setImageBitmap(result)
                }
            }
            override fun doInBackground(): Bitmap? {
                return getImageBitmap(url)
            }
        })
    }

    @JvmStatic
    fun getImageBitmap(url: String): Bitmap? {
        val imgUrl: URL?
        var bitmap: Bitmap? = null
        try {
            imgUrl = URL(url)
            val conn: HttpURLConnection = imgUrl
                .openConnection() as HttpURLConnection
            conn.doInput = true
            conn.connect()
            val `is`: InputStream = conn.inputStream
            bitmap = BitmapFactory.decodeStream(`is`)
            `is`.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    fun createCircleImage(source: Bitmap): Bitmap{
        val length = if (source.width < source.height) source.width else source.height
        val paint = Paint()
        paint.isAntiAlias = true
        val target = Bitmap.createBitmap(length, length, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(target)
        canvas.drawCircle((length / 2).toFloat(), (length / 2).toFloat(),
            (length / 2).toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return target
    }
}