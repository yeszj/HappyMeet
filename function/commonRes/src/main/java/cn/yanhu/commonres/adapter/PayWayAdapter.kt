package cn.yanhu.commonres.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.commonres.bean.PayWayInfo
import cn.yanhu.commonres.databinding.AdapterPayWayItemBinding
import com.chad.library.adapter4.BaseQuickAdapter
import androidx.core.graphics.toColorInt
import cn.yanhu.baselib.utils.GlideUtils

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
            if (TextUtils.isEmpty(item.icon)){
                ivLogo.setImageResource(item.iconId)
            }else{
                GlideUtils.load(context, item.icon, ivLogo)
            }
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
            val item = getItem(position)
            if (item?.payType == PayWayInfo.TYPE_ALIPAY){
                viewBg.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_pay_select)
                ivSelect.imageTintList  = ColorStateList.valueOf("#2D9AFF".toColorInt())
            }else if (item?.payType == PayWayInfo.TYPE_WXPAY){
                viewBg.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_pay_select_wx)
                ivSelect.imageTintList  = ColorStateList.valueOf("#00CF5F".toColorInt())
            }else{
                viewBg.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_pay_select_bank)
                ivSelect.imageTintList  = ColorStateList.valueOf("#FFB039".toColorInt())
            }
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