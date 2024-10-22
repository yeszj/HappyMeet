package cn.yanhu.agora.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.bean.ToolBean
import cn.yanhu.agora.databinding.ItemToolBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/17
 * desc:
 */
class ToolsAdapter : BaseQuickAdapter<ToolBean, ToolsAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: ItemToolBinding = ItemToolBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: ToolBean?) {
        holder.binding.apply {
            this.item = item
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}