package cn.yanhu.imchat.custom.message.chatphoneview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.GsonUtils;
import com.hyphenate.chat.EMCustomMessageBody;

import java.util.Map;

import cn.yanhu.imchat.custom.message.BaseEaseChatRow;
import cn.yanhu.baselib.utils.CommonUtils;
import cn.yanhu.baselib.utils.DateUtils;
import cn.yanhu.commonres.config.ChatConstant;
import cn.yanhu.commonres.config.ImMessageParamsConfig;
import cn.yanhu.commonres.manager.AppCacheManager;
import cn.yanhu.imchat.R;
import cn.yanhu.imchat.bean.CallFinishRewardBean;

@SuppressLint("ViewConstructor")
public class ChatPhoneNew extends BaseEaseChatRow {

    private View inflate;
    private TextView contentView;
    private ImageView imageView;
    private TextView tvRewardDesc;
    private ImageView ivQuestion;

    public ChatPhoneNew(Context context, boolean isSender) {
        super(context, isSender);
    }

    @Override
    protected void onInflateView() {
        inflate = inflater.inflate(isSender ? R.layout.ease_row_sent_phone_new : R.layout.ease_row_received_phone_new, this);
    }

    @Override
    protected void onFindViewById() {
        super.onFindViewById();
        contentView = findViewById(R.id.tv_chatcontent);
        imageView = findViewById(R.id.tv_phone);
        tvRewardDesc = findViewById(R.id.tv_rewardDesc);
        ivQuestion = findViewById(R.id.iv_question);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onSetUpView() {
        super.onSetUpView();

        try {
            EMCustomMessageBody messageBody = (EMCustomMessageBody) message.getBody();
            Map<String, String> params = messageBody.getParams();
            String chatType;
            String stringAttribute = message.getStringAttribute(ChatConstant.CUSTOM_MSG, "");
            String rewardGoldSum1;
            String videoCardRewardSum = "0";
            if (TextUtils.isEmpty(stringAttribute)) {
                chatType = params.get("chatType");
                rewardGoldSum1 = params.get(ImMessageParamsConfig.KEY_REWARD_GOLD_SUM);
                if (params.containsKey(ImMessageParamsConfig.KEY_VIDEO_CARD_REWARD_GOLD_SUM)) {
                    videoCardRewardSum = params.get(ImMessageParamsConfig.KEY_VIDEO_CARD_REWARD_GOLD_SUM);
                }
                contentView.setText(params.get("content"));
            } else {
                CallFinishRewardBean callFinishRewardBean = GsonUtils.fromJson(stringAttribute, CallFinishRewardBean.class);
                chatType = callFinishRewardBean.getChatType();
                rewardGoldSum1 = callFinishRewardBean.getGoldNum();
                videoCardRewardSum = callFinishRewardBean.getVideoCardGoldNum();

                String desc = "";
                if (callFinishRewardBean.getIfBalanceNoEnough()) {
                    desc = "余额不足通话结束，";
                }
                contentView.setText((TextUtils.isEmpty(desc)?(chatType.equals("0") ? "语音通话" : "视频通话"):desc) + "时长 " + DateUtils.stringForTime(
                        callFinishRewardBean.getSeconds() * 1000L
                ));
            }

            if ("0".equals(chatType)) {
                imageView.setImageResource(isSender ? cn.yanhu.commonres.R.mipmap.ic_voice_line : cn.yanhu.commonres.R.mipmap.ic_voice_receive_line);
            } else {
                imageView.setImageResource(isSender ? cn.yanhu.commonres.R.mipmap.ic_video_line : cn.yanhu.commonres.R.mipmap.ic_video_receive_line);
            }
            LinearLayout coinLl = inflate.findViewById(R.id.coin_ll);
            TextView coinTxt = inflate.findViewById(R.id.coin_txt);
            ImageView ivCoin = inflate.findViewById(R.id.iv_coin);

            if (!TextUtils.isEmpty(rewardGoldSum1) && AppCacheManager.isWoman()) {
                int rewardGoldSum = Integer.parseInt(rewardGoldSum1);
                if (rewardGoldSum > 0) {
                    coinLl.setVisibility(VISIBLE);
                    if (CommonUtils.INSTANCE.compareZero(videoCardRewardSum)) {
                        coinTxt.setText("+" + rewardGoldSum + "金币(含视频卡" + videoCardRewardSum + ") 已获得");
                    } else {
                        coinTxt.setText("+" + rewardGoldSum + "金币 已获得");
                    }
                    ivCoin.setVisibility(View.VISIBLE);
                    ivQuestion.setVisibility(View.GONE);
                    tvRewardDesc.setText("");
                } else {
                    coinLl.setVisibility(GONE);
                }
            } else {
                coinLl.setVisibility(GONE);
            }


        } catch (Exception e) {
        }
    }
}
