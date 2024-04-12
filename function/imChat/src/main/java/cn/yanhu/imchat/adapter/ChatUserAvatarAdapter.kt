package cn.yanhu.imchat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.imchat.databinding.AdapterChatTopAvatarItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/5
 * desc:
 */
class ChatUserAvatarAdapter : BaseQuickAdapter<String, ChatUserAvatarAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterChatTopAvatarItemBinding = AdapterChatTopAvatarItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: String?) {
        holder.binding.apply {
            GlideUtils.load(context, item, ivAvatar)
            executePendingBindings()
        }
    }
    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}