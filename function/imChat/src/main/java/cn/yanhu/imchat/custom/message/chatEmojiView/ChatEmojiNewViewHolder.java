package cn.yanhu.imchat.custom.message.chatEmojiView;

import android.view.View;

import androidx.annotation.NonNull;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.viewholder.EaseChatRowViewHolder;

public class ChatEmojiNewViewHolder extends EaseChatRowViewHolder {

    public ChatEmojiNewViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
        super(itemView, itemClickListener);
    }

    @Override
    public void onBubbleClick(EMMessage message) {
        super.onBubbleClick(message);
        // 实现相关点击方法
    }
}