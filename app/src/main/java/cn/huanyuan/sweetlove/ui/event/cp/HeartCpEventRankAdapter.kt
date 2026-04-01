package cn.huanyuan.sweetlove.ui.event.cp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.bean.CommonEventRankInfo
import cn.huanyuan.sweetlove.bean.CommonEventRankResponse
import cn.huanyuan.sweetlove.databinding.AdapterCommonEventRankItemBinding
import cn.yanhu.baselib.utils.GlideUtils
import com.chad.library.adapter4.BaseQuickAdapter
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.HeartCpEventRankInfo
import cn.huanyuan.sweetlove.bean.HeartCpEventRankResponse
import cn.huanyuan.sweetlove.databinding.AdapterHeartCpEventRankItemBinding

/**
 * @author: zhengjun
 * created: 2025/1/16
 * desc:
 */
class HeartCpEventRankAdapter : BaseQuickAdapter<HeartCpEventRankInfo,HeartCpEventRankAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterHeartCpEventRankItemBinding = AdapterHeartCpEventRankItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: HeartCpEventRankInfo?) {
        holder.binding.apply {
            rankInfo = item
            val topThreeIcon = commonEventRankInfo?.topThreeIcon

//            tvNickName.setTextColor(Color.parseColor(item?.nickNameColor))
//            tvDesc.setTextColor(Color.parseColor(item?.descColor))
//            tvValue.setTextColor(Color.parseColor(item?.valueColor))

            when (position) {
                0 -> {
                    tvNum.visibility = View.GONE
                    vgRank.visibility = View.VISIBLE
                    GlideUtils.load(context,topThreeIcon?.one,ivRank,placeholderId = -1)
                    vgParent.setBackgroundResource(R.drawable.bg_heart_cp_rank_1)
                    ivRankTop.setImageResource(R.mipmap.heart_cp_rank_top_1)
                    ivRankTop.visibility = View.VISIBLE

                }

                1 -> {
                    tvNum.visibility = View.GONE
                    vgRank.visibility = View.VISIBLE
                    GlideUtils.load(context,topThreeIcon?.two,ivRank,placeholderId = -1)
                    vgParent.setBackgroundResource(R.drawable.bg_heart_cp_rank_2)
                    ivRankTop.setImageResource(R.mipmap.heart_cp_rank_top_2)
                    ivRankTop.visibility = View.VISIBLE
                }

                2 -> {
                    tvNum.visibility = View.GONE
                    vgRank.visibility = View.VISIBLE
                    GlideUtils.load(context,topThreeIcon?.three,ivRank,placeholderId = -1)
                    vgParent.setBackgroundResource(R.drawable.bg_heart_cp_rank_3)
                    ivRankTop.setImageResource(R.mipmap.heart_cp_rank_top_3)
                    ivRankTop.visibility = View.VISIBLE
                }

                else -> {
                    ivRankTop.visibility = View.INVISIBLE
                    tvNum.visibility = View.VISIBLE
                    vgRank.visibility = View.GONE
                    tvNum.text = (position + 1).toString()
                    vgParent.setBackgroundResource(R.drawable.bg_heart_cp_rank_4)
                }
            }
//            val itemBgImg = commonEventRankInfo?.itemBgImg?:return
//            GlideUtils.loadAsDrawable(context,itemBgImg){
//                vgParent.background = it
//            }
            executePendingBindings()

        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    private var commonEventRankInfo:HeartCpEventRankResponse?=null
    fun setEventRankInfo(commonRankRes: HeartCpEventRankResponse?) {
        commonEventRankInfo = commonRankRes
    }
}