package cn.yanhu.imchat.manager

import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomNotification
import com.netease.nimlib.sdk.msg.model.CustomNotificationConfig


/**
 * @author: zhengjun
 * created: 2024/2/2
 * desc:
 */
object ImMsgManager {
    const val TYPE_SEND_GIFT = "sendGift"
    /**
     * 获取总未读消息数
     */
    fun getUnReadMsgCount(): Int {
        return NIMClient.getService(MsgService::class.java).totalUnreadCount
    }

    /**
     * 发送通知透传消息
     * @param json 实体类继承BaseCustomMsgType 转json
     */
    fun sendCustomNotificationMsg(receiverId:String,json:String){
        // 构造自定义通知，指定接收者
        val notification = CustomNotification()
        notification.sessionId = receiverId
        notification.sessionType = SessionTypeEnum.P2P
        notification.content = json
        notification.isSendToOnlineUserOnly = true
        val config = CustomNotificationConfig()
        config.enablePush = false
        config.enableUnreadCount = false
        notification.config = config
        NIMClient.getService(MsgService::class.java).sendCustomNotification(notification)
    }


}