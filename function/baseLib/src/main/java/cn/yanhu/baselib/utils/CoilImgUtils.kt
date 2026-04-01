package cn.yanhu.baselib.utils

import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.NinePatch
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.NinePatchDrawable
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.adapters.ViewBindingAdapter.setPadding
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.ext.logComToFile
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.view.CircleBorderTransformation
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Size
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.bumptech.glide.request.target.CustomTarget
import com.luck.picture.lib.utils.ActivityCompatHelper
import ua.anatolii.graphics.ninepatch.NinePatchChunk
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author: zhengjun
 * created: 2026/1/20
 * desc:
 */
object CoilImgUtils {
    @SuppressLint("CheckResult")
    fun loadCircleImg(
        url: Any?,
        imageView: ImageView?,
        @DrawableRes placeholderId: Int? = R.drawable.icon_portrait,
        borderWidth: Float = 0f,
        borderColor: Int = Color.WHITE
    ) {
        try {
            if (isUrlNull(url)) {
                return
            }
            if (imageView == null) {
                logComToFile("glide", "加载失败：url=${url},imageView==null")
                return
            }
            val placeholderIds = if (placeholderId == 0) {
                R.drawable.icon_portrait
            } else {
                placeholderId
            }
            imageView.load(url) {
                if (placeholderIds != null && placeholderId != -1) {
                    placeholder(placeholderIds)
                }
                if (borderWidth>0){
                    transformations(CircleBorderTransformation(borderWidth,borderColor))
                }else{
                    transformations(CircleCropTransformation())
                }
            }

        } catch (e: Exception) {
            logComToFile("glide", "加载失败：url=${url},error=${e.message}")
            e.printStackTrace()
        }

    }

    fun loadRoundImg(url: String?, imageView: ImageView,roundDimens: Float) {
        imageView.load(url){
            placeholder(R.drawable.image_placeholder)
            transformations(RoundedCornersTransformation(roundDimens))
        }
    }

    @SuppressLint("CheckResult")
    fun loadImg(
        url: Any?,
        imageView: ImageView?,
        @DrawableRes placeholderId: Int? = -1,
    ) {
        try {
            if (isUrlNull(url)) {
                return
            }
            if (imageView == null) {
                logComToFile("glide", "加载失败：url=${url},imageView==null")
                return
            }
            val placeholderIds = if (placeholderId == 0) {
                R.drawable.icon_portrait
            } else {
                placeholderId
            }
            imageView.load(url) {
                if (placeholderIds != null && placeholderId != -1) {
                    placeholder(placeholderIds)
                }
            }

        } catch (e: Exception) {
            logComToFile("glide", "加载失败：url=${url},error=${e.message}")
            e.printStackTrace()
        }

    }

    private fun isUrlNull(url: Any?): Boolean {
        return url == null || (url is String && TextUtils.isEmpty(url.toString()))
    }


    fun loadNinePatchImage(context: Context,url: String?,onLoadNinePatchImageListener: OnLoadNinePatchImageListener) {
        if (isUrlNull(url)){
            return
        }
        context.imageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(url)
                .target(onSuccess = { drawable ->
                    val bitmapDrawable = drawable as? BitmapDrawable
                    val finalDrawable = if (bitmapDrawable != null) {
                        val bitmap = bitmapDrawable.bitmap
                        NinePatchChunk.create9PatchDrawable(
                            context,
                            bitmap,
                            "qipao"  // srcName，可为 null
                        )
                    } else {
                        drawable
                    }
                    onLoadNinePatchImageListener.onLoadNinePatchImage(finalDrawable)
                }, onError = {
                    // 错误处理
                })
                .build()
        )
    }
    interface OnLoadNinePatchImageListener {
        fun onLoadNinePatchImage(drawable: Drawable)
    }

}