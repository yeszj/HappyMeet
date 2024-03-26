package cn.huanyuan.happymeet.ui.setting.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.databinding.AdapterAppSettingItemBinding
import cn.yanhu.commonres.bean.SettingItemInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class AppSettingAdapter:BaseQuickAdapter<SettingItemInfo,AppSettingAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterAppSettingItemBinding = AdapterAppSettingItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: SettingItemInfo?) {
        holder.binding.apply {
            itemInfo = item
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: SettingItemInfo?,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()){
            holder.binding.apply {
                tvDesc.text = item?.desc
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}