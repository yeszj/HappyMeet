package cn.yanhu.commonres.view.gridimage

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.commonres.utils.GifLoadUtils
import com.lxj.xpopup.core.ImageViewerPopupView
import com.lxj.xpopup.interfaces.OnSrcViewUpdateListener

/**
 * @author: zhengjun
 * created: 2023/7/14
 * desc:
 */
open class ImageNineGridLayout : NineGridLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)


    @SuppressLint("CheckResult")
    override fun displayOneImage(
        imageView: RatioImageView,
        url: String,
        parentWidth: Int,
    ): Boolean {
        GifLoadUtils.loadHasGifPic(url,imageView, context = mContext)
        return false
    }

    @SuppressLint("CheckResult")
    override fun displayImage(imageView: RatioImageView, url: String) {
        GifLoadUtils.loadHasGifPic(url,imageView, context = mContext)
    }

    override fun onClickImage(i: Int, url: String, urlList: List<String>) {
        val imageView = getChildAt(i) as ImageView?
        DialogUtils.showImageViewerDialog(imageView,i, urlList.toMutableList(),object : OnSrcViewUpdateListener{
            override fun onSrcViewUpdate(popupView: ImageViewerPopupView, position: Int) {
                try {
                    val ivBg2 = getChildAt(position) as ImageView
                    popupView.updateSrcView(ivBg2)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        })
        //CustomDynamicImagePopupView.asImageViewer(context,vgSingleVideo.ivBg,0,0,momentInfo = list )
    }

    companion object {
        protected const val MAX_W_H_RATIO = 3
    }
}