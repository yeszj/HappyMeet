package cn.yanhu.imchat.ui.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.StatusBarUtil
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.imchat.ImChatViewModel
import cn.yanhu.imchat.R
import cn.yanhu.imchat.databinding.ActivityImChatBinding
import cn.yanhu.imchat.db.ChatUserInfoManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.GsonUtils
import com.hyphenate.easeui.constants.EaseConstant


/**
 * @author: zhengjun
 * created: 2024/2/2
 * desc:
 */
class ImChatActivity : BaseActivity<ActivityImChatBinding, ImChatViewModel>(
    R.layout.activity_im_chat,
    ImChatViewModel::class.java
) {

    private lateinit var imChatFrg: ImChatFrg
     var conversationId:String = ""
    override fun initData() {
        conversationId = intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_ID).toString()
        setStatusBarStyle(false)
        imChatFrg  = ImChatFrg()
        imChatFrg.arguments = intent.extras
        addFragment(imChatFrg)
        val chatActivity: Activity? = getChatActivity()
        (chatActivity as? ImChatActivity)?.finish()
    }

    private fun getChatActivity(): Activity? {
        val activityList: MutableList<Activity> = ActivityUtils.getActivityList().toMutableList()
        activityList.removeAt(0)
        if (activityList.size > 0) {
            activityList.forEach {
                if (it is ImChatActivity) {
                    return it
                }
            }
            return null
        } else {
            return null
        }
    }

    override fun initStatusBar() {
        mBinding.root.fitsSystemWindows = true
        StatusBarUtil.setWindowStatusBarColor(mContext, cn.yanhu.baselib.R.color.base_bg_gray)
        setStatusBarStyle(false)
    }


    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        imChatFrg.onNewIntent(intent)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (imChatFrg.chatFragment == null) {
            return handlerOtherTypeDispatchTouchEvent(event)
        }
        val leftTop = intArrayOf(0, 0)
        //获取输入框当前的location位置
        imChatFrg.chatFragment.customEaseChatPrimaryMenu!!.buttonSend.getLocationInWindow(leftTop)
        val left = leftTop[0]
        val top = leftTop[1]
        val bottom: Int =
            top + imChatFrg.chatFragment.customEaseChatPrimaryMenu!!.buttonSend.getHeight()
        val right: Int =
            left + imChatFrg.chatFragment.customEaseChatPrimaryMenu!!.buttonSend.getWidth()
        val x = event.x.toInt()
        val y = event.y.toInt()
        return if (x in (left + 1)..<right && y > top && y < bottom) {
            // 点击的是发送按钮，保留点击EditText的事件
            // 必不可少，否则所有的组件都不会有TouchEvent了
            if (window.superDispatchTouchEvent(event)) {
                true
            } else onTouchEvent(event)
        } else handlerOtherTypeDispatchTouchEvent(event)
    }


    private fun handlerOtherTypeDispatchTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (isShouldHideInput(v, event)) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                if (v != null) {
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
            return super.dispatchTouchEvent(event)
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return if (window.superDispatchTouchEvent(event)) {
            true
        } else onTouchEvent(event)
    }

    private fun isShouldHideInput(v: View?, event: MotionEvent): Boolean {
        if (v is EditText) {
            val leftTop = intArrayOf(0, 0)
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop)
            val left = leftTop[0]
            val top = leftTop[1]
            val bottom = top + v.getHeight()
            val right = left + v.getWidth()
            return if (event.x > left && event.x < right && event.y > top && event.y < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                false
            } else {
                //使EditText触发一次失去焦点事件
                v.setFocusable(false)
                v.setFocusableInTouchMode(true)
                true
            }
        }
        return false
    }

    companion object{
        fun getImChatIntent(mContext:Context,conversationId:String):Intent{
            val intent = Intent(mContext, ImChatActivity::class.java)
            intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, conversationId)
            intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, 1)
            val emUserInfo = ChatUserInfoManager.getUserInfo(conversationId)
            intent.putExtra(IntentKeyConfig.USERINFO, GsonUtils.toJson(emUserInfo))
            intent.putExtra(
                "title", if (emUserInfo != null) emUserInfo.nickName else conversationId
            )
            return intent
        }
        fun lunch(mContext:Context,conversationId:String){
            val imChatIntent = getImChatIntent(mContext, conversationId)
            mContext.startActivity(imChatIntent)
        }
    }
}