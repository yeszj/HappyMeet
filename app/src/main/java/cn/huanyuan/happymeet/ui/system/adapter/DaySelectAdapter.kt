package cn.huanyuan.happymeet.ui.system.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.databinding.AdapterSelectDayItemBinding
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.CommonUtils
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class DaySelectAdapter:BaseQuickAdapter<String,DaySelectAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterSelectDayItemBinding = AdapterSelectDayItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: String?) {
        holder.binding.apply {
            tvDay.text = item
            changeSelect(position)
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: String?, payloads: List<Any>) {
        if (payloads.isNotEmpty()){
            holder.binding.apply {
                changeSelect(position)
                executePendingBindings()
            }
        }
    }

    private fun AdapterSelectDayItemBinding.changeSelect(position: Int) {
        if (position == selectPosition) {
            tvDay.setBackgroundColor(CommonUtils.getColor(R.color.white))
            tvDay.setTextColor(CommonUtils.getColor(R.color.colorMain))
        } else {
            tvDay.setBackgroundColor(CommonUtils.getColor(R.color.base_bg_gray))
            tvDay.setTextColor(CommonUtils.getColor(R.color.fontTextColor))
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    private var selectPosition: Int = 0
    fun setSelectPosition(position:Int) {
        val oldPosition = selectPosition
        if (selectPosition!=position){
            selectPosition = position
            notifyItemChanged(oldPosition,true)
            notifyItemChanged(selectPosition,true)
        }

    }
}