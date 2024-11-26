package cn.yanhu.agora.adapter.liveRoom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.bean.AngleRankInfo
import cn.yanhu.agora.databinding.AdapterAngleRoomRankItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/17
 * desc:
 */
class AngleRankAdapter : BaseQuickAdapter<AngleRankInfo, AngleRankAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterAngleRoomRankItemBinding = AdapterAngleRoomRankItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: AngleRankInfo?) {
        holder.binding.apply {
            rankInfo = item?:return
            if (!TextUtils.isEmpty(item.angelUnlockDesc)){
                clUser.setBackgroundColor(Color.parseColor("#1AF8459B"))
                tvDesc.text = item.angelUnlockDesc
            }else{
                clUser.setBackgroundColor(Color.parseColor("#FFFFFF"))
                tvDesc.text = "距离解锁本场天使还差${item.diffRoseNum}玫瑰"
            }
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