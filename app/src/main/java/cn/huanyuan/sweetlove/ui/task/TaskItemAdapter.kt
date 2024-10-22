package cn.huanyuan.sweetlove.ui.task

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.bean.TaskInfo
import cn.huanyuan.sweetlove.databinding.AdapterTaskItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/15
 * desc:
 */
class TaskItemAdapter:BaseQuickAdapter<TaskInfo,TaskItemAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterTaskItemBinding = AdapterTaskItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: TaskInfo?) {
        holder.binding.apply {
            this.item = item
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}