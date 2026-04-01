package cn.yanhu.imchat.custom.chat

import android.R.attr.resource
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.os.Build
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.DynamicDrawableSpan
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.baselib.anim.AnimManager
import cn.yanhu.baselib.utils.CommonUtils.disableCopy
import cn.yanhu.baselib.utils.CommonUtils.drawableToBitmap
import cn.yanhu.baselib.utils.CommonUtils.getDimension
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.imchat.R
import cn.yanhu.imchat.view.emojiicon.VerticalImageSpan2
import com.blankj.utilcode.util.GsonUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.hjq.toast.ToastUtils
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.domain.EaseEmojicon
import com.hyphenate.easeui.modules.chat.EaseInputEditText
import com.hyphenate.easeui.modules.chat.EaseInputEditText.OnEditTextChangeListener
import com.hyphenate.easeui.modules.chat.EaseInputMenuStyle
import com.hyphenate.easeui.modules.chat.interfaces.EaseChatPrimaryMenuListener
import com.hyphenate.easeui.modules.chat.interfaces.IChatPrimaryMenu
import androidx.core.view.isVisible
import androidx.core.view.isGone
import cn.yanhu.baselib.utils.CoilImgUtils
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.commonres.api.commonRxApi
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ErrorCode
import com.xiaomi.push.da
import org.json.JSONObject

