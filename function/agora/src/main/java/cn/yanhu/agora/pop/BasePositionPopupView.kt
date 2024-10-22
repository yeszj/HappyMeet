package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import cn.yanhu.agora.ui.liveRoom.live.LiveRoomActivity
import cn.yanhu.commonres.bean.AppMsgNotifyInfo
import cn.yanhu.imchat.pop.ImChatDialog
import cn.yanhu.imchat.ui.chat.ImChatActivity
import com.blankj.utilcode.util.ActivityUtils
import com.hyphenate.easeui.constants.EaseConstant
import com.lxj.xpopup.animator.PopupAnimator
import com.lxj.xpopup.animator.TranslateAnimator
import com.lxj.xpopup.core.PositionPopupView
import com.lxj.xpopup.enums.PopupAnimation

/**
 * @author: zhengjun
 * created: 2024/5/17
 * desc:
 */
@SuppressLint("ViewConstructor")
open class BasePositionPopupView(context: Context,  val appMsgNotifyInfo: AppMsgNotifyInfo): PositionPopupView(context) {
     fun toPrivacyChat() {
        val intent = Intent(context, ImChatActivity::class.java)
        intent.putExtra(
            EaseConstant.EXTRA_CONVERSATION_ID,
            appMsgNotifyInfo.userId
        )
        intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, 1)
        intent.putExtra("title", appMsgNotifyInfo.nickName)
        val topActivity = ActivityUtils.getTopActivity()
        if (topActivity is LiveRoomActivity) {
            showChatDialog(intent)
        } else {
            context.startActivity(intent)
        }
    }

    private fun showChatDialog(intent: Intent) {
        val extras = intent.extras
        val chatListDialog = ImChatDialog(extras)
        chatListDialog.showNow((context as FragmentActivity).supportFragmentManager, "show_chat_im")
    }

    override fun getPopupAnimator(): PopupAnimator {
        return TranslateAnimator(
            popupContentView,
            animationDuration,
            PopupAnimation.TranslateFromTop
        )
    }

}