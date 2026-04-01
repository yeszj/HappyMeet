package cn.huanyuan.sweetlove.ui.invite.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterInviteUserTotalItemBinding
import cn.yanhu.agora.bean.StatisticInfo
import cn.yanhu.agora.databinding.AdapterLiveStatisticTotalItemBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.TextFontStyleUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.zj.dimens.R

/**
 * @author: zhengjun
 * created: 2024/12/23
 * desc:
 */
class InviteUserTotalAdapter :
    BaseQuickAdapter<StatisticInfo, InviteUserTotalAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterInviteUserTotalItemBinding = AdapterInviteUserTotalItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: StatisticInfo?) {
        holder.binding.apply {
            this.statisticInfo = item?:return
            if (item.title.contains("时间")){
                val typeface = cn.yanhu.baselib.utils.TypefaceUtils.getTypeface(400, context)
                tvValue.typeface = typeface
                tvValue.setTextColor(CommonUtils.getColor(cn.yanhu.commonres.R.color.color_888888))
                tvValue.textSize = CommonUtils.getSpByDimen(R.dimen.sp_12).toFloat()
            }else{
                val typeface = cn.yanhu.baselib.utils.TypefaceUtils.getTypeface(500, context)
                tvValue.typeface = typeface
                tvValue.setTextColor(CommonUtils.getColor(cn.yanhu.commonres.R.color.cl_common))
                tvValue.textSize = CommonUtils.getSpByDimen(R.dimen.sp_16).toFloat()
            }
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
       return VH(parent)
    }
}