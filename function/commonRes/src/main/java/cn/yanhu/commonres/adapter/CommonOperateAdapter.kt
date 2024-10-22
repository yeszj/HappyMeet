package cn.yanhu.commonres.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.commonres.bean.OperateInfo
import cn.yanhu.commonres.databinding.AdapterCommonOperateItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/10
 * desc:
 */
class CommonOperateAdapter : BaseQuickAdapter<OperateInfo, CommonOperateAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterCommonOperateItemBinding = AdapterCommonOperateItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: OperateInfo?) {
        holder.binding.apply {
            tvValue.text = item?.value
            tvValue.setTextColor(CommonUtils.getColor(item!!.colorResId))
            if (position == itemCount - 1) {
                divider.visibility = View.INVISIBLE
            } else {
                divider.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}