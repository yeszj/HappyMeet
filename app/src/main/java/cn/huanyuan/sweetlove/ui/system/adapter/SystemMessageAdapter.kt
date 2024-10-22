package cn.huanyuan.sweetlove.ui.system.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.commonres.bean.SystemMessageInfo
import cn.huanyuan.sweetlove.databinding.AdapterSystemMessageItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/11
 * desc:
 */
class SystemMessageAdapter : BaseQuickAdapter<SystemMessageInfo, SystemMessageAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterSystemMessageItemBinding = AdapterSystemMessageItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: SystemMessageInfo?) {
        holder.binding.apply {
            this.item = item
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}