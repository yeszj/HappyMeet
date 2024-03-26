package cn.huanyuan.happymeet.ui.userinfo.dressUp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.databinding.AdapterDressBuyPriceItemBinding
import cn.yanhu.commonres.R
import cn.yanhu.commonres.bean.StorePriceInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:
 */
class GoodsPriceAdapter : BaseQuickAdapter<StorePriceInfo, GoodsPriceAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterDressBuyPriceItemBinding = AdapterDressBuyPriceItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: StorePriceInfo?) {
        holder.binding.apply {
            priceInfo = item
            changeSelect(position)
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: StorePriceInfo?,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            holder.binding.apply {
                changeSelect(position)
            }
        }
    }

    private fun AdapterDressBuyPriceItemBinding.changeSelect(position: Int) {
        if (selectPosition == position) {
            ivSelect.visibility = View.VISIBLE
            vgParent.setBackgroundResource(R.drawable.bg_dress_price_select)
        } else {
            ivSelect.visibility = View.INVISIBLE
            vgParent.setBackgroundResource(R.drawable.bg_dress_price_normal)
        }
    }

    fun getSelectItem():StorePriceInfo?{
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