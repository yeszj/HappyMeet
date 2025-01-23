package cn.huanyuan.sweetlove.ui.event.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.bean.CommonEventRankInfo
import cn.huanyuan.sweetlove.bean.CommonEventRankResponse
import cn.huanyuan.sweetlove.databinding.AdapterCommonEventRankItemBinding
import cn.yanhu.baselib.utils.GlideUtils
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2025/1/16
 * desc:
 */
class CommonEventRankAdapter : BaseQuickAdapter<CommonEventRankInfo,CommonEventRankAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterCommonEventRankItemBinding = AdapterCommonEventRankItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: CommonEventRankInfo?) {
        holder.binding.apply {
            rankInfo = item
            val topThreeIcon = commonEventRankInfo?.topThreeIcon

            tvNickName.setTextColor(Color.parseColor(item?.nickNameColor))
            tvDesc.setTextColor(Color.parseColor(item?.descColor))
            tvValue.setTextColor(Color.parseColor(item?.valueColor))

            when (position) {
                0 -> {
                    tvNum.visibility = View.GONE
                    vgRank.visibility = View.VISIBLE
                    GlideUtils.load(context,topThreeIcon?.one,ivRank,placeholderId = -1)
                }

                1 -> {
                    tvNum.visibility = View.GONE
                    vgRank.visibility = View.VISIBLE
                    GlideUtils.load(context,topThreeIcon?.two,ivRank,placeholderId = -1)
                }

                2 -> {
                    tvNum.visibility = View.GONE
                    vgRank.visibility = View.VISIBLE
                    GlideUtils.load(context,topThreeIcon?.three,ivRank,placeholderId = -1)
                }

                else -> {
                    tvNum.visibility = View.VISIBLE
                    vgRank.visibility = View.GONE
                    tvNum.text = (position + 1).toString()
                }
            }
            val itemBgImg = commonEventRankInfo?.itemBgImg?:return
            GlideUtils.loadAsDrawable(context,itemBgImg,object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    vgParent.background = resource
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
            executePendingBindings()

        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    private var commonEventRankInfo:CommonEventRankResponse?=null
    fun setEventRankInfo(commonRankRes: CommonEventRankResponse?) {
        commonEventRankInfo = commonRankRes
    }
}