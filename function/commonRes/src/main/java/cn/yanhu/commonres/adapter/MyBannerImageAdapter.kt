package cn.yanhu.commonres.adapter

import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.commonres.bean.BannerBean
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerAdapter
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder

/**
 * @author: zhengjun
 * created: 2024/2/6
 * desc:
 */
class MyBannerImageAdapter(
    val banner: Banner<Any, BannerAdapter<Any, *>>,
    list:MutableList<BannerBean>): BannerImageAdapter<BannerBean>(list) {
    override fun onBindView(
        holder: BannerImageHolder,
        bannerInfo: BannerBean,
        position: Int,
        size: Int
    ) {
        GlideUtils.load(banner.context,bannerInfo.img,holder.imageView,placeholderId = cn.yanhu.commonres.R.drawable.pic_default_bg)
    }
}