class CustomEaseChatPrimaryMenu(
    context: Context,
) : RelativeLayout(context), IChatPrimaryMenu, View.OnClickListener,
    OnEditTextChangeListener, TextWatcher {
    private var rlBottom: LinearLayout? = null
    private var buttonSetModeVoice: ImageView? = null
    private var buttonSetModeKeyboard: ImageView? = null
    private var buttonPressToSpeak: FrameLayout? = null
    private var edittext_layout: ViewGroup? = null
    private var editText: EaseInputEditText? = null
    private var buttonMore: CheckBox? = null
    private var vgVideoChat: ViewGroup? = null
    var buttonSend: Button? = null
    private var imExtendEmoji: ImageView? = null
    private var imExtendPhoto: ImageView? = null
    private var imExtendRecharge: ImageView? = null
    private var imExtendPhone: ImageView? = null
    private var imExtendGift: ImageView? = null
    private var quickMsgRv: RecyclerView? = null

    private var vgAddFriendTips: ViewGroup? = null

    private var listener: EaseChatPrimaryMenuListener? = null
    private var menuType: EaseInputMenuStyle? = EaseInputMenuStyle.All //菜单展示形式
    protected var inputManager: InputMethodManager
    protected var activity: Activity

    var animatorSet: AnimatorSet? = null
    private var isShowEmoji = false
    private var btmRlHeight = 0

    var ivFreeTag: ImageView? = null

    fun setShowEmoji(showEmoji: Boolean) {
        isShowEmoji = showEmoji
    }

    private fun initViews() {
        rlBottom = findViewById<LinearLayout>(R.id.rl_bottom)
        buttonSetModeVoice = findViewById<ImageView>(R.id.btn_set_mode_voice)
        buttonSetModeKeyboard = findViewById<ImageView>(R.id.btn_set_mode_keyboard)
        buttonPressToSpeak = findViewById<FrameLayout>(R.id.btn_press_to_speak)
        edittext_layout = findViewById<ViewGroup>(R.id.edittext_layout)
        editText = findViewById<EaseInputEditText?>(R.id.et_sendmessage)
        buttonMore = findViewById<CheckBox>(R.id.btn_more)
        buttonSend = findViewById<Button>(R.id.btn_send)
        imExtendEmoji = findViewById<ImageView>(R.id.im_extend_emoji)
        imExtendPhoto = findViewById<ImageView>(R.id.im_extend_photo)
        imExtendRecharge = findViewById<ImageView>(R.id.im_extend_recharge)
        imExtendPhone = findViewById<ImageView>(R.id.im_extend_phone)
        imExtendGift = findViewById<ImageView>(R.id.im_extend_gift)
        quickMsgRv = findViewById<RecyclerView>(R.id.chat_quick_mss_rv)
        vgVideoChat = findViewById<ViewGroup?>(R.id.vg_videoChat)

        btmRlHeight = rlBottom!!.getLayoutParams().height
        animatorSet = AnimManager.showScaleAnim(imExtendPhone!!, 1.2f)
        vgAddFriendTips = findViewById<ViewGroup>(R.id.vg_addFriendTips)
        ivFreeTag = findViewById<ImageView?>(R.id.iv_freeTag)
        showNormalStatus()
        hideSoftKeyboard()

        initListener()
        editText!!.disableCopy()

        checkMenuSwitch()
    }

    private fun checkMenuSwitch() {
        request({ commonRxApi.getConfigInfo("chat_menu_switch") },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    val result = data.data ?: return
                    val jsonObject = JSONObject(result)
                    val ifShowVideoChat = jsonObject.optBoolean("ifShowVideoChat", false)
                    val ifShowGift = jsonObject.optBoolean("ifShowGift", false)
                    if (ifShowVideoChat) {
                        vgVideoChat?.visibility = VISIBLE
                    } else {
                        if (ifShowGift){
                            vgVideoChat?.visibility = GONE
                        }else{
                            vgVideoChat?.visibility = INVISIBLE
                        }
                    }
                    if (ifShowGift) {
                        imExtendGift?.visibility = VISIBLE
                    } else {
                        if (ifShowVideoChat){
                            imExtendGift?.visibility = GONE
                        }else{
                            imExtendGift?.visibility = INVISIBLE
                        }
                    }
                }

            })
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("CheckResult")
    private fun createEmojiImage(emojicon: EaseEmojicon) {
        val inputEmojiCount = this.inputEmojiCount
        if (inputEmojiCount >= 5) {
            ToastUtils.show("单条消息不得超过5个表情")
            return
        }
        val iconPath = emojicon.iconPath
        val bigIconPath = arrayOf<String?>(GsonUtils.toJson(emojicon) + "/forlove")

        GlideUtils.loadAsBitmap(context,iconPath, getDimension(com.zj.dimens.R.dimen.dp_20)) { it ->
            val imageSpan = VerticalImageSpan2(
                context,
                it,
                DynamicDrawableSpan.ALIGN_CENTER
            )
            val s = editText!!.getText()
            if (!s.toString().endsWith("/forlove")) {
                bigIconPath[0] = "/forlove" + bigIconPath[0]
            }
            val selectionStart = editText!!.selectionStart
            val spannableString = SpannableString(bigIconPath[0])
            spannableString.setSpan(
                imageSpan,
                0,
                bigIconPath[0]!!.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            s.insert(editText!!.selectionStart, spannableString)
            editText!!.setText(s)
            editText!!.setSelection(selectionStart + bigIconPath[0]!!.length)
        }
    }

    private val inputEmojiCount: Int
        get() {
            val content = editText!!.getText().toString()
            if (TextUtils.isEmpty(content)) {
                return 0
            }
            val split =
                content.split("/forlove".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var count = 0
            for (value in split) {
                if ((value.contains("bigIconPath"))) {
                    count++
                }
            }
            return count
        }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        buttonSend!!.setOnSingleClickListener(1) { view: View? ->
            //发送消息
            if (checkIsFriend()) return@setOnSingleClickListener view
            if (listener != null) {
                val s = editText!!.getText().toString()
                editText!!.setText("")
                listener!!.onSendBtnClicked(s)
                //hideSoftKeyboard();
            }
            view
        }
        buttonSetModeKeyboard!!.setOnClickListener(this)
        buttonSetModeVoice!!.setOnClickListener(this)
        buttonMore!!.setOnClickListener(this)
        editText!!.setOnClickListener(this)
        imExtendEmoji!!.setOnClickListener(this)
        imExtendPhoto!!.setOnClickListener(this)
        imExtendRecharge!!.setOnClickListener(this)
        imExtendPhone!!.setOnClickListener(this)
        imExtendGift!!.setOnClickListener(this)
        editText!!.setOnEditTextChangeListener(this)
        editText!!.addTextChangedListener(this)
        buttonPressToSpeak!!.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent? ->
            if (listener != null) {
                val onPressToSpeakBtnTouch = listener!!.onPressToSpeakBtnTouch(v, event)
                onPressToSpeakBtnTouch
            }else{
                false
            }
        })
        vgAddFriendTips!!.setOnClickListener(OnClickListener { v: View? -> onChatTypeClickListener!!.onAddFriend() })
    }


    fun goneQuickMsgs() {
        if (quickMsgRv!!.isVisible) {
            quickMsgRv!!.visibility = GONE
        }
    }

    fun showQuickMsgs() {
        if (quickMsgRv!!.isGone) {
            quickMsgRv!!.visibility = VISIBLE
            //            cn.gxgre.forlove.utils.AnimManager.INSTANCE.createDropAnimator(quickMsgRv, 0, CommonUtils.getDimension(R.dimen.dp_38), 400, () -> {
//            },false);
        }
    }

    private fun checkSendButton() {
        if (TextUtils.isEmpty(editText!!.getText().toString().trim { it <= ' ' })) {
            buttonSend!!.visibility = GONE
        } else {
            buttonMore!!.visibility = GONE
            buttonSend!!.visibility = VISIBLE
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        editText!!.removeTextChangedListener(this)
    }

    override fun setMenuShowType(style: EaseInputMenuStyle?) {
        this.menuType = style
    }

    override fun showNormalStatus() {
        buttonSetModeVoice!!.setVisibility(VISIBLE)
        buttonSetModeKeyboard!!.setVisibility(GONE)
        edittext_layout!!.visibility = VISIBLE
        buttonPressToSpeak!!.visibility = GONE
        hideExtendStatus()
        checkSendButton()
    }

    override fun showTextStatus() {
        buttonSetModeVoice!!.setVisibility(VISIBLE)
        buttonSetModeKeyboard!!.setVisibility(GONE)
        edittext_layout!!.visibility = VISIBLE
        buttonPressToSpeak!!.visibility = GONE
        hideExtendStatus()
        showSoftKeyboard(editText)
        checkSendButton()

        if (listener != null) {
            listener!!.onToggleTextBtnClicked()
        }
    }

    override fun showVoiceStatus() {
        if (checkIsFriend()) return
        hideSoftKeyboard()
        buttonSetModeVoice!!.setVisibility(GONE)
        buttonSetModeKeyboard!!.setVisibility(VISIBLE)
        edittext_layout!!.setVisibility(GONE)
        buttonPressToSpeak!!.setVisibility(VISIBLE)
        hideExtendStatus()

        if (listener != null) {
            listener!!.onToggleVoiceBtnClicked()
        }
    }

    override fun showEmojiconStatus() {
    }

    override fun showMoreStatus() {
        if (buttonMore!!.isChecked) {
            hideSoftKeyboard()
            buttonSetModeVoice!!.setVisibility(VISIBLE)
            buttonSetModeKeyboard!!.setVisibility(GONE)
            edittext_layout!!.visibility = VISIBLE
            buttonPressToSpeak!!.visibility = GONE
        } else {
            showTextStatus()
        }

        if (listener != null) {
            listener!!.onToggleExtendClicked(buttonMore!!.isChecked)
        }
    }

    override fun hideExtendStatus() {
        buttonMore!!.setChecked(false)
    }

    override fun onEmojiconInputEvent(emojiContent: CharSequence?) {
        //插入表情
        editText!!.append(emojiContent)
    }

    override fun onInputCustomEmojicon(emojicon: EaseEmojicon) {
        createEmojiImage(emojicon)
    }

    override fun onEmojiconDeleteEvent() {
        if (!TextUtils.isEmpty(editText!!.getText())) {
            val event =
                KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL)
            editText!!.dispatchKeyEvent(event)
        }
    }

    override fun onTextInsert(text: CharSequence?) {
        val start = editText!!.getSelectionStart()
        val editable = editText!!.getEditableText()
        editable.insert(start, text)
        showTextStatus()
    }

    override fun getEditText(): EditText? {
        return editText
    }

    override fun setMenuBackground(bg: Drawable?) {
        rlBottom!!.background = bg
    }

    override fun setSendButtonBackground(bg: Drawable?) {
        buttonSend!!.background = bg
    }

    override fun onClick(v: View) {
        val id = v.getId()
        if (id == R.id.btn_set_mode_keyboard) { //切换到文本模式
            showTextStatus()
        } else if (id == R.id.btn_more) { //切换到更多模式
            showMoreStatus()
        } else if (id == R.id.et_sendmessage) { //切换到文本模式
//            isShowEmoji = false;
//            onChatTypeClickListener.onShowEmojiInput(false);
            showTextStatus()
        } else if (id == R.id.btn_set_mode_voice) { //切换到语音模式
            onChatTypeClickListener!!.clickVoice()
        } else if (id == R.id.im_extend_emoji) { //表情
            if (checkIsFriend()) return
            isShowEmoji = !isShowEmoji
            onChatTypeClickListener!!.onShowEmojiInput(isShowEmoji)
        } else if (id == R.id.im_extend_photo) { //相册
            if (checkIsFriend()) return
            onChatTypeClickListener!!.clickPhoto()
        } else if (id == R.id.im_extend_recharge) {
            onChatTypeClickListener!!.onRecharge()
        } else if (id == R.id.im_extend_phone) { //通话
            if (checkIsFriend()) return
            onChatTypeClickListener!!.clickPhone()
        } else if (id == R.id.im_extend_gift) { //礼物
            if (checkIsFriend()) return
            onChatTypeClickListener!!.onSendGift()
        }
    }

    private fun checkIsFriend(): Boolean {
        if (vgAddFriendTips!!.isVisible) {
            onChatTypeClickListener!!.onAddFriend()
            return true
        }
        return false
    }

    override fun onClickKeyboardSendBtn(content: String?) {
        if (listener != null) {
            listener!!.onSendBtnClicked(content)
        }
    }

    override fun onEditTextHasFocus(hasFocus: Boolean) {
        try {
            Thread.sleep(50L)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
        if (hasFocus) {
            isShowEmoji = false
            onChatTypeClickListener!!.onShowEmojiInput(false)
        }
        if (listener != null) {
            listener!!.onEditTextHasFocus(hasFocus)
        }
    }

    fun adjustLayoutHeight(isKeyboardVisible: Boolean, pxValue: Int) {
        val layoutHeight: Int
        if (isKeyboardVisible) {
            layoutHeight = pxValue + btmRlHeight
        } else {
            layoutHeight = btmRlHeight
        }
        rlBottom!!.layoutParams.height = layoutHeight
        rlBottom!!.requestLayout()
    }

    private fun showSendButton(s: CharSequence?) {
        if (!TextUtils.isEmpty(s)) {
            buttonSend!!.visibility = VISIBLE
        } else {
            buttonSend!!.visibility = GONE
        }
    }

    /**
     * hide soft keyboard
     */
    override fun hideSoftKeyboard() {
        if (editText == null) {
            return
        }
        //        editText.requestFocus();
        if (activity.window
                .attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        ) {
            if (activity.currentFocus != null) inputManager.hideSoftInputFromWindow(
                activity.currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    /**
     * show soft keyboard
     */
    private fun showSoftKeyboard(et: EditText?) {
        if (et == null) {
            return
        }

        et.requestFocus()
        inputManager.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun setEaseChatPrimaryMenuListener(listener: EaseChatPrimaryMenuListener?) {
        this.listener = listener
    }

    override fun primaryStartQuote(message: EMMessage?) {
    }

    override fun hideQuoteSelect() {
    }

    override fun getQuoteLayout(): ConstraintLayout? {
        return null
    }

    override fun setShowDefaultQuote(isShowDefault: Boolean) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        Log.e("TAG", this.javaClass.getSimpleName() + " onTextChanged s:" + s)
        showSendButton(s)
        if (listener != null) {
            listener!!.onTyping(s, start, before, count)
        }
    }

    override fun afterTextChanged(s: Editable?) {
        Log.e("TAG", this.javaClass.getSimpleName() + " afterTextChanged s:" + s)
    }

    /*
     * 底部扩展监听
     * */
    private var onChatTypeClickListener: OnChatTypeClickListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.ease_widget_chat_primary_menu, this)
        activity = context as Activity
        inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        initViews()
    }

    fun registerMenuClickListener(onChatTypeClickListener: OnChatTypeClickListener) {
        this.onChatTypeClickListener = onChatTypeClickListener
    }


    fun setUserInfo(userInfo: UserDetailInfo) {
        if (userInfo.isFriend) {
            vgAddFriendTips!!.visibility = GONE
            edittext_layout!!.visibility = VISIBLE
            editText!!.setHint("输入聊天内容")
            showNormalStatus()
        } else {
            vgAddFriendTips!!.visibility = VISIBLE
            buttonPressToSpeak!!.visibility = GONE
            edittext_layout!!.visibility = GONE
        }
    }


    interface OnChatTypeClickListener {
        fun clickVoice()

        fun clickPhoto()

        fun clickHotChatTxt()

        fun onShowEmojiInput(show: Boolean)

        fun onSendCustomEmoji(url: String?)


        fun onSendGift()

        fun onAddFriend()

        fun onRecharge()

        fun clickPhone()

        fun onAddVoiceMsg()
    }
}
