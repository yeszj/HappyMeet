package cn.yanhu.imchat.custom.message.push

import android.content.Intent
import android.text.TextUtils
import com.blankj.utilcode.util.ActivityUtils
import com.hyphenate.easeui.constants.EaseConstant
import cn.yanhu.imchat.ui.chat.ImChatActivity

/**
 * @author: zhengjun
 * created: 2024/4/28
 * desc:
 */
object PushManager {
    @JvmStatic
    fun clickOfflinePush(sendUserId: String?, groupId: String?) {
        val topActivity = ActivityUtils.getTopActivity()
        if (TextUtils.isEmpty(groupId)) {
            if (TextUtils.isEmpty(sendUserId)){
                return
            }
            val clickIntent = Intent(topActivity, ImChatActivity::class.java)
            clickIntent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, sendUserId)
            clickIntent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, 1)
            clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            topActivity.startActivity(clickIntent)
        } else {
          //  lunch(topActivity as BaseAppActivity, groupId!!,false)
        }
    }
}