package cn.huanyuan.sweetlove.ui.invite.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.bean.InviteRecordResponse
import cn.huanyuan.sweetlove.databinding.AdapterInviteRecordHeadItemBinding
import cn.yanhu.commonres.adapter.MyLottieBannerImageAdapter
import com.chad.library.adapter4.BaseSingleItemAdapter

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class InviteRecordHeadAdapter(val mContext: FragmentActivity) :
    BaseSingleItemAdapter<InviteRecordResponse, InviteRecordHeadAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterInviteRecordHeadItemBinding = AdapterInviteRecordHeadItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: VH,
        @SuppressLint("RecyclerView") item: InviteRecordResponse?
    ) {
        if (item == null) {
            return
        }
        holder.binding.apply {
            tvTotalCount.text = item.totalInviteCount.toString()
            tvTotalIncome.text = item.totalIncome
            if (TextUtils.isEmpty(item.numDesc)) {
                tvNumDesc.visibility = View.INVISIBLE
            } else {
                tvNumDesc.visibility = View.VISIBLE
                tvNumDesc.text = item.numDesc
            }
            bindBanner(item)
        }
    }

    private fun AdapterInviteRecordHeadItemBinding.bindBanner(item: InviteRecordResponse) {
        val banners = item.bannerList
        if (banners.isEmpty()){
            banner.visibility = View.GONE
            return
        }else{
            banner.visibility = View.VISIBLE
        }
        var bannerAdapter = banner.tag as MyLottieBannerImageAdapter?
        if (bannerAdapter == null) {
            banner.addBannerLifecycleObserver(mContext)
            bannerAdapter = MyLottieBannerImageAdapter(mContext, banners)
            banner.setAdapter(bannerAdapter)
            banner.tag = bannerAdapter
        }else{
            bannerAdapter.setDatas(banners)
        }
    }


    var filterName = "累计收益"
    var filterTimeName = "全部好友"

    override fun onBindViewHolder(holder: VH, item: InviteRecordResponse?, payloads: List<Any>) {
        if (!payloads.isNullOrEmpty()) {
            holder.binding.apply {
                tvFilter.text = filterName
                tvFilterTime.text = filterTimeName
            }
        }
    }


    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}