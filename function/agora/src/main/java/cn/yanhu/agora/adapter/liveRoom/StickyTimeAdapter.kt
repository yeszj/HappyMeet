package cn.yanhu.agora.adapter.liveRoom

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.databinding.AdapterStickyTimeItemBinding
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.commonres.bean.StickyInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/12/20
 * desc:
 */
class StickyTimeAdapter : BaseQuickAdapter<StickyInfo, StickyTimeAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterStickyTimeItemBinding = AdapterStickyTimeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: StickyInfo?) {
        holder.binding.apply {
            changeSelect(position)
            tvTime.text = "${item?.hour}h"
        }
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: StickyInfo?,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            holder.binding.apply {
                changeSelect(position)
            }
        }
    }

    private fun AdapterStickyTimeItemBinding.changeSelect(position: Int) {
        if (position==selectPosition){
            tvTime.setTextColor(CommonUtils.getColor(R.color.white))
            tvTime.backgroundTintList = ColorStateList.valueOf(CommonUtils.getColor(R.color.colorMain))
        }else{
            tvTime.setTextColor(CommonUtils.getColor(cn.yanhu.commonres.R.color.cl_common))
            tvTime.backgroundTintList = ColorStateList.valueOf(CommonUtils.getColor(R.color.blackAlpha96))
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    fun getSelectItem(): StickyInfo?{
        return getItem(selectPosition)
    }

    private var selectPosition: Int = 0
    fun setSelectPosition(position: Int) {
        if (selectPosition != position) {
            val oldPosition = selectPosition
            selectPosition = position
            notifyItemChanged(oldPosition, true)
            notifyItemChanged(selectPosition, true)
        }
    }

}