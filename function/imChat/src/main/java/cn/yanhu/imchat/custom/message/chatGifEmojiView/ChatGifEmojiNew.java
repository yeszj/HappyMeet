package cn.yanhu.imchat.custom.message.chatGifEmojiView;

import android.annotation.SuppressLint;
import android.content.Context;

import cn.yanhu.commonres.utils.GifLoadUtils;
import cn.yanhu.imchat.R;
import cn.yanhu.imchat.custom.message.BaseEaseChatRow;
import cn.yanhu.commonres.config.ChatConstant;
import pl.droidsonroids.gif.GifImageView;

@SuppressLint("ViewConstructor")
public class ChatGifEmojiNew extends BaseEaseChatRow {

    private GifImageView gifImage;

    public ChatGifEmojiNew(Context context, boolean isSender) {
        super(context, isSender);

    }

    @Override
    protected void onInflateView() {
        inflater.inflate(isSender ? R.layout.ease_row_sent_custom_gif_emoji : R.layout.ease_row_received_custom_gif_emoji, this);
    }

    @Override
    protected void onFindViewById() {
        super.onFindViewById();
        gifImage = findViewById(R.id.gifImage);
    }

    @Override
    protected void onSetUpView() {
        try {
            super.onSetUpView();
            String stringAttribute = message.getStringAttribute(ChatConstant.CUSTOM_MSG, "");
            GifLoadUtils.INSTANCE.loadGif(stringAttribute,gifImage,0);
        } catch (Exception e) {
        }
    }
}
