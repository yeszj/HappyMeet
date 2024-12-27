package cn.yanhu.agora.adapter.statistic

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.bean.LiveIncomeDetailInfo
import cn.yanhu.agora.databinding.AdapterLiveIncomeDetailItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/12/23
 * desc:
 */
class LiveIncomeDetailAdapter :
    BaseQuickAdapter<LiveIncomeDetailInfo.IncomeInfo, LiveIncomeDetailAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterLiveIncomeDetailItemBinding = AdapterLiveIncomeDetailItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: LiveIncomeDetailInfo.IncomeInfo?
    ) {
        holder.binding.apply {
            incomeInfo = item
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}