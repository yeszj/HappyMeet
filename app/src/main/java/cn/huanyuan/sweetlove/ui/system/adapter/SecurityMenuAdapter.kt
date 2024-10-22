package cn.huanyuan.sweetlove.ui.system.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterSecurityMenuItemBinding
import cn.yanhu.commonres.bean.MineMenuBean
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2023/12/20
 * desc:
 */
class SecurityMenuAdapter :
    BaseQuickAdapter<MineMenuBean, SecurityMenuAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterSecurityMenuItemBinding = AdapterSecurityMenuItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: MineMenuBean?) {
        holder.binding.apply {
            menuInfo = item
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}