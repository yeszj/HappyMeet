package cn.yanhu.agora.adapter.song

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.databinding.AdapterSongUserChooseItemBinding
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.commonres.bean.SeatUserInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2025/1/15
 * desc:
 */
class SongUserChooseAdapter(private val ownerUserId:String) : BaseQuickAdapter<SeatUserInfo, SongUserChooseAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterSongUserChooseItemBinding = AdapterSongUserChooseItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: SeatUserInfo?) {
        holder.binding.apply {
            if (item?.userId == ownerUserId){
                tvSeatNum.text = "主持"
            }else{
                val seat = item!!.seatId - 1
                tvSeatNum.text = seat.toString()
            }
            seatUserInfo = item
            changeSelect(position)
        }
    }

    private fun AdapterSongUserChooseItemBinding.changeSelect(position: Int) {
        if (position == selectPosition) {
            tvSeatNum.backgroundTintList =
                ColorStateList.valueOf(CommonUtils.getColor(R.color.colorMain))
            ivAvatar.setBorderColor(ColorStateList.valueOf(CommonUtils.getColor(R.color.colorMain)))
        } else {
            tvSeatNum.backgroundTintList =
                ColorStateList.valueOf(CommonUtils.getColor(R.color.fontGrayColor))
            ivAvatar.setBorderColor(ColorStateList.valueOf(CommonUtils.getColor(R.color.transparent)))
        }
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: SeatUserInfo?,
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

    fun getSelectItem(): SeatUserInfo?{
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