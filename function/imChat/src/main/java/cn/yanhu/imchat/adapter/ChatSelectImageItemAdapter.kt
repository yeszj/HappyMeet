package cn.yanhu.imchat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.imchat.bean.ChatFuncInfo
import cn.yanhu.imchat.databinding.AdapterSelectChatImageItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/5/23
 * desc:
 */
class ChatSelectImageItemAdapter : BaseQuickAdapter<ChatFuncInfo,ChatSelectImageItemAdapter.VH>() {
    class VH(
        parent: ViewGroup, val binding: AdapterSelectChatImageItemBinding = AdapterSelectChatImageItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: ChatFuncInfo?) {
        holder.binding.apply {
            item?.id = position
            this.funcInfo = item
            if (position==2){
                ViewUtils.setViewHeight(divider, CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_5))
            }else{
                ViewUtils.setViewHeight(divider,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_0_5))
            }
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}