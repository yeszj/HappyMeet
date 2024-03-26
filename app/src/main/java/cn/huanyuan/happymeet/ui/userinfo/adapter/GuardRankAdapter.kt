package cn.huanyuan.happymeet.ui.userinfo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.bean.RankInfo
import cn.huanyuan.happymeet.databinding.AdapterRankListItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/18
 * desc:
 */
class GuardRankAdapter : BaseQuickAdapter<RankInfo, GuardRankAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterRankListItemBinding = AdapterRankListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: RankInfo?) {
        holder.binding.apply {
            rankInfo = item
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}