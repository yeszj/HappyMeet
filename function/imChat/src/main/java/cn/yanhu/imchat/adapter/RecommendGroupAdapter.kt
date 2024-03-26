package cn.yanhu.imchat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.imchat.bean.GroupBean
import cn.yanhu.imchat.databinding.AdapterRecommendGroupItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/2/27
 * desc:
 */
class RecommendGroupAdapter: BaseQuickAdapter<GroupBean, RecommendGroupAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterRecommendGroupItemBinding = AdapterRecommendGroupItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: GroupBean?) {
        holder.binding.apply {
            groupInfo = item
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}