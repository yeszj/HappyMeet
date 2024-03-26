package cn.huanyuan.happymeet.ui.system.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.bean.TimeInfo
import cn.huanyuan.happymeet.databinding.AdapterSelectTimeItemBinding
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.CommonUtils
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class TimeSelectAdapter : BaseQuickAdapter<TimeInfo, TimeSelectAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterSelectTimeItemBinding = AdapterSelectTimeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: VH,
        @SuppressLint("RecyclerView") position: Int,
        item: TimeInfo?
    ) {
        holder.binding.apply {
            if (item == null) {
                return
            }
            if (dayItemPosition==0){
                if (currentHour >= item.startHour) {
                    tvType.alpha = 1f
                } else {
                    tvType.alpha = 0.5f
                }
                if (selectPosition != -1) {
                    if (currentHour in item.startHour..<item.endHour) {
                        selectPosition = position
                    }
                }
            }else{
                tvType.alpha = 1f
            }
            if (selectPosition == -1) {
                if (currentHour in item.startHour..<item.endHour) {
                    selectPosition = position
                }
            }
            tvType.text = "${item.startHour}:00-${item.endHour}:00"
            changeSelect(position)
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: TimeInfo?, payloads: List<Any>) {
        if (payloads.isNotEmpty()) {
            holder.binding.apply {
                changeSelect(position)
                executePendingBindings()
            }
        }
    }

    private fun AdapterSelectTimeItemBinding.changeSelect(position: Int) {
        if (position == selectPosition) {
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

    fun getSelectItem(): TimeInfo? {
        return getItem(selectPosition)
    }

    private var selectPosition: Int = -1
    fun setSelectPosition(position: Int) {
        val oldPosition = selectPosition
        if (selectPosition != position) {
            selectPosition = position
            notifyItemChanged(oldPosition, true)
            notifyItemChanged(selectPosition, true)
        }
    }

    private var currentHour: Int = 0
    fun setCurrentHour(hour: Int) {
        currentHour = hour
    }

    private var dayItemPosition = 0
    @SuppressLint("NotifyDataSetChanged")
    fun setSelectDayItem(position: Int){
        dayItemPosition = position
        notifyDataSetChanged()
    }
}