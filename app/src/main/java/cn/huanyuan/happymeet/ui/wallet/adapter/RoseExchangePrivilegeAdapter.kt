package cn.huanyuan.happymeet.ui.wallet.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.databinding.AdapterRosePrivilegeItemBinding
import cn.yanhu.commonres.bean.MineMenuBean
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/8
 * desc:
 */
class RoseExchangePrivilegeAdapter : BaseQuickAdapter<MineMenuBean, RoseExchangePrivilegeAdapter.VH>() {

    class VH(
        parent: ViewGroup,
        val binding: AdapterRosePrivilegeItemBinding = AdapterRosePrivilegeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: MineMenuBean?) {
        holder.binding.apply {
            itemInfo = item
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

}