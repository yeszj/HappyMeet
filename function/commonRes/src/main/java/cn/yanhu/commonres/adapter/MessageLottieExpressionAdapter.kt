package cn.yanhu.commonres.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.commonres.bean.ExpressionInfo
import cn.yanhu.commonres.databinding.AdapterLottieMessageExpressionBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/4/2
 * desc:
 */
class MessageLottieExpressionAdapter : BaseQuickAdapter<ExpressionInfo,MessageLottieExpressionAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterLottieMessageExpressionBinding = AdapterLottieMessageExpressionBinding.inflate(
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