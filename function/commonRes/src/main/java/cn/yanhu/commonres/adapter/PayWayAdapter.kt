package cn.yanhu.commonres.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.commonres.bean.PayWayInfo
import cn.yanhu.commonres.databinding.AdapterPayWayItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/7
 * desc:
 */
class PayWayAdapter : BaseQuickAdapter<PayWayInfo, PayWayAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterPayWayItemBinding = AdapterPayWayItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: PayWayInfo?) {
        holder.binding.apply {
            if (item==null){
                return
            }
            payWayInfo = item
            ivLogo.setImageResource(item.iconId)
            changeSelect(position)
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: PayWayInfo?,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            holder.binding.apply {
                changeSelect(position)
            }
        }
    }

    private fun AdapterPayWayItemBinding.changeSelect(position: Int) {
        if (selectPosition == position) {
            viewBg.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_pay_select)
            ivSelect.visibility = View.VISIBLE
        } else {
            viewBg.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_pay_normal)
            ivSelect.visibility = View.INVISIBLE
        }
    }

    private var selectPosition: Int = 0
    fun setSelectPosition(position: Int) {
        if (position!=selectPosition){
            val oldPosition = selectPosition
            selectPosition = position
            notifyItemChanged(oldPosition,true)
            notifyItemChanged(selectPosition,true)
        }
    }

    fun getSelectPayType():Int{
        return getItem(selectPosition)!!.payType
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}