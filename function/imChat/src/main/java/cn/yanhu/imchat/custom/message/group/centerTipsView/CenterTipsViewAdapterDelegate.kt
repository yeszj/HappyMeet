package cn.yanhu.imchat.custom.message.group.centerTipsView

import android.view.View
import android.view.ViewGroup

import cn.yanhu.commonres.config.ChatConstant
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.delegate.EaseMessageAdapterDelegate
import com.hyphenate.easeui.interfaces.MessageListItemClickListener
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder
import com.hyphenate.easeui.widget.chatrow.EaseChatRow

class CenterTipsViewAdapterDelegate :
    EaseMessageAdapterDelegate<EMMessage?, EaseChatRowViewHolder>() {
    override fun isForViewType(item: EMMessage?, position: Int): Boolean {
        if (item?.type == EMMessage.Type.CUSTOM) {
            val messageBody = item.body as EMCustomMessageBody
            val event = messageBody.event()
            return event == ChatConstant.MSG_GROUP_CENTER_TIPS
        }
        return false
    }

    override fun getEaseChatRow(parent: ViewGroup, isSender: Boolean): EaseChatRow {
        return CenterTipsGroupView(parent.context, isSender)
    }

    override fun createViewHolder(
        view: View,
        itemClickListener: MessageListItemClickListener
    ): EaseChatRowViewHolder {
        return CenterTipsViewHolder(view, itemClickListener)
    }
}