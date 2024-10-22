package cn.yanhu.imchat.custom.message.chatEmojiView;

import android.content.Context;
import android.view.ViewGroup;

import cn.yanhu.imchat.custom.message.BaseEaseChatRow;
import cn.yanhu.imchat.R;
import cn.yanhu.imchat.custom.chat.EaseCommonUtils;

public class ChatEmojiNew extends BaseEaseChatRow {

    private ViewGroup vgContent;

    public ChatEmojiNew(Context context, boolean isSender) {
        super(context, isSender);

    }

    @Override
    protected void onInflateView() {
        inflater.inflate(isSender ? R.layout.ease_row_sent_custom_emoji : R.layout.ease_row_received_custom_emoji, this);
    }

    @Override
    protected void onFindViewById() {
        super.onFindViewById();
        vgContent = findViewById(R.id.vg_content);
    }

    @Override
    protected void onSetUpView() {
        try {
            super.onSetUpView();
            EaseCommonUtils.showCustomEmojiView(context,message,vgContent,false,false);
        } catch (Exception e) {
        }
    }
}
