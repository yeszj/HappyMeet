package cn.yanhu.imchat.custom.message.chatimageview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.utils.EaseImageUtils;

import cn.yanhu.imchat.custom.message.BaseEaseChatRowFile;


/**
 * image for row
 */
public class ChatRowImage extends BaseEaseChatRowFile {
    protected ImageView imageView;
    private EMImageMessageBody imgBody;
    private View inflate;

    public ChatRowImage(Context context, boolean isSender) {
        super(context, isSender);
    }

    public ChatRowImage(Context context, EMMessage message, int position, Object adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflate = inflater.inflate(!showSenderType ? R.layout.ease_row_received_picture
                : R.layout.ease_row_sent_picture, this);
    }
    @Override
    protected void onFindViewById() {
        super.onFindViewById();
        percentageView = (TextView) findViewById(R.id.percentage);
        imageView = (ImageView) findViewById(R.id.image);
    }

    @Override
    protected void onSetUpView() {
        super.onSetUpView();

        if (bubbleLayout != null) {
            bubbleLayout.setBackground(null);
        }
        imgBody = (EMImageMessageBody) message.getBody();
        // received messages
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            ViewGroup.LayoutParams params = EaseImageUtils.getImageShowSize(context, message);
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            layoutParams.width = params.width;
            layoutParams.height = params.height;
            return;
        }
        showImageView(message);
    }

    @Override
    protected void onViewUpdate(EMMessage msg) {
        super.onViewUpdate(msg);
        //此方法中省略掉了之前的有关非自动下载缩略图后展示图片的逻辑
    }

    @Override
    protected void onMessageSuccess() {
        super.onMessageSuccess();
        //即使是sender，发送成功后也要在执行，防止出现图片尺寸不对的问题
        showImageView(message);
    }

    @Override
    protected void onMessageInProgress() {
        if (message.direct() == EMMessage.Direct.SEND) {
            super.onMessageInProgress();
        } else {
            if (EMClient.getInstance().getOptions().getAutodownloadThumbnail()) {
                //imageView.setImageResource(R.drawable.ease_default_image);
            } else {
                progressBar.setVisibility(View.INVISIBLE);
                if (percentageView != null) {
                    percentageView.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    /**
     * load image into image view
     */
    @SuppressLint("StaticFieldLeak")
    private void showImageView(final EMMessage message) {
        EaseImageUtils.showImage(context, imageView, message);
    }
}
