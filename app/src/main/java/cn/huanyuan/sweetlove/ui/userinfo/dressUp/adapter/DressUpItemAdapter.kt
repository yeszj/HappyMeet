package cn.huanyuan.sweetlove.ui.userinfo.dressUp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterDressUpItemBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.commonres.bean.DressUpInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:
 */
class DressUpItemAdapter : BaseQuickAdapter<DressUpInfo, DressUpItemAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterDressUpItemBinding = AdapterDressUpItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: DressUpInfo?) {
        holder.binding.apply {
            itemInfo = item
//            val bubbleInfo = item?.bubble
//            if (bubbleInfo != null) {
//                tvChatStyle.visibility = View.VISIBLE
//                tvPopTxt.visibility = View.VISIBLE
//                ivCover.visibility = View.INVISIBLE
//                val content = bubbleInfo.content
//                val gradientList = content.gradientList
//                val startColor = gradientList[0].color
//                var centerColor = ""
//                var endColor = startColor
//                if (gradientList.size == 2) {
//                    endColor = gradientList[1].color
//                } else if (gradientList.size >= 3) {
//                    centerColor = gradientList[1].color
//                    endColor = gradientList[2].color
//                }
//                if (!CommonUtils.isEmpty(centerColor)) {
//                    tvChatStyle.setGradientColor(
//                        bubbleInfo.content.angle,
//                        startColor.toColorInt(),
//                        centerColor.toColorInt(),
//                        endColor.toColorInt()
//                    )
//                } else {
//                    tvChatStyle.setGradientColor(
//                        bubbleInfo.content.angle,
//                        startColor.toColorInt(),
//                        endColor.toColorInt()
//                    )
//                }
//            } else {
//                ivCover.visibility = View.VISIBLE
//                tvChatStyle.visibility = View.INVISIBLE
//                tvPopTxt.visibility = View.INVISIBLE
//            }
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}