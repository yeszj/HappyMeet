package cn.yanhu.baselib.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import cn.yanhu.baselib.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
        Glide.with(context).asDrawable().load(imgUrl).dontAnimate()
            .into(listener)
    }

    fun loadAsBitmap(context: Context, imgUrl: Any, listener: CustomTarget<Bitmap>) {
        Glide.with(context).asBitmap().load(imgUrl).dontAnimate()
            .into(listener)
    }

    @JvmStatic
    fun loadImage(context: Context,
             url: Any?,
             imageView: ImageView?,){
        load(context,url,imageView)
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
        if (imageView == null) return
        if (context is Activity && context.isDestroyed) {
            return
        }
        val requestOptions = RequestOptions().diskCacheStrategy(
            DiskCacheStrategy.ALL
        ).skipMemoryCache(false)
        requestOptions.centerCrop()
        if (transformations != null) requestOptions.transform(transformations)
        if (placeholderId != null && placeholderId != -1) {
            requestOptions.placeholder(placeholderId)
            requestOptions.error(placeholderId)
        }
        if (errorId != null) requestOptions.error(errorId)
        Glide.with(context).load(url).apply(requestOptions).into(imageView)
    }

    fun loadBlurTransPic(
        context: Context, imgUrl: Any, maskRadius: Int = 20,
        maskSampling: Int = 4, imageView: ImageView
    ) {
        if (context is Activity && context.isDestroyed){
            return
        }
        val requestOptions = RequestOptions.bitmapTransform(BlurTransformation(maskRadius, maskSampling))
        Glide.with(context).load(imgUrl).apply(requestOptions)
            .into(imageView)
    }
}