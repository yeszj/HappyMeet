package cn.yanhu.imchat.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.imchat.databinding.AdapterSendGiftItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/11
 * desc:
 */
class SendGiftItemAdapter : BaseQuickAdapter<GiftInfo, SendGiftItemAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterSendGiftItemBinding = AdapterSendGiftItemBinding.inflate(
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

    private fun AdapterSendGiftItemBinding.changeSelect(position: Int) {
        if (selectPosition == position) {
            tvSend.visibility = View.VISIBLE
            tvGiftName.visibility = View.GONE
            vgParent.setBackgroundResource(cn.yanhu.commonres.R.drawable.white_stroke_alpha10_corner_8)
        } else {
            tvSend.visibility = View.INVISIBLE
            tvGiftName.visibility = View.VISIBLE
            vgParent.setBackgroundResource(cn.yanhu.commonres.R.drawable.shape_transparent)
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

     private var selectPosition: Int = 0
    fun getSelectItem(): GiftInfo? {
        return getItem(selectPosition)
    }

    fun getSelectPosition():Int{
        return selectPosition
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectPosition(position: Int) {
        if (selectPosition != position) {
            val oldPosition = selectPosition
            selectPosition = position
            notifyDataSetChanged()
//            notifyItemChanged(oldPosition, true)
//            notifyItemChanged(selectPosition, true)
        }
    }

    fun getDefaultSelectPosition(giftId: Int): Int {
        for (i in items.indices) {
            if (items[i].id == giftId) {
                return i
            }
        }
        return 0
    }
}