package cn.yanhu.commonres.adapter

import android.view.ViewGroup
import android.widget.ImageView
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.commonres.R
import cn.yanhu.commonres.bean.BannerBean
import cn.yanhu.commonres.manager.ImageThumbUtils
import com.makeramen.roundedimageview.RoundedImageView
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerAdapter
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder

/**
 * @author: zhengjun
 * created: 2024/2/6
 * desc:
 */
class CircleBannerImageAdapter(
    val banner: Banner<Any, BannerAdapter<Any, *>>,
    list:MutableList<String>): BannerImageAdapter<String>(list) {

        override fun onCreateHolder(parent: ViewGroup?, viewType: Int): BannerImageHolder {
            val imageView = RoundedImageView(parent!!.context)
            imageView.isOval = true
            //注意，必须设置为match_parent，这个是viewpager2强制要求的
            val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            imageView.setLayoutParams(params)
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP)
            return BannerImageHolder(imageView)
        }

    override fun onBindView(
        holder: BannerImageHolder,
        imgUrl: String,
        position: Int,
        size: Int
    ) {
        GlideUtils.load(banner.context, ImageThumbUtils.getThumbUrl(imgUrl),holder.imageView)
    }
}