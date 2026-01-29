package cn.yanhu.baselib.utils

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.TextUtils
import android.widget.ImageView
import androidx.annotation.DrawableRes
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.ext.logComToFile
import cn.yanhu.baselib.view.CircleBorderTransformation
import coil.load
import coil.transform.CircleCropTransformation

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
}