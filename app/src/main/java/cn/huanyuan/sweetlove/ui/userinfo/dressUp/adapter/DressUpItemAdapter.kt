package cn.huanyuan.sweetlove.ui.userinfo.dressUp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterDressUpItemBinding
import cn.yanhu.commonres.bean.DressUpInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:
 */
class DressUpItemAdapter : BaseQuickAdapter<DressUpInfo, DressUpItemAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterDressUpItemBinding = AdapterDressUpItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: DressUpInfo?) {
        holder.binding.apply {
            itemInfo = item
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}