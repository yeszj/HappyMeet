package cn.yanhu.dynamic

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import cn.yanhu.commonres.utils.GifLoadUtils
import cn.yanhu.commonres.view.gridimage.NineGridLayout
import cn.yanhu.commonres.view.gridimage.RatioImageView
import cn.yanhu.dynamic.bean.DynamicInfo
import cn.yanhu.dynamic.pop.CustomDynamicImagePopupView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

/**
 * @author: zhengjun
 * created: 2023/7/14
 * desc:
 */
open class DynamicNineGridLayout : NineGridLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)


    @SuppressLint("CheckResult")
    override fun displayOneImage(
        imageView: RatioImageView,
        url: String,
        parentWidth: Int,
    ): Boolean {
        Glide.with(mContext).asBitmap().load(url).placeholder(imageView.drawable)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(
                    bitmap: Bitmap,
                    transition: Transition<in Bitmap?>?,
                ) {
                    val w = bitmap.width
                    val h = bitmap.height
                    val newW: Int
                    val newH: Int
                    if (h > w * MAX_W_H_RATIO) { //h:w = 5:3
                        newW = parentWidth / 2
                        newH = newW * 5 / 3
                    } else if (h < w) { //h:w = 2:3
                        newW = parentWidth * 2 / 3
                        newH = newW * 2 / 3
                    } else { //newH:h = newW :w
                        newW = parentWidth / 2
                        newH = h * newW / w
                    }
                    setOneImageLayoutParams(imageView, newW, newH)
                    GifLoadUtils.loadHasGifPic(url, imageView, context = mContext)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
        return false
    }

    @SuppressLint("CheckResult")
    override fun displayImage(imageView: RatioImageView, url: String) {
        GifLoadUtils.loadHasGifPic(url, imageView, context = mContext)
    }

    private var dynamicInfo: DynamicInfo?=null
    fun setUrlList(urlList: MutableList<String>?, info: DynamicInfo) {
        dynamicInfo = info
        super.setUrlList(urlList)
    }

    override fun onClickImage(i: Int, url: String, urlList: List<String>) {
        val childAt = getChildAt(i) as ImageView?
        CustomDynamicImagePopupView.asImageViewer(context, childAt, i,
            { popupView, position ->
                try {
                    (popupView as CustomDynamicImagePopupView).setIndicatorStyle(
                        position,dynamicInfo!!
                    )
                    popupView.playVideo(urlList[position])
                    val ivBg2 = getChildAt(position) as ImageView
                    popupView.updateSrcView(ivBg2)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, momentInfo = dynamicInfo!!)
    }

    companion object {
        protected const val MAX_W_H_RATIO = 3
    }
}