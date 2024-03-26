package cn.yanhu.commonres.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.commonres.bean.BannerBean
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ScreenUtils
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerAdapter
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.zj.dimens.R

/**
 * @author: zhengjun
 * created: 2024/2/6
 * desc:
 */
class MyBannerImageAdapter(
    val banner: Banner<Any, BannerAdapter<Any, *>>,
    list:MutableList<BannerBean>,
    val marginDimenId:Int = R.dimen.dp_20): BannerImageAdapter<BannerBean>(list) {
    override fun onBindView(
        holder: BannerImageHolder,
        bannerInfo: BannerBean,
        position: Int,
        size: Int
    ) {
        GlideUtils.loadAsBitmap(ActivityUtils.getTopActivity(), bannerInfo.img, object : CustomTarget<Bitmap>() {
            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                val appScreenWidth =
                    ScreenUtils.getAppScreenWidth() - CommonUtils.getDimension(marginDimenId)
                val realHeight = appScreenWidth * resource.height / resource.width
                ViewUtils.setViewSize(banner, appScreenWidth, realHeight)
                holder.imageView.setImageBitmap(resource)
            }
            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })

    }
}