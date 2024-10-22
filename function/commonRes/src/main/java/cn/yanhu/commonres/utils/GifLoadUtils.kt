package cn.yanhu.commonres.utils

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ImageView
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.commonres.R
import cn.yanhu.commonres.manager.ImageThumbUtils
import com.blankj.utilcode.util.ThreadUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.io.IOException
import java.net.URL
import java.nio.ByteBuffer
import java.nio.channels.Channels

/**
 * @author: zhengjun
 * created: 2023/8/3
 * desc:
 */
object GifLoadUtils {

    fun loadGif(url: String, giftImageView: GifImageView,dimenId:Int = com.zj.dimens.R.dimen.dp_4) {
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<ByteBuffer>() {
            override fun onSuccess(result: ByteBuffer?) {
                if (result != null) {
                    val gifFromBytes = GifDrawable(result)
                    if (dimenId!=0){
                        gifFromBytes.cornerRadius = CommonUtils.getDimension(dimenId).toFloat()
                    }
                    giftImageView.setImageDrawable(gifFromBytes)
                }
            }

            override fun doInBackground(): ByteBuffer? {
                return downloadGif(url)
            }
        })
    }



    private fun downloadGif(url: String): ByteBuffer? {
        try {
            val urlConnection = URL(url).openConnection()
            urlConnection.connect()
            val contentLength = urlConnection.contentLength
            if (contentLength < 0) {
                throw IOException("Content-Length header not present")
            }
            urlConnection.getInputStream().use {
                val buffer = ByteBuffer.allocateDirect(contentLength)
                Channels.newChannel(it).use { channel ->
                    while (buffer.remaining() > 0) {
                        channel.read(buffer)
                    }
                    return buffer
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            return null
        }

    }


    @SuppressLint("CheckResult")
    fun loadHasGifPic(url: String, imageView: GifImageView, dimenId:Int = com.zj.dimens.R.dimen.dp_4,context:Context){
        if (url.endsWith(".gif")) {
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            imageView.setImageResource(R.drawable.pic_default_bg)
            loadGif(url, imageView,dimenId)
        } else {
            val requestOptions = RequestOptions().diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).skipMemoryCache(false)
            requestOptions.transform(
                CenterCrop(),
                RoundedCorners(
                    CommonUtils.getDimension(dimenId)
                )
            )
            requestOptions.placeholder(R.drawable.pic_default_bg)
            requestOptions.error(R.drawable.pic_default_bg)
            Glide.with(context).load(ImageThumbUtils.getThumbUrl(url)).apply(requestOptions).into(imageView)
        }
    }
}