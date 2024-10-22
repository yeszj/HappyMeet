package cn.yanhu.imchat.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.commonres.bean.ChatPriceItemInfo
import cn.yanhu.imchat.databinding.AdapterChatPriceSetItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/11
 * desc:
 */
class ChatPriceSetAdapter : BaseQuickAdapter<ChatPriceItemInfo, ChatPriceSetAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterChatPriceSetItemBinding = AdapterChatPriceSetItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: ChatPriceItemInfo?) {
        holder.binding.apply {
            if (item==null){
                return
            }
            val list = item.list
            val indexOfLast = list.indexOfLast {
                it.selected
            }
            if (indexOfLast>=0){
                val get = list[indexOfLast]
                tvPrice.text = get.desc
            }
            when (item.type) {
                "txt" -> {
                    tvValue.text = "文字价格"
                }
                "voice" -> {
                    tvValue.text = "语音价格"
                }
                "video" -> {
                    tvValue.text = "视频价格"
                }
                else -> {
                    tvValue.text = "价格设置-${item.type}"
                }
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}