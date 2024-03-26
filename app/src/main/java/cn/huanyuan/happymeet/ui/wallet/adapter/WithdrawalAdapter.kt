package cn.huanyuan.happymeet.ui.wallet.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.databinding.AdapterWithdrawalItemBinding
import cn.yanhu.commonres.R
import cn.yanhu.commonres.bean.WithDrawInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/8
 * desc:
 */
class WithdrawalAdapter:BaseQuickAdapter<WithDrawInfo,WithdrawalAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterWithdrawalItemBinding = AdapterWithdrawalItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: WithDrawInfo?) {
        holder.binding.apply {
            withdrawalInfo = item
            changeSelect(position)
        }
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: WithDrawInfo?,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()){
            holder.binding.apply {
                changeSelect(position)
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    private fun AdapterWithdrawalItemBinding.changeSelect(position: Int) {
        if (selectPosition == position) {
            ivSelect.visibility = View.VISIBLE
            viewBg.setBackgroundResource(R.drawable.bg_common_item_select)
        } else {
            ivSelect.visibility = View.INVISIBLE
            viewBg.setBackgroundResource(R.drawable.bg_pay_normal)
        }
        executePendingBindings()
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