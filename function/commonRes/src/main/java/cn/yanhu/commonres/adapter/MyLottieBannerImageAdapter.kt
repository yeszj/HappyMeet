package cn.yanhu.commonres.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.R
import cn.yanhu.commonres.bean.BannerBean
import cn.yanhu.commonres.databinding.AdapterLottieBannerItemBinding
import cn.yanhu.commonres.router.PageIntentUtil
import com.youth.banner.adapter.BannerAdapter

/**
 * @author: zhengjun
 * created: 2024/2/6
 * desc:
 */
class MyLottieBannerImageAdapter(
    val context: Context, list: MutableList<BannerBean>
) : BannerAdapter<BannerBean, MyLottieBannerImageAdapter.ViewHolder>(list) {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mBinding: AdapterLottieBannerItemBinding? = null

        init {
            mBinding = DataBindingUtil.bind(view)
        }
    }

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val inflate =
            LayoutInflater.from(context).inflate(R.layout.adapter_lottie_banner_item, parent, false)
        return ViewHolder(inflate)
    }

    override fun onBindView(holder: ViewHolder?, data: BannerBean?, position: Int, size: Int) {
        holder?.mBinding?.apply {
            data ?: return
            try {
                val img = data.img
                if (img.endsWith(".json")) {
                    ivImage.setAnimationFromUrl(img)
                    ivImage.playAnimation()
                } else {
                    GlideUtils.load(
                        context,
                        img,
                        ivImage,
                        placeholderId = R.drawable.pic_default_bg
                    )
                }
                ivImage.setOnSingleClickListener {
                    PageIntentUtil.url2Page(context, data.pageUrl)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}