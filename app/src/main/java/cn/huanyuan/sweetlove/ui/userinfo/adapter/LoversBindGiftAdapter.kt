package cn.huanyuan.sweetlove.ui.userinfo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterBindLoversGiftItemBinding
import cn.yanhu.commonres.R
import cn.yanhu.commonres.bean.GiftInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2025/2/18
 * desc:
 */
class LoversBindGiftAdapter : BaseQuickAdapter<GiftInfo, LoversBindGiftAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterBindLoversGiftItemBinding = AdapterBindLoversGiftItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: GiftInfo?) {
        holder.binding.apply {
            giftInfo = item
            changeSelect(position)
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: GiftInfo?, payloads: List<Any>) {
        if (payloads.isNotEmpty()) {
            holder.binding.apply {
                changeSelect(position)
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    private fun AdapterBindLoversGiftItemBinding.changeSelect(position: Int) {
        if (selectPosition == position) {
            ivSelect.visibility = View.VISIBLE
            vgParent.setBackgroundResource(R.drawable.bg_lovers_gift_select)
            vgSelectTag.visibility = View.VISIBLE
            tvTag.visibility = View.INVISIBLE
        } else {
            ivSelect.visibility = View.INVISIBLE
            vgParent.setBackgroundResource(R.drawable.white_corner10_alpha4)
            vgSelectTag.visibility = View.INVISIBLE
            tvTag.visibility = View.VISIBLE

        }
    }


    fun getSelectItem(): GiftInfo? {
        return getItem(selectPosition)
    }

    private var selectPosition: Int = 1
    fun setSelectPosition(position: Int) {
        if (selectPosition != position) {
            val oldPosition = selectPosition
            selectPosition = position
            notifyItemChanged(oldPosition, true)
            notifyItemChanged(selectPosition, true)
        }
    }

}