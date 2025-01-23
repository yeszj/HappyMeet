package cn.huanyuan.sweetlove.ui.event.newYear

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.bean.NewYearRankInfo
import cn.huanyuan.sweetlove.databinding.AdapterNewYearRankItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2025/1/16
 * desc:
 */
class NewYearRankAdapter : BaseQuickAdapter<NewYearRankInfo,NewYearRankAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterNewYearRankItemBinding = AdapterNewYearRankItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: NewYearRankInfo?) {
        holder.binding.apply {
            rankInfo = item
            when (position) {
                0 -> {
                    tvNum.visibility = View.GONE
                    vgRank.visibility = View.VISIBLE
                    ivRank.setImageResource(cn.yanhu.commonres.R.drawable.icon_new_year_rank1)
                }

                1 -> {
                    tvNum.visibility = View.GONE
                    vgRank.visibility = View.VISIBLE
                    ivRank.setImageResource(cn.yanhu.commonres.R.drawable.icon_new_year_rank2)
                }

                2 -> {
                    tvNum.visibility = View.GONE
                    vgRank.visibility = View.VISIBLE
                    ivRank.setImageResource(cn.yanhu.commonres.R.drawable.icon_new_year_rank3)
                }

                else -> {
                    tvNum.visibility = View.VISIBLE
                    vgRank.visibility = View.GONE
                    tvNum.text = (position + 1).toString()
                }
            }
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}