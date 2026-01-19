package cn.yanhu.baselib.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import cn.yanhu.baselib.R
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.controller.ControllerListener
import com.facebook.drawee.generic.GenericDraweeHierarchy
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.ImageInfo
import androidx.core.graphics.drawable.toDrawable


/**
 * @author: zhengjun
 * created: 2026/1/19
 * desc:
 */
object FrescoUtils {

    @SuppressLint("CheckResult")
    fun load(
        context: Context,
        url: Any?,
        imageView: SimpleDraweeView?,
        @DrawableRes placeholderId: Int? = R.drawable.icon_portrait
    ) {
        try {
            if (isUrlNull(url)) {
                return
            }
            if (imageView == null) {
                return
            }
            if (isDestroy(context)) {
                return
            }
            val currentDrawable = imageView.drawable
            val hierarchy = imageView.hierarchy

            if (currentDrawable != null && hierarchy != null) {
                // 创建安全的副本
                val placeholderCopy = createDrawableCopy(currentDrawable)
                hierarchy.setPlaceholderImage(placeholderCopy)

                // 记录这是临时占位图
                placeholderCopy?.let {
                    imageView.setTag(R.id.previous_placeholder, it)
                }
            }
            imageView.setImageURI(url.toString().toUri())
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    fun createDrawableCopy(drawable: Drawable): Drawable? {
        return try {
            when (drawable) {
                is BitmapDrawable -> {
                    val bitmap = drawable.bitmap
                    if (!bitmap.isRecycled) {
                        // 创建Bitmap的副本
                        val bitmapCopy = bitmap.copy(bitmap.config, false)
                        bitmapCopy.toDrawable(Resources.getSystem())
                    } else {
                        null
                    }
                }
                is ColorDrawable -> {
                    drawable.color.toDrawable()
                }
                is GradientDrawable -> {
                    val copy = GradientDrawable()
                    copy.cornerRadius = drawable.cornerRadius
                    copy.setColor(drawable.color?.defaultColor ?: Color.TRANSPARENT)
                    copy
                }
                else -> {
                    // 对于其他类型的Drawable，使用ConstantState复制
                    drawable.constantState?.newDrawable()?.mutate()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 检查Drawable是否有效
    fun isDrawableValid(drawable: Drawable?): Boolean {
        if (drawable == null) return false

        return when (drawable) {
            is BitmapDrawable -> {
                val bitmap = drawable.bitmap
                bitmap != null && !bitmap.isRecycled
            }
            else -> true
        }
    }

    private fun createStableHierarchy(
        context: Context,
        placeholderResId: Int?,
        currentDrawable: Drawable?
    ): GenericDraweeHierarchy {
        val builder = GenericDraweeHierarchyBuilder(context.resources)
        // 配置层次结构
        builder.fadeDuration = 0

        // 如果有当前显示的图片，将其作为占位图
        if (currentDrawable != null) {
            builder.placeholderImage = currentDrawable
        } else {
            if (placeholderResId != null && placeholderResId != -1) {
                builder.placeholderImage = ContextCompat.getDrawable(context, placeholderResId)
            }
        }

        // 禁用失败图片的显示（避免闪烁）
        builder.failureImage = null

        return builder.build()
    }

    private fun getCurrentDisplayedDrawable(draweeView: SimpleDraweeView): Drawable? {
        return try {
            val hierarchy = draweeView.hierarchy
            val topLevelDrawable = hierarchy?.topLevelDrawable
            topLevelDrawable
        } catch (e: Exception) {
            null
        }
    }

    private fun createStableListener(draweeView: SimpleDraweeView): ControllerListener<ImageInfo> {
        return object : BaseControllerListener<ImageInfo>() {
            override fun onIntermediateImageFailed(id: String?, throwable: Throwable?) {
                // 中间图片失败时不显示错误图片（避免闪烁）
                // 保持当前显示的内容
            }

            override fun onFailure(id: String?, throwable: Throwable?) {
                // 加载失败时也不显示错误图片
                // 可以在这里记录日志，但不改变UI
                Log.e("StableImageDisplay", "Load failed for: $id", throwable)
            }
        }
    }

    private fun isUrlNull(url: Any?): Boolean {
        return url == null || (url is String && TextUtils.isEmpty(url.toString()))
    }

    private fun isDestroy(context: Context): Boolean {
        return context is Activity && context.isDestroyed
    }

}