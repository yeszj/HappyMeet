package cn.yanhu.agora.adapter.liveRoom

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.bean.AngleRankInfo
import cn.yanhu.agora.databinding.AdapterCrownedUserItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/17
 * desc:
 */
class CrownedUserRankAdapter : BaseQuickAdapter<AngleRankInfo, CrownedUserRankAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterCrownedUserItemBinding = AdapterCrownedUserItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: AngleRankInfo?) {
        holder.binding.apply {
            rankInfo = item
            when (position) {
                0 -> {
                    tvNum.visibility = View.INVISIBLE
                    vgRank.visibility = View.VISIBLE
                    ivRank.setImageResource(cn.yanhu.commonres.R.drawable.icon_rank1)
                }

                1 -> {
                    tvNum.visibility = View.INVISIBLE
                    vgRank.visibility = View.VISIBLE
                    ivRank.setImageResource(cn.yanhu.commonres.R.drawable.icon_rank2)
                }

                2 -> {
                    tvNum.visibility = View.INVISIBLE
                    vgRank.visibility = View.VISIBLE
                    ivRank.setImageResource(cn.yanhu.commonres.R.drawable.icon_rank3)
                }

                else -> {
                    tvNum.visibility = View.VISIBLE
                    vgRank.visibility = View.INVISIBLE
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