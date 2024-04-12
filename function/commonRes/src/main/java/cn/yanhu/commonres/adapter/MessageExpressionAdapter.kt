package cn.yanhu.commonres.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.commonres.bean.ExpressionInfo
import cn.yanhu.commonres.databinding.AdapterMessageExpressionBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/4/2
 * desc:
 */
class MessageExpressionAdapter : BaseQuickAdapter<ExpressionInfo,MessageExpressionAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterMessageExpressionBinding = AdapterMessageExpressionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: ExpressionInfo?) {
        holder.binding.apply {
            tvValue.text = item?.name
            animExpression.setAnimationFromUrl(item?.url, item?.name)
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}