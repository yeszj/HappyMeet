package cn.yanhu.imchat.custom.message.inviteSendGiftView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.GsonUtils;
import com.bumptech.glide.Glide;
import com.jeremyliao.liveeventbus.LiveEventBus;

import cn.yanhu.commonres.config.ChatConstant;
import cn.yanhu.imchat.R;
import cn.yanhu.imchat.custom.message.BaseEaseChatRow;
import cn.yanhu.imchat.bean.AddFriendGiftInfo;

@SuppressLint("ViewConstructor")
public class InviteSendGiftView extends BaseEaseChatRow {


    private ImageView ivGiftIcon;
    private TextView tvTips;
    private TextView tvGiftName;

    private TextView tvSend;

    public InviteSendGiftView(Context context, boolean isSender) {
        super(context, isSender);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(R.layout.ease_invite_send_gift_layout, this);
    }

    @Override
    protected void onFindViewById() {
        ivGiftIcon = findViewById(R.id.iv_giftIcon);
        tvTips = findViewById(R.id.tv_tips);
        tvGiftName = findViewById(R.id.tv_giftName);
        tvSend = findViewById(R.id.tv_send);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onSetUpView() {
        super.onSetUpView();
        try {
            String stringAttribute = message.getStringAttribute(ChatConstant.CUSTOM_DATA, "");
            AddFriendGiftInfo addFriendGiftInfo = GsonUtils.fromJson(stringAttribute, AddFriendGiftInfo.class);
            tvGiftName.setText("“" + addFriendGiftInfo.getGiftName() + "”");
            Glide.with(context).load(addFriendGiftInfo.getGiftIcon()).into(ivGiftIcon);
            if (isSender()) {
                tvTips.setText("已邀对方赠送");
                tvSend.setVisibility(View.GONE);
            } else {
                tvTips.setText("对方邀你赠送");
                tvSend.setVisibility(View.VISIBLE);
                boolean hasSend = message.getBooleanAttribute(ChatConstant.HAS_SEND_GIFT, false);
                if (hasSend) {
                    tvSend.setText("已赠送");
                    tvSend.setAlpha(0.5f);
                    tvSend.setEnabled(false);
                } else {
                    tvSend.setEnabled(true);
                    tvSend.setAlpha(1f);
                    tvSend.setText("立即赠送");
                    tvSend.setOnClickListener(v -> LiveEventBus.get(ChatConstant.SHOW_SEND_GIFT).post(message.getMsgId()));
                }
            }
        } catch (Exception e) {
        }
    }
}
