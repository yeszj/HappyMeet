package cn.yanhu.imchat.custom.message.group.centerTipsView

import android.view.View
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.interfaces.MessageListItemClickListener
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder

class CenterTipsViewHolder(itemView: View, itemClickListener: MessageListItemClickListener?) :
    EaseChatRowViewHolder(itemView, itemClickListener) {
    override fun onBubbleClick(message: EMMessage) {
        super.onBubbleClick(message)
    }
}