package cn.yanhu.agora.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.databinding.AdapterGiftSelectItemBinding
import cn.yanhu.commonres.bean.GiftInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/11
 * desc:
 */
class GiftSelectItemAdapter : BaseQuickAdapter<GiftInfo, GiftSelectItemAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterGiftSelectItemBinding = AdapterGiftSelectItemBinding.inflate(
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

    private fun AdapterGiftSelectItemBinding.changeSelect(position: Int) {
        if (selectPosition == position) {
            vgParent.setBackgroundResource(cn.yanhu.commonres.R.drawable.main_alpha10_stroke_r8)
        } else {
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
            notifyItemChanged(oldPosition, true)
            notifyItemChanged(selectPosition, true)
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