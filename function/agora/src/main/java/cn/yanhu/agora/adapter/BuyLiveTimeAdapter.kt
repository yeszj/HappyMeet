package cn.yanhu.agora.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.bean.LiveTimePriceInfo
import cn.yanhu.agora.databinding.AdapterLiveTimeItemBinding
import cn.yanhu.commonres.R
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/8
 * desc:
 */
class BuyLiveTimeAdapter : BaseQuickAdapter<LiveTimePriceInfo, BuyLiveTimeAdapter.VH>() {

    class VH(
        parent: ViewGroup,
        val binding: AdapterLiveTimeItemBinding = AdapterLiveTimeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: LiveTimePriceInfo?) {
        holder.binding.apply {
            priceInfo = item
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: LiveTimePriceInfo?,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            holder.binding.apply {
                changeSelect(position)
            }
        }
    }

    private fun AdapterLiveTimeItemBinding.changeSelect(position: Int) {
        if (selectPosition == position) {
            ivSelect.visibility = View.VISIBLE
            vgParent.setBackgroundResource(R.drawable.bg_common_item_select)
        } else {            changeSelect(position)

            ivSelect.visibility = View.INVISIBLE
            vgParent.setBackgroundResource(R.drawable.bg_pay_normal)
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }


    fun getSelectItem(): LiveTimePriceInfo?{
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