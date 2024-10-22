package cn.yanhu.imchat.custom.message.chattextview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.blankj.utilcode.util.GsonUtils;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMTranslationResult;
import com.hyphenate.easeui.manager.EaseDingMessageHelper;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.easeui.widget.chatrow.AutolinkSpan;

import cn.yanhu.baselib.utils.CommonUtils;
import cn.yanhu.baselib.widget.spans.Spans;
import cn.yanhu.imchat.R;
import cn.yanhu.imchat.custom.message.BaseEaseChatRow;
import cn.yanhu.commonres.config.ChatConstant;
import cn.yanhu.imchat.bean.GroupChatUserInfo;

@SuppressLint("ViewConstructor")
public class ChatTextNew extends BaseEaseChatRow {
    private TextView contentView;
    private TextView translationContentView;
    private View translationContainer;
    private View inflate;

    public ChatTextNew(Context context, boolean isSender) {
        super(context, isSender);
    }

    public ChatTextNew(Context context, EMMessage message, int position, Object adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflate = inflater.inflate(!showSenderType ? R.layout.ease_row_received_message
                : R.layout.ease_row_sent_message, this);
    }

    @Override
    protected void onFindViewById() {
        super.onFindViewById();
        contentView = findViewById(R.id.tv_chatcontent);
        translationContentView = findViewById(R.id.tv_subContent);
        translationContainer = findViewById(R.id.subBubble);
    }

    @Override
    protected void onSetUpView() {
        super.onSetUpView();

        EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
        if (txtBody != null) {
            String content = txtBody.getMessage();
            String stringAttribute = message.getStringAttribute(ChatConstant.WELCOME_CONTENT, "");


            if (!TextUtils.isEmpty(stringAttribute)) {
                if (isSender) {
                    contentView.setText(String.format("@%s %s", content, stringAttribute));
                } else {
                    Spans build = Spans.builder()
                            .text("@" + content + " ").color(Color.parseColor("#EA4528")).style(Typeface.BOLD)
                            .text(stringAttribute).color(CommonUtils.getColor(cn.yanhu.commonres.R.color.cl_common)).build();
                    contentView.setText(build);
                }

            } else {
                String aTeUserInfo = message.getStringAttribute(
                        ChatConstant.ATE_USER_INFO, "");
                if (!TextUtils.isEmpty(aTeUserInfo)){
                    //群聊@用户 数据绑定
                    Spannable span = EaseSmileUtils.getSmiledText(context, txtBody.getMessage());
                    GroupChatUserInfo groupChatUserInfo = GsonUtils.fromJson(aTeUserInfo,GroupChatUserInfo.class);
                    if (isSender) {
                        contentView.setText(String.format("@%s %s", groupChatUserInfo.getNickName(), span));
                    } else {
                        Spans build = Spans.builder()
                                .text("@" + groupChatUserInfo.getNickName() + " ").color(Color.parseColor("#EA4528")).style(Typeface.BOLD)
                                .text(span).color(CommonUtils.getColor(cn.yanhu.commonres.R.color.cl_common)).build();
                        contentView.setText(build);
                    }
                }else {
                    Spannable span = EaseSmileUtils.getSmiledText(context, txtBody.getMessage());
                    // 设置内容
                    contentView.setText(span, BufferType.SPANNABLE);
                    replaceSpan();
                }
            }
            contentView.setOnLongClickListener(v -> {
//                    contentView.setTag(R.id.action_chat_long_click, true);
                if (itemClickListener != null) {
                    return itemClickListener.onBubbleLongClick(v, message);
                }
                return false;
            });

            EMTranslationResult result = EMClient.getInstance().translationManager().getTranslationResult(message.getMsgId());
            if (result != null) {
                if (result.showTranslation()) {
                    translationContainer.setVisibility(View.VISIBLE);
                    translationContentView.setText(result.translatedText());
                    translationContainer.setOnLongClickListener(new OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
//                            contentView.setTag(R.id.action_chat_long_click, true);
                            if (itemClickListener != null) {
                                return itemClickListener.onBubbleLongClick(v, message);
                            }
                            return false;
                        }
                    });
                } else {
                    translationContainer.setVisibility(View.GONE);
                }
            } else {
                translationContainer.setVisibility(View.GONE);
            }
        }



    }


    /**
     * 解决长按事件与relink冲突，参考：https://www.jianshu.com/p/d3bef8449960
     */
    private void replaceSpan() {
        Spannable spannable = (Spannable) contentView.getText();
        URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        for (URLSpan span : spans) {
            String url = span.getURL();
            int index = spannable.toString().indexOf(url);
            int end = index + url.length();
            if (index == -1) {
                if (url.contains("http://")) {
                    url = url.replace("http://", "");
                } else if (url.contains("https://")) {
                    url = url.replace("https://", "");
                } else if (url.contains("rtsp://")) {
                    url = url.replace("rtsp://", "");
                }
                index = spannable.toString().indexOf(url);
                end = index + url.length();
            }
            if (index != -1) {
                spannable.removeSpan(span);
                spannable.setSpan(new AutolinkSpan(span.getURL()), index
                        , end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
    }

    @Override
    protected void onMessageCreate() {
        setStatus(View.GONE, View.GONE);
    }

    @Override
    protected void onMessageSuccess() {
        super.onMessageSuccess();
        setStatus(View.GONE, View.GONE);
        if (isSender() && EaseDingMessageHelper.get().isDingMessage(message) && ackedView != null) {
            ackedView.setVisibility(VISIBLE);
            int count = message.groupAckCount();
            ackedView.setText("已读");

        }
    }

    @Override
    protected void onMessageError() {
        super.onMessageError();
        setStatus(View.GONE, View.VISIBLE);
    }

    @Override
    protected void onMessageInProgress() {
        setStatus(View.GONE, View.GONE);
    }

    /**
     * set progress and status view visible or gone
     */
    private void setStatus(int progressVisible, int statusVisible) {
        if (progressBar != null) {
            progressBar.setVisibility(progressVisible);
        }
        if (statusView != null) {
            statusView.setVisibility(statusVisible);
        }
    }
}
