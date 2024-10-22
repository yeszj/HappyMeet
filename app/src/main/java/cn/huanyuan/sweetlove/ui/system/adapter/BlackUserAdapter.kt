package cn.huanyuan.sweetlove.ui.system.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterBlackUserItemBinding
import cn.yanhu.commonres.bean.BaseUserInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/11
 * desc:
 */
class BlackUserAdapter : BaseQuickAdapter<BaseUserInfo, BlackUserAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterBlackUserItemBinding = AdapterBlackUserItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: BaseUserInfo?) {
        holder.binding.apply {
            userinfo = item
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}