package cn.huanyuan.happymeet.ui.wallet.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.databinding.AdapterRoseRechargeItemBinding
import cn.yanhu.commonres.R
import cn.yanhu.commonres.bean.RoseRechargeBean
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/8
 * desc:
 */
class RoseRechargeAdapter : BaseQuickAdapter<RoseRechargeBean, RoseRechargeAdapter.VH>() {

    class VH(
        parent: ViewGroup,
        val binding: AdapterRoseRechargeItemBinding = AdapterRoseRechargeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: RoseRechargeBean?) {
        holder.binding.apply {
            rechargeInfo = item
            changeSelect(position)
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: RoseRechargeBean?,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            holder.binding.apply {
                changeSelect(position)
            }
        }
    }

    private fun AdapterRoseRechargeItemBinding.changeSelect(position: Int) {
        if (selectPosition == position) {
            ivSelect.visibility = View.VISIBLE
            vgParent.setBackgroundResource(R.drawable.bg_common_item_select)
        } else {
            ivSelect.visibility = View.INVISIBLE
            vgParent.setBackgroundResource(R.drawable.bg_pay_normal)
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
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