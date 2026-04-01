package cn.yanhu.imchat.custom.message.chatvoiceview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.easeui.R;
import com.hyphenate.util.EMLog;

import cn.yanhu.imchat.custom.chat.EaseChatRowVoicePlayer;
import cn.yanhu.imchat.custom.message.BaseEaseChatRowFile;


@SuppressLint("ViewConstructor")
public class ChatRowVoice extends BaseEaseChatRowFile {
    private static final String TAG = ChatRowVoice.class.getSimpleName();
    private LottieAnimationView voiceImageView;
    private TextView voiceLengthView;
    private ImageView readStatusView;

    private View inflate;

    public ChatRowVoice(Context context, boolean isSender) {
        super(context, isSender);
    }

    public ChatRowVoice(Context context, EMMessage message, int position, Object adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflate = inflater.inflate(!showSenderType ? R.layout.ease_row_received_voice
                : R.layout.ease_row_sent_voice, this);
    }

    @Override
    protected void onFindViewById() {
        super.onFindViewById();
        voiceImageView = findViewById(R.id.iv_voice);
        voiceLengthView = findViewById(R.id.tv_length);
        readStatusView = findViewById(R.id.iv_unread_voice);


    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onSetUpView() {
        super.onSetUpView();

        EMVoiceMessageBody voiceBody = (EMVoiceMessageBody) message.getBody();
        int len = voiceBody.getLength();
        if (len > 0) {
            voiceLengthView.setText(voiceBody.getLength() + "\"");
            voiceLengthView.setVisibility(View.VISIBLE);
        } else {
            voiceLengthView.setVisibility(View.INVISIBLE);
        }


        if (message.direct() == EMMessage.Direct.RECEIVE) {
            voiceImageView.setImageResource(cn.yanhu.imchat.R.drawable.icon_voice_from3);
            setColorValue(hasSetReceiveDrawable());
            if (readStatusView != null) {
                if (message.isListened()) {
                    // hide the unread icon
                    readStatusView.setVisibility(View.INVISIBLE);
                } else {
                    readStatusView.setVisibility(View.INVISIBLE);
                }
            }

            EMLog.d(TAG, "it is receive msg");
        } else {
            voiceImageView.setImageResource(cn.yanhu.imchat.R.drawable.icon_to_voice);
            readStatusView.setVisibility(View.INVISIBLE);
            setColorValue(hasSetSendDrawable());
        }

        EaseChatRowVoicePlayer voicePlayer = EaseChatRowVoicePlayer.getInstance(getContext());
        if (voicePlayer.isPlaying() && message.getMsgId().equals(voicePlayer.getCurrentPlayingId())) {
            startVoicePlayAnimation();
        }
    }

    private void setColorValue(boolean isSet) {
        if (isSet){
            voiceLengthView.setTextColor(ContextCompat.getColor(context,R.color.white));
            voiceImageView.setImageTintList(ContextCompat.getColorStateList(context,R.color.white));
        }else {
            voiceLengthView.setTextColor(ContextCompat.getColor(context,R.color.colorIm));
            voiceImageView.setImageTintList(ContextCompat.getColorStateList(context,R.color.colorIm));
        }
    }

    @Override
    protected void onViewUpdate(EMMessage msg) {
        super.onViewUpdate(msg);

        if (message.direct() == EMMessage.Direct.SEND) {
            return;
        }

        EMVoiceMessageBody voiceBody = (EMVoiceMessageBody) msg.getBody();
        if (voiceBody.downloadStatus() == EMFileMessageBody.EMDownloadStatus.DOWNLOADING ||
                voiceBody.downloadStatus() == EMFileMessageBody.EMDownloadStatus.PENDING) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressLint("ResourceType")
    public void startVoicePlayAnimation() {
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            if (hasSetReceiveDrawable()){
                voiceImageView.setAnimation("voice_msg_play_white.json");
            }else {
                voiceImageView.setAnimation("voice_msg_play.json");
            }
        } else {
            if (hasSetSendDrawable()){
                voiceImageView.setAnimation("voice_msg_play_reverse_white.json");
            }else {
                voiceImageView.setAnimation("voice_msg_play_reverse.json");
            }
        }
        voiceImageView.playAnimation();

        // Hide the voice item not listened status view.
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            readStatusView.setVisibility(View.INVISIBLE);
        }
    }

    public void stopVoicePlayAnimation() {
        if (voiceImageView != null) {
            voiceImageView.pauseAnimation();
        }
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            voiceImageView.setImageResource(cn.yanhu.imchat.R.drawable.icon_voice_from3);
        } else {
            voiceImageView.setImageResource(cn.yanhu.imchat.R.drawable.icon_to_voice);
        }
    }
}
