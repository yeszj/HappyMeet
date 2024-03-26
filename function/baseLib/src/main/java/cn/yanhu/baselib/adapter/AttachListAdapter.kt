package cn.yanhu.baselib.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.baselib.bean.AttachParamsInfo
import cn.yanhu.baselib.databinding.LayoutCommonAttachItemBinding
import cn.yanhu.baselib.utils.CommonUtils
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2023/7/26
 * desc:
 */
class AttachListAdapter :
    BaseQuickAdapter<AttachParamsInfo, AttachListAdapter.VH>() {

    class VH(
        parent: ViewGroup,
        val binding: LayoutCommonAttachItemBinding = LayoutCommonAttachItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: AttachParamsInfo?) {
        holder.binding.apply {
            item?.apply {
                if (item.iconId==-1){
                    ivImage.visibility = View.GONE
                }else{
                    ivImage.visibility = View.VISIBLE
                    ivImage.setImageResource(item.iconId)
                }
                tvText.text = item.value
                tvText.setTextColor(CommonUtils.getColor(item.itemColor))
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }


}