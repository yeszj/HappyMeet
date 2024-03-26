package cn.yanhu.agora.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.bean.RoomTypeInfo
import cn.yanhu.agora.databinding.AdapterRoomTypeItemBinding
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.CommonUtils
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class RoomTypeSelectAdapter:BaseQuickAdapter<RoomTypeInfo,RoomTypeSelectAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterRoomTypeItemBinding = AdapterRoomTypeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: RoomTypeInfo?) {
        holder.binding.apply {
            tvType.text = item?.name
            changeSelect(position)
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: RoomTypeInfo?, payloads: List<Any>) {
        if (payloads.isNotEmpty()){
            holder.binding.apply {
                changeSelect(position)
                executePendingBindings()
            }
        }
    }

    private fun AdapterRoomTypeItemBinding.changeSelect(position: Int) {
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

    fun getSelectItem():RoomTypeInfo?{
       return getItem(selectPosition)
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