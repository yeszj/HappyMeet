package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import cn.yanhu.agora.R
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.databinding.PopSendChatRoomMessageBinding
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.widget.SimpleTextWatcher
import cn.yanhu.commonres.adapter.MessageLottieExpressionAdapter
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.ExpressionInfo
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ThreadUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.interfaces.XPopupCallback

/**
 * @author: witness
 * created: 2022/12/14
 * desc:
 */
@SuppressLint("ViewConstructor")
class SendMessagePop(
    context: Context,
    private var isKeyBoard: Boolean = true,
    private var user: BaseUserInfo?,
    private val messageSendListener: OnMessageSendListener,
) : BottomPopupView(context) {
    private val expressionAdapter by lazy {
        MessageLottieExpressionAdapter()
    }
    private var expressionList: List<ExpressionInfo> = mutableListOf()
    override fun getImplLayoutId(): Int {
        return R.layout.pop_send_chat_room_message
    }

    private var mBinding: PopSendChatRoomMessageBinding? = null

    private var altInfo = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        mBinding = PopSendChatRoomMessageBinding.bind(popupImplView)

        setMentionInfo(user)
        initEmojiAdapter()
        initListener()
        getExpression()
        setOperateImg()
        mBinding!!.etMessage.setOnKeyListener(object : OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_DEL && event?.action == KeyEvent.ACTION_DOWN) {
                    val toString = mBinding!!.etMessage.text.toString()
                    if (toString == altInfo) {
                        mBinding!!.etMessage.setText("")
                        return true
                    }
                    return false
                }
                return false
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setMentionInfo(user: BaseUserInfo?) {
        if (user != null) {
            altInfo = "@${user.nickName} "
        }
        if (isShow) {
            mBinding?.apply {
                if (user != null) {
                    this.etMessage.setText(altInfo)
                    this.etMessage.setSelection(this.etMessage.text.toString().length)
                } else {
                    this.etMessage.setText("")
                }
            }

        }

    }

    private fun initEmojiAdapter() {
        mBinding?.apply {
            this.rvExpression.adapter = expressionAdapter
            expressionAdapter.setOnItemClickListener { _, _, position ->
                val item = expressionAdapter.getItem(position)
                messageSendListener.onSendEmoji(item!!.url)
            }
        }

    }

    private fun initListener() {
        mBinding?.apply {
            this.ivOperate.setOnSingleClickListener {
                (!isKeyBoard).also {
                    isKeyBoard = it
                    showKeyboard()
                }
            }
            this.ivSend.setOnSingleClickListener {
                var content = this.etMessage.text.toString().trim()
                if (user != null && content.contains(altInfo)) {
                    content = content.replace(altInfo, "")
                    messageSendListener.onSendMessage(content,true)
                } else {
                    if (!TextUtils.isEmpty(content)) {
                        messageSendListener.onSendMessage(content,false)
                    }
                }
                this.etMessage.setText("")
            }
            this.etMessage.addTextChangedListener(object : SimpleTextWatcher() {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (!TextUtils.isEmpty(s)) {
                        mBinding!!.ivSend.setImageResource(cn.yanhu.commonres.R.drawable.ic_green_msg_send)
                    } else {
                        mBinding!!.ivSend.setImageResource(cn.yanhu.commonres.R.drawable.ic_gray_msg_send)
                    }
                }
            })
        }

    }

    private fun showKeyboard() {
        mBinding?.apply {
            if (!isKeyBoard) {
                this.etMessage.isFocusable = false
                this.etMessage.isFocusableInTouchMode = false
                KeyboardUtils.hideSoftInput(this.etMessage)
                this.rvExpression.visibility = VISIBLE
                ThreadUtils.getMainHandler().post {
                    val height = vgParent.height
                    messageSendListener.onShowEmoji(height)
                }
            } else {
                this.rvExpression.visibility = GONE
                this.etMessage.isFocusable = true
                this.etMessage.isFocusableInTouchMode = true
                messageSendListener.onShowEmoji(0)
                KeyboardUtils.showSoftInput(this.etMessage)
            }
            setOperateImg()
        }

    }

    private fun setOperateImg() {
        mBinding?.apply {
            if (isKeyBoard) {
                this.ivOperate.setImageResource(cn.yanhu.commonres.R.drawable.icon_gray_smile)
            } else {
                this.ivOperate.setImageResource(cn.yanhu.commonres.R.drawable.ic_gray_keyboard)
            }
        }

    }


    private fun getExpression() {
        request({ agoraRxApi.getExpression() },
            object : OnRequestResultListener<List<ExpressionInfo>> {
                override fun onSuccess(data: BaseBean<List<ExpressionInfo>>) {
                    expressionList = data.data!!
                    expressionAdapter.submitList(expressionList)
                }
            })
    }

    override fun beforeDismiss() {
        super.beforeDismiss()
        mBinding?.rvExpression?.visibility = View.GONE
    }

    override fun beforeShow() {
        super.beforeShow()
        mBinding?.apply {
            if (isKeyBoard) {
                rvExpression.visibility = View.GONE
            } else {
                rvExpression.visibility = View.VISIBLE
            }
        }
    }

    override fun onShow() {
        super.onShow()
        showKeyboard()
    }


    interface OnMessageSendListener {
        fun onSendMessage(content: String,hasAlt:Boolean)
        fun onSendEmoji(url: String)
        fun onShowEmoji(height: Int)
    }

    @SuppressLint("SetTextI18n")
    fun showDialog(isKeyBoard: Boolean, user: BaseUserInfo?) {
        this.isKeyBoard = isKeyBoard
        this.user = user
        setMentionInfo(user)
        show()
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            isKeyboard: Boolean = true,
            user: BaseUserInfo?,
            messageSendListener: OnMessageSendListener,
            xPopupCallback: XPopupCallback,
        ): SendMessagePop {
            val confirmPayDialog =
                SendMessagePop(mContext, isKeyboard, user, messageSendListener)
            val builder = XPopup.Builder(mContext)
            builder.setPopupCallback(xPopupCallback).autoFocusEditText(isKeyboard)
                .hasShadowBg(false)
                //.isRequestFocus(isKeyboard)
                .autoOpenSoftInput(isKeyboard).asCustom(confirmPayDialog).show()
            return confirmPayDialog
        }


    }


}