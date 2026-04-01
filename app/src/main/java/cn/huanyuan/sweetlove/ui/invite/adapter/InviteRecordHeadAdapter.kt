package cn.huanyuan.sweetlove.ui.invite.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.bean.InviteRecordResponse
import cn.huanyuan.sweetlove.databinding.AdapterInviteRecordHeadItemBinding
import cn.huanyuan.sweetlove.ui.invite.InviteViewModel
import cn.huanyuan.sweetlove.ui.invite.MyInviteRecordActivity
import cn.yanhu.baselib.widget.SimpleTextWatcher
import cn.yanhu.commonres.adapter.MyLottieBannerImageAdapter
import com.chad.library.adapter4.BaseSingleItemAdapter
import com.google.android.material.internal.TextWatcherAdapter

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class InviteRecordHeadAdapter(val mContext: FragmentActivity,val mViewModel: InviteViewModel) :
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
            viewModel = mViewModel
            tvFilter.text = filterName
            tvFilterTime.text = filterTimeName
            tvTotalCount.text = item.totalInviteCount.toString()
            tvTotalIncome.text = item.monthInviteIncome
            if (TextUtils.isEmpty(item.numDesc)) {
                tvNumDesc.visibility = View.INVISIBLE
            } else {
                tvNumDesc.visibility = View.VISIBLE
                tvNumDesc.text = item.numDesc
            }
            bindBanner(item)

            if (etContent.tag == null){
                val txtWatcher = object : SimpleTextWatcher() {
                    override fun afterTextChanged(s: Editable?) {
                        super.afterTextChanged(s)
                        if (mContext is MyInviteRecordActivity){
                            mContext.refreshData()
                        }
                    }
                }
                etContent.tag = txtWatcher
                etContent.addTextChangedListener(txtWatcher)
            }

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


    var filterName = ""
    var filterTimeName = ""
    override fun onBindViewHolder(holder: VH, item: InviteRecordResponse?, payloads: List<Any>) {
        if (payloads.isNotEmpty()) {
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