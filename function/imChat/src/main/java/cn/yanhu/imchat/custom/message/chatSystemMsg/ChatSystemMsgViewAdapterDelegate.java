package cn.yanhu.imchat.custom.message.chatSystemMsg;

import android.view.View;
import android.view.ViewGroup;

import com.hyphenate.chat.EMCustomMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.delegate.EaseMessageAdapterDelegate;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;

import cn.yanhu.commonres.config.ChatConstant;


public class ChatSystemMsgViewAdapterDelegate extends EaseMessageAdapterDelegate<EMMessage, EaseChatRowViewHolder> {
    @Override
    public boolean isForViewType(EMMessage item, int position) {
        if (item.getType() == EMMessage.Type.CUSTOM) {
            EMCustomMessageBody messageBody = (EMCustomMessageBody) item.getBody();
            String event = messageBody.event();
            return event.equals(ChatConstant.MSG_COMMON_SYSTEM);
        }
        return false;
    }

    @Override
    protected EaseChatRow getEaseChatRow(ViewGroup parent, boolean isSender) {
        return new ChatSystemMsgView(parent.getContext(), isSender);
    }

    @Override
    protected EaseChatRowViewHolder createViewHolder(View view, MessageListItemClickListener itemClickListener) {
        return new ChatSystemMsgViewHolder(view, itemClickListener);
    }
}
