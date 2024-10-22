package cn.yanhu.imchat.custom.message.chatGifEmojiView;

import android.view.View;

import androidx.annotation.NonNull;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder;

public class ChatGifEmojiNewViewHolder extends EaseChatRowViewHolder {

    public ChatGifEmojiNewViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
        super(itemView, itemClickListener);
    }

    @Override
    public void onBubbleClick(EMMessage message) {
        super.onBubbleClick(message);
        // 实现相关点击方法
    }
}
