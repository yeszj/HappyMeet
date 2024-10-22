package cn.yanhu.imchat.custom.chat;

import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.DynamicDrawableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.GsonUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hjq.toast.ToastUtils;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.modules.chat.EaseInputEditText;
import com.hyphenate.easeui.modules.chat.EaseInputMenuStyle;
import com.hyphenate.easeui.modules.chat.interfaces.EaseChatPrimaryMenuListener;
import com.hyphenate.easeui.modules.chat.interfaces.IChatPrimaryMenu;

import cn.yanhu.baselib.anim.AnimManager;
import cn.yanhu.baselib.utils.CommonUtils;
import cn.yanhu.baselib.utils.ext.ViewExtKt;
import cn.yanhu.imchat.R;
import cn.yanhu.imchat.view.emojiicon.VerticalImageSpan2;


public class CustomEaseChatPrimaryMenu extends RelativeLayout implements IChatPrimaryMenu, View.OnClickListener, EaseInputEditText.OnEditTextChangeListener, TextWatcher {
    private LinearLayout rlBottom;
    private ImageView buttonSetModeVoice;
    private ImageView buttonSetModeKeyboard;
    private FrameLayout buttonPressToSpeak;
    private ViewGroup edittext_layout;
    private EaseInputEditText editText;
    private CheckBox buttonMore;
    public Button buttonSend;
    private ImageView imExtendEmoji;
    private ImageView imExtendPhoto;
    private ImageView imExtendRecharge;
    private ImageView imExtendPhone;
    private ImageView imExtendGift;
    private RecyclerView quickMsgRv;

    private ViewGroup vgRechargeTips;

    private EaseChatPrimaryMenuListener listener;
    private EaseInputMenuStyle menuType = EaseInputMenuStyle.All;//菜单展示形式
    protected InputMethodManager inputManager;
    protected Activity activity;

    public AnimatorSet animatorSet;
    private boolean isShowEmoji = false;
    private int btmRlHeight = 0;

    public ImageView ivFreeTag;

    public void setShowEmoji(boolean showEmoji) {
        isShowEmoji = showEmoji;
    }

    public CustomEaseChatPrimaryMenu(Context context) {
        this(context, null);
    }

    public CustomEaseChatPrimaryMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomEaseChatPrimaryMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.ease_widget_chat_primary_menu, this);
        activity = (Activity) context;
        inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        initViews();
    }

    private void initViews() {
        rlBottom = findViewById(R.id.rl_bottom);
        buttonSetModeVoice = findViewById(R.id.btn_set_mode_voice);
        buttonSetModeKeyboard = findViewById(R.id.btn_set_mode_keyboard);
        buttonPressToSpeak = findViewById(R.id.btn_press_to_speak);
        edittext_layout = findViewById(R.id.edittext_layout);
        editText = findViewById(R.id.et_sendmessage);
        buttonMore = findViewById(R.id.btn_more);
        buttonSend = findViewById(R.id.btn_send);
        imExtendEmoji = findViewById(R.id.im_extend_emoji);
        imExtendPhoto = findViewById(R.id.im_extend_photo);
        imExtendRecharge = findViewById(R.id.im_extend_recharge);
        imExtendPhone = findViewById(R.id.im_extend_phone);
        imExtendGift = findViewById(R.id.im_extend_gift);
        quickMsgRv = findViewById(R.id.chat_quick_mss_rv);

        btmRlHeight = rlBottom.getLayoutParams().height;
        animatorSet = AnimManager.showScaleAnim(imExtendPhone, 1.2f);
        vgRechargeTips = findViewById(R.id.vg_rechargeTips);
        ivFreeTag = findViewById(R.id.iv_freeTag);
        showNormalStatus();
        hideSoftKeyboard();

        initListener();
        CommonUtils.INSTANCE.disableCopy(editText);
    }

    @SuppressLint("CheckResult")
    private void createEmojiImage(EaseEmojicon emojicon) {
        int inputEmojiCount = getInputEmojiCount();
        if (inputEmojiCount >= 5) {
            ToastUtils.show("单条消息不得超过5个表情");
            return;
        }
        String iconPath = emojicon.getIconPath();
        final String[] bigIconPath = {GsonUtils.toJson(emojicon) + "/forlove"};
        Glide.with(getContext()).as(PictureDrawable.class).override(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20)).load(iconPath).into(new CustomTarget<PictureDrawable>() {
            @Override
            public void onResourceReady(@NonNull PictureDrawable resource, @Nullable Transition<? super PictureDrawable> transition) {
                VerticalImageSpan2 imageSpan = new VerticalImageSpan2(getContext(), CommonUtils.INSTANCE.drawableToBitmap(resource), DynamicDrawableSpan.ALIGN_CENTER);
                Editable s = editText.getText();
                if (!s.toString().endsWith("/forlove")) {
                    bigIconPath[0] = "/forlove" + bigIconPath[0];
                }
                int selectionStart = editText.getSelectionStart();
                SpannableString spannableString = new SpannableString(bigIconPath[0]);
                spannableString.setSpan(imageSpan, 0, bigIconPath[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                s.insert(editText.getSelectionStart(), spannableString);
                editText.setText(s);
                editText.setSelection(selectionStart + bigIconPath[0].length());
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });


    }

    private int getInputEmojiCount() {
        String content = editText.getText().toString();
        if (TextUtils.isEmpty(content)) {
            return 0;
        }
        String[] split = content.split("/forlove");
        int count = 0;
        for (String value : split) {
            if ((value.contains("bigIconPath"))) {
                count++;
            }
        }
        return count;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListener() {
        buttonSend.setOnClickListener(this);
        ViewExtKt.setOnSingleClickListener(buttonSend, 1, view -> {
            //发送消息
            if (listener != null) {
                String s = editText.getText().toString();
                editText.setText("");
                listener.onSendBtnClicked(s);
                //hideSoftKeyboard();
            }
            return view;
        });
        buttonSetModeKeyboard.setOnClickListener(this);
        buttonSetModeVoice.setOnClickListener(this);
        buttonMore.setOnClickListener(this);
        editText.setOnClickListener(this);
        imExtendEmoji.setOnClickListener(this);
        imExtendPhoto.setOnClickListener(this);
        imExtendRecharge.setOnClickListener(this);
        imExtendPhone.setOnClickListener(this);
        imExtendGift.setOnClickListener(this);
        editText.setOnEditTextChangeListener(this);
        editText.addTextChangedListener(this);
        buttonPressToSpeak.setOnTouchListener((v, event) -> {
            if (listener != null) {
                return listener.onPressToSpeakBtnTouch(v, event);
            }
            return false;
        });
        vgRechargeTips.setOnClickListener(v -> onChatTypeClickListener.onRecharge());
    }



    public void goneQuickMsgs() {
        if (quickMsgRv.getVisibility() == VISIBLE) {
            quickMsgRv.setVisibility(GONE);
        }
    }

    public void showQuickMsgs() {
        if (quickMsgRv.getVisibility() == GONE) {
            quickMsgRv.setVisibility(VISIBLE);
//            cn.gxgre.forlove.utils.AnimManager.INSTANCE.createDropAnimator(quickMsgRv, 0, CommonUtils.getDimension(R.dimen.dp_38), 400, () -> {
//            },false);
        }
    }

    private void checkSendButton() {
        if (TextUtils.isEmpty(editText.getText().toString().trim())) {
            buttonSend.setVisibility(GONE);
        } else {
            buttonMore.setVisibility(GONE);
            buttonSend.setVisibility(VISIBLE);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        editText.removeTextChangedListener(this);
    }

    @Override
    public void setMenuShowType(EaseInputMenuStyle style) {
        this.menuType = style;
    }

    @Override
    public void showNormalStatus() {
        buttonSetModeVoice.setVisibility(VISIBLE);
        buttonSetModeKeyboard.setVisibility(GONE);
        edittext_layout.setVisibility(VISIBLE);
        buttonPressToSpeak.setVisibility(GONE);
        hideExtendStatus();
        checkSendButton();

    }

    @Override
    public void showTextStatus() {
        if (vgRechargeTips.getVisibility() == View.VISIBLE) {
            onChatTypeClickListener.onRecharge();
            return;
        }
        buttonSetModeVoice.setVisibility(VISIBLE);
        buttonSetModeKeyboard.setVisibility(GONE);
        edittext_layout.setVisibility(VISIBLE);
        buttonPressToSpeak.setVisibility(GONE);
        hideExtendStatus();
        showSoftKeyboard(editText);
        checkSendButton();

        if (listener != null) {
            listener.onToggleTextBtnClicked();
        }
    }

    @Override
    public void showVoiceStatus() {
        if (vgRechargeTips.getVisibility() == View.VISIBLE) {
            onChatTypeClickListener.onRecharge();
            return;
        }
        hideSoftKeyboard();
        buttonSetModeVoice.setVisibility(GONE);
        buttonSetModeKeyboard.setVisibility(VISIBLE);
        edittext_layout.setVisibility(GONE);
        buttonPressToSpeak.setVisibility(VISIBLE);
        hideExtendStatus();

        if (listener != null) {
            listener.onToggleVoiceBtnClicked();
        }
    }

    @Override
    public void showEmojiconStatus() {
    }

    @Override
    public void showMoreStatus() {
        if (buttonMore.isChecked()) {
            hideSoftKeyboard();
            buttonSetModeVoice.setVisibility(VISIBLE);
            buttonSetModeKeyboard.setVisibility(GONE);
            edittext_layout.setVisibility(VISIBLE);
            buttonPressToSpeak.setVisibility(GONE);
        } else {
            showTextStatus();
        }

        if (listener != null) {
            listener.onToggleExtendClicked(buttonMore.isChecked());
        }
    }

    @Override
    public void hideExtendStatus() {
        buttonMore.setChecked(false);
    }

    @Override
    public void onEmojiconInputEvent(CharSequence emojiContent) {
        //插入表情
        editText.append(emojiContent);
    }

    @Override
    public void onInputCustomEmojicon(EaseEmojicon emojicon) {
        createEmojiImage(emojicon);
    }

    @Override
    public void onEmojiconDeleteEvent() {
        if (!TextUtils.isEmpty(editText.getText())) {
            KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
            editText.dispatchKeyEvent(event);
        }
    }

    @Override
    public void onTextInsert(CharSequence text) {
        int start = editText.getSelectionStart();
        Editable editable = editText.getEditableText();
        editable.insert(start, text);
        showTextStatus();
    }

    @Override
    public EditText getEditText() {
        return editText;
    }

    @Override
    public void setMenuBackground(Drawable bg) {
        rlBottom.setBackground(bg);
    }

    @Override
    public void setSendButtonBackground(Drawable bg) {
        buttonSend.setBackground(bg);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_set_mode_keyboard) {//切换到文本模式
            showTextStatus();
        } else if (id == R.id.btn_more) {//切换到更多模式
            showMoreStatus();
        } else if (id == R.id.et_sendmessage) { //切换到文本模式
//            isShowEmoji = false;
//            onChatTypeClickListener.onShowEmojiInput(false);
            showTextStatus();
        } else if (id == R.id.btn_set_mode_voice) {//切换到语音模式
            onChatTypeClickListener.clickVoice();

        } else if (id == R.id.im_extend_emoji) {//表情
            if (vgRechargeTips.getVisibility() == View.VISIBLE) {
                onChatTypeClickListener.onRecharge();
                return;
            }
            isShowEmoji = !isShowEmoji;
            onChatTypeClickListener.onShowEmojiInput(isShowEmoji);

        } else if (id == R.id.im_extend_photo) {//相册
            onChatTypeClickListener.clickPhoto();

        } else if (id == R.id.im_extend_recharge) {
            onChatTypeClickListener.onRecharge();

        } else if (id == R.id.im_extend_phone) {//通话
            onChatTypeClickListener.clickPhone();

        } else if (id == R.id.im_extend_gift) {//礼物
            onChatTypeClickListener.onSendGift();
        }
    }

    @Override
    public void onClickKeyboardSendBtn(String content) {
        if (listener != null) {
            listener.onSendBtnClicked(content);
        }
    }

    @Override
    public void onEditTextHasFocus(boolean hasFocus) {
        try {
            Thread.sleep(50L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (hasFocus) {
            isShowEmoji = false;
            onChatTypeClickListener.onShowEmojiInput(false);
        }
        if (listener != null) {
            listener.onEditTextHasFocus(hasFocus);
        }
    }

    public void adjustLayoutHeight(boolean isKeyboardVisible, int pxValue) {
        int layoutHeight;
        if (isKeyboardVisible) {
            layoutHeight = pxValue + btmRlHeight;
        } else {
            layoutHeight = btmRlHeight;
        }
        rlBottom.getLayoutParams().height = layoutHeight;
        rlBottom.requestLayout();
    }

    private void showSendButton(CharSequence s) {
        if (!TextUtils.isEmpty(s)) {
            buttonSend.setVisibility(View.VISIBLE);
        } else {
            buttonSend.setVisibility(View.GONE);
        }

    }

    /**
     * hide soft keyboard
     */
    @Override
    public void hideSoftKeyboard() {
        if (editText == null) {
            return;
        }
//        editText.requestFocus();
        if (activity.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (activity.getCurrentFocus() != null)
                inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * show soft keyboard
     */
    private void showSoftKeyboard(EditText et) {

        if (et == null) {
            return;
        }

        et.requestFocus();
        inputManager.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void setEaseChatPrimaryMenuListener(EaseChatPrimaryMenuListener listener) {
        this.listener = listener;
    }

    @Override
    public void primaryStartQuote(EMMessage message) {

    }

    @Override
    public void hideQuoteSelect() {

    }

    @Override
    public ConstraintLayout getQuoteLayout() {
        return null;
    }

    @Override
    public void setShowDefaultQuote(boolean isShowDefault) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.e("TAG", this.getClass().getSimpleName() + " onTextChanged s:" + s);
        showSendButton(s);
        if (listener != null) {
            listener.onTyping(s, start, before, count);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.e("TAG", this.getClass().getSimpleName() + " afterTextChanged s:" + s);
    }

    /*
     * 底部扩展监听
     * */
    private OnChatTypeClickListener onChatTypeClickListener;

    public void registerMenuClickListener(OnChatTypeClickListener onChatTypeClickListener) {
        this.onChatTypeClickListener = onChatTypeClickListener;
    }

//    public void setFreeChatTipInfo(FreeChatTipBean data) {
//        if (data.isFriend()) {
//            if (vgRechargeTips.getVisibility() == View.VISIBLE) {
//                vgRechargeTips.setVisibility(View.GONE);
//                edittext_layout.setVisibility(View.VISIBLE);
//                editText.setHint("输入聊天内容");
//                showNormalStatus();
//            }
//        } else {
//            int chatCardCount = data.getChatCardCount();
//            int darlingFreeCount = data.getDarlingFreeCount();
//            if (chatCardCount > 0 || darlingFreeCount > 0) {
//                editText.setHint("免费消息剩余" + (chatCardCount + darlingFreeCount) + "条");
//            } else {
//                editText.setHint("输入聊天内容");
//            }
//            if (darlingFreeCount <= 0 && chatCardCount <= 0 && !CommonUtils.INSTANCE.compareString(data.getRoseNum().toPlainString(), data.getChatConsumeRose().toPlainString())) {
//                vgRechargeTips.setVisibility(View.VISIBLE);
//                buttonPressToSpeak.setVisibility(View.GONE);
//                edittext_layout.setVisibility(View.GONE);
//            } else {
//                vgRechargeTips.setVisibility(View.GONE);
//                edittext_layout.setVisibility(View.VISIBLE);
//                showNormalStatus();
//            }
//        }
//    }

    public interface OnChatTypeClickListener {
        void clickVoice();

        void clickPhoto();

        void clickHotChatTxt();

        void onShowEmojiInput(boolean show);

        void onSendCustomEmoji(String url);


        void onSendGift();

        void onRecharge();

        void clickPhone();

        void onAddVoiceMsg();
    }
}
