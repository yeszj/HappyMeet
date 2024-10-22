package cn.huanyuan.sweetlove.ui.setting.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.bean.AppCheckItemInfo
import cn.huanyuan.sweetlove.databinding.AdapterAppCheckItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class AppCheckAdapter : BaseQuickAdapter<AppCheckItemInfo, AppCheckAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterAppCheckItemBinding = AdapterAppCheckItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: AppCheckItemInfo?) {
        holder.binding.apply {
            checkInfo = item
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}