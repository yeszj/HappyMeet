package cn.yanhu.agora.adapter.statistic

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.bean.StatisticInfo
import cn.yanhu.agora.databinding.AdapterLiveStatisticTotalItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/12/23
 * desc:
 */
class LiveStatisticsTotalAdapter :
    BaseQuickAdapter<StatisticInfo, LiveStatisticsTotalAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterLiveStatisticTotalItemBinding = AdapterLiveStatisticTotalItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: StatisticInfo?) {
        holder.binding.apply {
            this.position = position
            this.statisticInfo = item
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
       return VH(parent)
    }
}