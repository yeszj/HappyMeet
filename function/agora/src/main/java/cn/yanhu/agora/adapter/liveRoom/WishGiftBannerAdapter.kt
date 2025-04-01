package cn.yanhu.agora.adapter.liveRoom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.commonres.bean.WishInfo
import cn.yanhu.agora.databinding.AdapterWishBannerItemBinding
import com.youth.banner.adapter.BannerAdapter
import cn.yanhu.agora.R
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.widget.spans.Spans

/**
 * @author: zhengjun
 * created: 2025/3/27
 * desc:
 */
class WishGiftBannerAdapter(private val context: Context, list: MutableList<WishInfo>) :
    BannerAdapter<WishInfo, WishGiftBannerAdapter.ViewHolder>(list) {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mBinding: AdapterWishBannerItemBinding? = null

        init {
            mBinding = DataBindingUtil.bind(view)
        }
    }

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val inflate =
            LayoutInflater.from(context).inflate(R.layout.adapter_wish_banner_item,parent, false)
        return ViewHolder(inflate)
    }

    override fun onBindView(holder: ViewHolder?, data: WishInfo?, position: Int, size: Int) {
        holder?.mBinding?.apply {
            if (data?.sunNum==0){
                ivGift.setImageResource(R.drawable.icon_wish_empty)
            }else{
                GlideUtils.loadImage(context, data?.giftIcon, ivGift)
            }
            val build =
                Spans.builder().text(data?.finishNum.toString()).color(CommonUtils.getColor(cn.yanhu.baselib.R.color.colorMain)).text("/" + data?.sunNum.toString())
                    .build()
            tvCount.text = build
            executePendingBindings()
        }
    }
}