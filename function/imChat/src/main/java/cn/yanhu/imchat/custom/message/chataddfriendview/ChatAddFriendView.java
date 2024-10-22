package cn.yanhu.imchat.custom.message.chataddfriendview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.hyphenate.chat.EMCustomMessageBody;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.Map;

import cn.yanhu.imchat.custom.message.BaseEaseChatRow;
import cn.yanhu.imchat.R;

public class ChatAddFriendView extends BaseEaseChatRow {

    private LinearLayout applyFriendLl;
    private ImageView icon;
    private TextView easeAgree;
    private TextView hintView;

    public ChatAddFriendView(Context context, boolean isSender) {
        super(context, isSender);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(R.layout.ease_add_friend_layout, this);
    }

    @Override
    protected void onFindViewById() {
        hintView = (TextView) findViewById(R.id.tv_alert);
        easeAgree = (TextView) findViewById(R.id.ease_agree);
        applyFriendLl = (LinearLayout) findViewById(R.id.apply_friend_ll);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onSetUpView() {
        super.onSetUpView();
        try {
            EMCustomMessageBody messageBody = (EMCustomMessageBody) message.getBody();
            Map<String, String> params = messageBody.getParams();
            String isApplySuccess = params.get("isApplySuccess");
            String fromNickName = params.get("fromNickName");

            if ("0".equals(isApplySuccess)) {
                applyFriendLl.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.shape_item_un_select));
                easeAgree.setVisibility(VISIBLE);
                hintView.setText(fromNickName + "申请加你为好友");
                easeAgree.setOnClickListener(v -> LiveEventBus.get("agreeFriend").post(message.getMsgId()));
            } else {
                applyFriendLl.setBackground(ContextCompat.getDrawable(getContext(),cn.yanhu.commonres.R.drawable.shape_transparent));
                easeAgree.setVisibility(GONE);
                String targetNickName = params.get("targetNickName");
                String showName ;
                if (isSender()){
                    showName = targetNickName;
                }else {
                    showName = fromNickName;
                }
                hintView.setText("你与" + showName + "已成为好友，开始聊天吧～");
            }

        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }
}
