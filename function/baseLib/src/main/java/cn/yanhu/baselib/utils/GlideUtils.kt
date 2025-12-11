package cn.yanhu.baselib.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.ext.logComToFile
import cn.yanhu.baselib.utils.ext.logcom
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import jp.wasabeef.glide.transformations.BlurTransformation

/**
 * @author: zhengjun
 * created: 2024/1/11
 * desc:
 */
object GlideUtils {
    fun loadAsDrawable(context: Context, imgUrl: Any, listener: CustomTarget<Drawable>) {
        try {
            if (isDestroy(context)) return
            Glide.with(context).asDrawable().load(imgUrl).dontAnimate()
                .into(listener)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun loadAsBitmap(context: Context, imgUrl: Any, listener: CustomTarget<Bitmap>) {
        try {
            if (isDestroy(context)) return
            Glide.with(context).asBitmap().load(imgUrl).dontAnimate()
                .into(listener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun loadImage(
        context: Context,
        url: Any?,
        imageView: ImageView?,
    ) {
        if (isDestroy(context)) return
        load(context, url, imageView)
    }


    @SuppressLint("CheckResult")
    fun load(
        context: Context,
        url: Any?,
        imageView: ImageView?,
        @DrawableRes placeholderId: Int? = R.drawable.icon_portrait,
        @DrawableRes errorId: Int? = null,
        transformations: MultiTransformation<Bitmap>? = null,
    ) {
        try {
            if (url == null || (url is String && TextUtils.isEmpty(url.toString()))) {
                return
            }
            if (imageView == null){
                logComToFile("glide", "加载失败：url=${url},imageView==null")
                return
            }
            if (isDestroy(context)){
                logComToFile("glide", "加载失败：url=${url},isDestroy")
                return
            }
            val requestOptions = RequestOptions().diskCacheStrategy(
                DiskCacheStrategy.ALL
            ).skipMemoryCache(false).format(DecodeFormat.PREFER_RGB_565)
            requestOptions.centerCrop()
            if (transformations != null) requestOptions.transform(transformations)
            if (placeholderId != null && placeholderId != -1) {
                requestOptions.placeholder(placeholderId)
                requestOptions.error(placeholderId)
            }
            if (errorId != null) requestOptions.error(errorId)
            Glide.with(context).load(url).apply(requestOptions)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        logComToFile("glide", "加载失败：url=${url},error=${e?.message}")
                       // GlideHealthMonitor.onLoadFailed(e)
                        return false // 继续交给 Glide 默认逻辑
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(imageView)
        } catch (e: Exception) {
            logComToFile("glide", "加载失败：url=${url},error=${e.message}")
            e.printStackTrace()
        }

    }

    private fun isDestroy(context: Context): Boolean {
        return context is Activity && context.isDestroyed
    }

    fun loadBlurTransPic(
        context: Context, imgUrl: Any, maskRadius: Int = 20,
        maskSampling: Int = 4, imageView: ImageView
    ) {
        try {
            if (isDestroy(context)) return
            val requestOptions =
                RequestOptions.bitmapTransform(BlurTransformation(maskRadius, maskSampling))
            Glide.with(context).load(imgUrl).apply(requestOptions)
                .into(imageView)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}