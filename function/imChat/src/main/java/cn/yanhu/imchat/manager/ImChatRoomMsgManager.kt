package cn.yanhu.imchat.manager

import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder
import com.netease.nimlib.sdk.chatroom.ChatRoomService
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment


/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
object ImChatRoomMsgManager {

    fun sendCustomChatRoomMsg(roomId: String, msgAttachment: MsgAttachment) {
        val message = ChatRoomMessageBuilder.createChatRoomCustomMessage(roomId, msgAttachment)
        NIMClient.getService(ChatRoomService::class.java).sendMessage(message, false)
    }

    fun sendTipsMessage(roomId: String, content: String) {
        val createTipMessage = ChatRoomMessageBuilder.createTipMessage(roomId)
        createTipMessage.content = content
        NIMClient.getService(ChatRoomService::class.java).sendMessage(createTipMessage, false)
    }
}