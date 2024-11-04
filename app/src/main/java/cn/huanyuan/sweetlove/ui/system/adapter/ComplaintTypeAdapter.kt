package cn.huanyuan.sweetlove.ui.system.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterComplaintTypeItemBinding
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.commonres.bean.ReportConfigInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class ComplaintTypeAdapter :
    BaseQuickAdapter<ReportConfigInfo.ConfigInfo, ComplaintTypeAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterComplaintTypeItemBinding = AdapterComplaintTypeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: ReportConfigInfo.ConfigInfo?) {
        holder.binding.apply {
            tvType.text = item?.desc
            changeSelect(position)
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: ReportConfigInfo.ConfigInfo?,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            holder.binding.apply {
                changeSelect(position)
                executePendingBindings()
            }
        }
    }

    private fun AdapterComplaintTypeItemBinding.changeSelect(position: Int) {
        val item = getItem(position)?:return
        if (item.select) {
            tvType.backgroundTintList =
                ColorStateList.valueOf(CommonUtils.getColor(R.color.colorMain))
            tvType.setTextColor(CommonUtils.getColor(R.color.white))
        } else {
            tvType.backgroundTintList =
                ColorStateList.valueOf(CommonUtils.getColor(R.color.blackAlpha96))
            tvType.setTextColor(CommonUtils.getColor(R.color.color6))
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    fun setSelectPosition(position: Int) {
        val item = getItem(position) ?: return
        item.select = !item.select
        notifyItemChanged(position, true)
    }
}