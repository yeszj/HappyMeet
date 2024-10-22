package cn.yanhu.imchat.custom.message.chatvoiceview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.utils.EaseVoiceLengthUtils;
import com.hyphenate.util.EMLog;

import cn.yanhu.imchat.custom.chat.EaseChatRowVoicePlayer;
import cn.yanhu.imchat.custom.message.BaseEaseChatRowFile;


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
        voiceImageView = ((LottieAnimationView) findViewById(R.id.iv_voice));
        voiceLengthView = (TextView) findViewById(R.id.tv_length);
        readStatusView = (ImageView) findViewById(R.id.iv_unread_voice);


    }

    @Override
    protected void onSetUpView() {
        super.onSetUpView();

        EMVoiceMessageBody voiceBody = (EMVoiceMessageBody) message.getBody();
        int len = voiceBody.getLength();
        int padding = 0;
        if (len > 0) {
            padding = EaseVoiceLengthUtils.getVoiceLength(getContext(), len);
            voiceLengthView.setText(voiceBody.getLength() + "\"");
            voiceLengthView.setVisibility(View.VISIBLE);
        } else {
            voiceLengthView.setVisibility(View.INVISIBLE);
        }
        if (!showSenderType) {
            voiceImageView.setImageResource(cn.yanhu.imchat.R.drawable.icon_voice_from3);
            voiceLengthView.setPadding(0, 0, padding, 0);
        } else {
            voiceImageView.setImageResource(cn.yanhu.imchat.R.drawable.icon_to_voice);
            voiceLengthView.setPadding(padding, 0, 0, 0);
        }

        if (message.direct() == EMMessage.Direct.RECEIVE) {
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
            // hide the unread icon
            readStatusView.setVisibility(View.INVISIBLE);
        }

        // To avoid the item is recycled by listview and slide to this item again but the animation is stopped.
        EaseChatRowVoicePlayer voicePlayer = EaseChatRowVoicePlayer.getInstance(getContext());
        if (voicePlayer.isPlaying() && message.getMsgId().equals(voicePlayer.getCurrentPlayingId())) {
            startVoicePlayAnimation();
        }
    }

    @Override
    protected void onViewUpdate(EMMessage msg) {
        super.onViewUpdate(msg);

        // Only the received message has the attachment download status.
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
            voiceImageView.setAnimation("voice_msg_play.json");
        } else {
            voiceImageView.setAnimation("voice_msg_play_reverse.json");
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
