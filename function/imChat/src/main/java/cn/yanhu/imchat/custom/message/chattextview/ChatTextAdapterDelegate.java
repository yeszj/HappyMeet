package cn.yanhu.imchat.custom.message.chattextview;

import android.view.View;
import android.view.ViewGroup;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.delegate.EaseMessageAdapterDelegate;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.model.styles.EaseMessageListItemStyle;
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;

/**
 * 文本代理类
 */
public class ChatTextAdapterDelegate extends EaseMessageAdapterDelegate<EMMessage, EaseChatRowViewHolder> {

    public ChatTextAdapterDelegate() {
    }

    public ChatTextAdapterDelegate(MessageListItemClickListener itemClickListener, EaseMessageListItemStyle itemStyle) {
        super(itemClickListener, itemStyle);
    }

    @Override
    public boolean isForViewType(EMMessage item, int position) {
//        if(item.getType() == EMMessage.Type.CUSTOM){
//            EMCustomMessageBody messageBody = (EMCustomMessageBody) item.getBody();
//            String event = messageBody.event();
//            if(event.equals(ChatConstant.MSG_GIFT)){
//                return true
//
//            }
//        }
        return item.getType() == EMMessage.Type.TXT;
    }

    @Override
    protected EaseChatRow getEaseChatRow(ViewGroup parent, boolean isSender) {
        return new ChatTextNew(parent.getContext(), isSender);
    }

    @Override
    public EaseChatRowViewHolder createViewHolder(View view, MessageListItemClickListener itemClickListener) {
        return new ChatTextViewHolder(view, itemClickListener);
    }

}
