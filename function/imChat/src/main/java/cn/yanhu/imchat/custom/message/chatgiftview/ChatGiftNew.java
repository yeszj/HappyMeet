package cn.yanhu.imchat.custom.message.chatgiftview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.hyphenate.chat.EMCustomMessageBody;
import com.hyphenate.chat.EMMessage;

import java.util.Map;

import cn.yanhu.baselib.utils.GlideUtils;
import cn.yanhu.imchat.R;
import cn.yanhu.imchat.custom.message.BaseEaseChatRow;

@SuppressLint("ViewConstructor")
public class ChatGiftNew extends BaseEaseChatRow {

    public static final String TYPE_GIFT_ROSE = "0";
    public static final String TYPE_GIFT_COIN = "1";
    private TextView contentView;
    private AppCompatImageView imageView;

    public ChatGiftNew(Context context, boolean isSender) {
        super(context, isSender);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(isSender ? R.layout.ease_row_sent_gift_new : R.layout.ease_row_received_gift_new, this);
    }

    @Override
    protected void onFindViewById() {
        super.onFindViewById();
        contentView = findViewById(R.id.tv_chatcontent);
        imageView = findViewById(R.id.tv_gift);
    }

    @Override
    protected void onSetUpView() {
        super.onSetUpView();
        try {
            EMCustomMessageBody messageBody = (EMCustomMessageBody) message.getBody();
            Map<String, String> params = messageBody.getParams();
            String sendDesc = "赠送";
//            if (params.containsKey("giftCoin")){
//                if (TYPE_GIFT_COIN.equals(params.get("giftCoin"))){
//                    sendDesc = "礼物币赠送";
//                }
//            }
            if (message.getChatType() == EMMessage.ChatType.GroupChat) {
                String toNickName = params.get("toNickName");
                if (TextUtils.isEmpty(toNickName)) {
                    toNickName = "";
                }
                contentView.setText(String.format("%s %s%s",sendDesc, toNickName, params.get("giftName")));
            } else {
                contentView.setText("0".equals(params.get("giftType")) ? sendDesc+" " + params.get("giftName") : "解锁“" + params.get("giftName") + "”礼盒，赠送" + params.get("gName"));
            }
            GlideUtils.loadImage(context, params.get("giftIcon"),imageView);
        } catch (Exception e) {
        }
    }
}
