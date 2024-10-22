package cn.yanhu.imchat.manager

import com.hyphenate.chat.EMClient


/**
 * @author: zhengjun
 * created: 2024/2/2
 * desc:
 */
object ImMsgManager {
    const val TYPE_SEND_GIFT = "sendGift"
    const val MSG_CUSTOM_EMOJI = "msg_custom_emoji" //自定义小表情和文字混排消息

    /**
     * 获取总未读消息数
     */
    fun getUnReadMsgCount(): Int {
        return EMClient.getInstance().chatManager().unreadMessageCount
    }

    /**
     * 发送通知透传消息
     * @param json 实体类继承BaseCustomMsgType 转json
     */
    fun sendCustomNotificationMsg(receiverId:String,json:String){
        // 构造自定义通知，指定接收者
    }


}