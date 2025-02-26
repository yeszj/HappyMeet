package cn.huanyuan.sweetlove.ui.setting.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.bean.SwitchConfigInfo
import cn.huanyuan.sweetlove.databinding.AdapterPrivacySwitchSetItemBinding
import cn.yanhu.commonres.R
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/4/9
 * desc:
 */
class PrivacySwitchSetAdapter : BaseQuickAdapter<SwitchConfigInfo, PrivacySwitchSetAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterPrivacySwitchSetItemBinding = AdapterPrivacySwitchSetItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: SwitchConfigInfo?) {
        holder.binding.apply {
            switchInfo = item
            bindStatus(holder, item)
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(
        holder: VH, position: Int, item: SwitchConfigInfo?, payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            bindStatus(holder, item)
        }
    }

    private fun bindStatus(
        holder: VH,
        item: SwitchConfigInfo?
    ) {
        holder.binding.apply {
            if (item?.status == 1) {
                toggleSwitch.setImageResource(R.drawable.svg_toggle_on_red)
            } else {
                toggleSwitch.setImageResource(R.drawable.svg_toggle_off_gray)
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

}