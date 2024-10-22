package cn.yanhu.imchat.custom.message.group.inviteEnterGroup

import android.view.View
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.interfaces.MessageListItemClickListener
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder

class InviterEnterGroupViewHolder(
    itemView: View,
    itemClickListener: MessageListItemClickListener?
) : EaseChatRowViewHolder(itemView, itemClickListener) {
    override fun onBubbleClick(message: EMMessage) {
        super.onBubbleClick(message)
        // 实现相关点击方法
    }
}