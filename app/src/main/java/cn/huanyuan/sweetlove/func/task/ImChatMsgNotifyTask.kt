package cn.huanyuan.sweetlove.func.task

import cn.jiguang.verifysdk.CtLoginActivity
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.pop.ImChatMsgPop
import cn.yanhu.commonres.bean.AppMsgNotifyInfo
import cn.yanhu.commonres.task.BaseAppNotifyQueueTask
import cn.yanhu.imchat.ui.chat.ImChatActivity
import com.blankj.utilcode.util.ActivityUtils
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback

/**
 * @author: zhengjun
 * created: 2023/5/29
 * desc:收到私聊消息
 */
class ImChatMsgNotifyTask(appMsgNotifyInfo: AppMsgNotifyInfo) : BaseAppNotifyQueueTask(appMsgNotifyInfo) {
    override fun doTask() {
        if (AgoraManager.isVideoCall){
            return
        }
        val topActivity = ActivityUtils.getTopActivity()
        if (topActivity is CtLoginActivity){
            doNextTask()
            return
        }
        if (topActivity is ImChatActivity && topActivity.conversationId == appMsgNotifyInfo.userId) {
            //如果当前正在跟此用户聊天的界面 则不显示对方查看我发送的消息通知 显示队列中的下一个通知
            doNextTask()
        } else {
            currentPop = ImChatMsgPop.showDialog(topActivity, appMsgNotifyInfo, object : SimpleCallback() {
                override fun onDismiss(popupView: BasePopupView?) {
                    doNextTask()
                }
            })
        }
    }

    override fun getTaskName(): String {
        return appMsgNotifyInfo.showTimeMills.toString()
    }
}