package cn.yanhu.agora.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.bean.BeautyBean
import cn.yanhu.agora.databinding.AdapterBeautySetItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/4/8
 * desc:
 */
class BeautyFaceSetAdapter : BaseQuickAdapter<BeautyBean, BeautyFaceSetAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterBeautySetItemBinding = AdapterBeautySetItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: BeautyBean?) {
        holder.binding.apply {
            beautyInfo = item
            isCheck = selectPosition == position
            if (selectPosition == position && TextUtils.isEmpty(item?.checkIcon)) {
                ivImage.setBorderWidth(com.zj.dimens.R.dimen.dp_2)
            } else {
                ivImage.borderWidth = 0f
            }
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    private var selectPosition: Int = -1
    fun setSelectPosition(position: Int) {
        val oldPosition = selectPosition
        if (selectPosition != position) {
            selectPosition = position
            notifyItemChanged(oldPosition)
            if (selectPosition != -1) {
                notifyItemChanged(selectPosition)
            }
        }

    }
}