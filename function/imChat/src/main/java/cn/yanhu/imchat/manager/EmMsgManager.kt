package cn.yanhu.imchat.manager

import android.annotation.SuppressLint
import android.text.TextUtils
import cn.yanhu.baselib.utils.DateUtils
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.ImMessageParamsConfig
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.bean.request.RewardRequest
import cn.yanhu.imchat.custom.chat.CustomEaseChatLayout
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.TimeUtils
import com.hyphenate.EMCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMCmdMessageBody
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMConversation.EMConversationType
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.chat.EMMessage
import com.jeremyliao.liveeventbus.LiveEventBus
import org.json.JSONObject


/**
 * @author: zhengjun
 * created: 2023/12/1
 * desc:
 */
object EmMsgManager {

    @JvmStatic
    @SuppressLint("CheckResult")
    fun receiveChatReward(rewardRequest: RewardRequest) {
        request(
            { imChatRxApi.chatReward(rewardRequest) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {

                }
            },
            false
        )
    }

    fun showSystemMsg(str: String, url: String, introduction: String, toUserId: String) {
        val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(ChatConstant.MSG_ALERT)
        val params: MutableMap<String, String> = HashMap()
        params["icon"] = url
        params["content"] = str
        params["introduction"] = introduction
        body.params = params
        message.body = body
        message.to = toUserId
        message.setStatus(EMMessage.Status.SUCCESS)
        EMClient.getInstance().chatManager().saveMessage(message)
    }

    @JvmStatic
    fun sendCmdMessagePeople(toUid: String, action: Int, params: Map<String, Any>?) {
        sendCmdMessagePeople(toUid, action, params, EMMessage.ChatType.Chat)
    }

    @JvmStatic
    fun sendCmdMessagePeople(
        toUid: String,
        action: Int,
        params: Map<String, Any>?,
        onlineOnly: Boolean = true
    ) {
        sendCmdMessagePeople(toUid, action, params, EMMessage.ChatType.Chat, onlineOnly)
    }


    @SuppressLint("SuspiciousIndentation")
    @JvmStatic
    fun sendCmdMessagePeople(
        toUid: String,
        action: Int,
        params: Map<String, Any>?,
        chatType: EMMessage.ChatType = EMMessage.ChatType.Chat,
        onlineOnly: Boolean = true
    ) {
        val cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD)
        cmdMsg.chatType = chatType
        params?.apply {
            this.mapKeys {
                when (it.value) {
                    is Boolean -> {
                        cmdMsg.setAttribute(it.key, it.value as Boolean)
                    }

                    is String -> {
                        cmdMsg.setAttribute(it.key, it.value as String)
                    }

                    is Int -> {
                        cmdMsg.setAttribute(it.key, it.value as Int)

                    }
                }
            }
        }

        cmdMsg.setAttribute("source", action)

        // 发送给特定用户。
        cmdMsg.to = toUid
        val cmdBody = EMCmdMessageBody("action")
        cmdMsg.deliverOnlineOnly(onlineOnly)
        cmdMsg.addBody(cmdBody)
        cmdMsg.setMessageStatusCallback(object : EMCallBack {
            override fun onSuccess() {
            }

            override fun onError(code: Int, error: String) {
            }
        })
        // 发送消息
        EMClient.getInstance().chatManager().sendMessage(cmdMsg)
    }

    @JvmStatic
    fun sendCmdMessagePeople(
        toUid: String,
        content: String,
        action: Int,
        onlineOnly: Boolean = true
    ) {
        val map: MutableMap<String, String> = java.util.HashMap()
        map[ChatConstant.CUSTOM_DATA] = content
        sendCmdMessagePeople(toUid, action, map, onlineOnly)
    }

    @JvmStatic
    fun sendCmdMessageToChatRoom(
        toUid: String,
        content: String,
        action: Int,
        onlineOnly: Boolean = true
    ) {
        val map: MutableMap<String, String> = java.util.HashMap()
        map[ChatConstant.CUSTOM_DATA] = content
        sendCmdMessagePeople(toUid, action, map, EMMessage.ChatType.ChatRoom, onlineOnly)
    }



    @JvmStatic
    fun sendTextMsg(
        content: String,
        toChatRoomId: String,
        chatType: EMMessage.ChatType = EMMessage.ChatType.GroupChat
    ) {
        //创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，后文皆是如此
        val message = EMMessage.createTextSendMessage(content, toChatRoomId)
        message.chatType = chatType
        //发送消息
        EMClient.getInstance().chatManager().sendMessage(message)
    }

    @JvmStatic
    fun deleteChatMsg(conversationId: String, day: Int, callBack: EMCallBack?) {
        val startTime = DateUtils.getYestodyStr(day, "yyyy-MM-dd")
        val timeMillis =
            TimeUtils.date2Millis(TimeUtils.string2Date(startTime, "yyyy-MM-dd"))
        val conversation =
            EMClient.getInstance().chatManager().getConversation(conversationId) ?: return
        var currentBack = callBack
        if (currentBack == null) {
            currentBack = object : EMCallBack {
                override fun onSuccess() {
                }

                override fun onError(code: Int, error: String?) {
                }

            }
        }
        conversation.removeMessagesFromServer(timeMillis, currentBack)
    }

    @JvmStatic
    fun deleteMsg(msgId: String, conversationId: String, callBack: EMCallBack?) {
        if (TextUtils.isEmpty(msgId)) {
            return
        }
        // 删除指定会话中指定的一条历史消息。
        val conversation =
            EMClient.getInstance().chatManager().getConversation(conversationId) ?: return
        conversation.removeMessage(msgId)
        conversation.removeMessagesFromServer(mutableListOf(msgId), callBack)
    }


    @JvmStatic
    fun updateMsg(message: EMMessage) {
        val conversation =
            EMClient.getInstance().chatManager().getConversation(message.conversationId()) ?: return
        conversation.updateMessage(message)
    }


    /**
     * 好友申请
     */
    @JvmStatic
    fun saveApplyFriend(
        conversationId: String,
        fromNickName: String,
        targetName: String,
        isApplySuccess: String
    ) {
        val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(ChatConstant.MSG_ADD_FRIEND)
        val params: MutableMap<String, String> = java.util.HashMap()
        params["isApplySuccess"] = isApplySuccess
        params["fromNickName"] = fromNickName
        params["targetNickName"] = targetName
        body.params = params
        message.body = body
        message.setAttribute("isApplySuccess", isApplySuccess)
        message.to = conversationId
        message.setStatus(EMMessage.Status.SUCCESS)
        EMClient.getInstance().chatManager().saveMessage(message)
    }

    @JvmStatic
    fun sendAlert(
        content: String,
        msgType: String,
        iconUrl: String,
        conversationId: String,
        introduction: String = "",
        event: String
    ) {
        val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(event)
        val params: MutableMap<String, String> = java.util.HashMap()
        params["icon"] = iconUrl
        params["content"] = content
        params["introduction"] = introduction
        params["msgType"] = msgType
        body.params = params
        message.body = body
        message.to = conversationId
        message.setStatus(EMMessage.Status.SUCCESS)
        EMClient.getInstance().chatManager().saveMessage(message)
    }

    fun sendRightIconAlert(
        str: String,
        url: String,
        conversationId: String,
        type: String,
        isHide: Int
    ) {
        val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(type)
        val params: MutableMap<String, String> = java.util.HashMap()
        params["rightIcon"] = url
        params["content"] = str
        body.params = params
        message.body = body
        message.to = conversationId
        message.setStatus(EMMessage.Status.SUCCESS)
        EMClient.getInstance().chatManager().saveMessage(message)
    }

    /**
     * 发送自定义消息
     */
    @JvmStatic
    fun sendCustomMsg(
        conversationId: String,
        event: String,
        content: String
    ) {
        val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(event)
        message.setAttribute(ChatConstant.CUSTOM_DATA, content)
        message.body = body
        message.to = conversationId
        message.setStatus(EMMessage.Status.INPROGRESS)
        EMClient.getInstance().chatManager().saveMessage(message)
        EMClient.getInstance().chatManager().sendMessage(message)
        LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_CHAT_LIST).post(true)
    }

    @JvmStatic
    fun updateInviteGiftMessage(inviteSendGiftMsgId: String) {
        val message = EMClient.getInstance().chatManager().getMessage(inviteSendGiftMsgId)
        message.setAttribute(ChatConstant.HAS_SEND_GIFT, true)
        EMClient.getInstance().chatManager().updateMessage(message)
    }


    @JvmStatic
    fun saveAlertMsg(chatUserId: String, params: MutableMap<String, String>) {
        val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(ChatConstant.MSG_ALERT)
        body.params = params
        message.body = body
        message.to = chatUserId
        message.setStatus(EMMessage.Status.SUCCESS)
        EMClient.getInstance().chatManager().saveMessage(message)
        LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_CHAT_LIST).post(true)
    }

    @JvmStatic
    fun saveCommonAlertMsg(jsonObject: JSONObject) {
        val params = HashMap<String, String>()
        params[ImMessageParamsConfig.KEY_PAGE_URL] =
            jsonObject.optString(ImMessageParamsConfig.KEY_PAGE_URL)
        params[ImMessageParamsConfig.KEY_CONTENT] =
            jsonObject.optString(ImMessageParamsConfig.KEY_CONTENT)
        val btnValue = jsonObject.optString(ImMessageParamsConfig.KEY_BTN_VALUE)
        if (!TextUtils.isEmpty(btnValue)) {
            params[ImMessageParamsConfig.KEY_BTN_VALUE] =
                jsonObject.optString(ImMessageParamsConfig.KEY_BTN_VALUE)
        }
        val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(ChatConstant.MSG_ALERT)
        body.params = params
        message.body = body
        message.to = jsonObject.optString(ImMessageParamsConfig.KEY_OTHER_USERID)
        message.setStatus(EMMessage.Status.SUCCESS)
        EMClient.getInstance().chatManager().saveMessage(message)
        LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_CHAT_LIST).post(true)
    }

    @JvmStatic
    fun saveCustomMsg(chatUserId: String, content: String, event: String) {
        val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(event)
        message.setAttribute(ChatConstant.CUSTOM_MSG, content)
        message.body = body
        message.to = chatUserId
        message.setStatus(EMMessage.Status.SUCCESS)
        EMClient.getInstance().chatManager().saveMessage(message)
        LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_CHAT_LIST).post(true)
    }

    @JvmStatic
    fun saveLoveMsg(chatUserId: String, params: MutableMap<String, String>, rewardGold: Int) {
        val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(ChatConstant.MSG_QIANXIAN)
        body.params = params
        message.body = body
        message.to = chatUserId
        message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_STATUS, 0)
        message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_SUM, rewardGold)
        message.setStatus(EMMessage.Status.SUCCESS)
        EMClient.getInstance().chatManager().saveMessage(message)
    }


    @JvmStatic
    fun isEditSendMessage(lastMessage: EMMessage): Boolean {
        val type: EMMessage.Type = lastMessage.type
        if (type == EMMessage.Type.CUSTOM) {
            val messageBody = lastMessage.body as EMCustomMessageBody
            val event = messageBody.event()
            if (event == ChatConstant.MSG_CUSTOM_EMOJI || event == ChatConstant.MSG_CUSTOM_GIF_EMOJI) {
                return true
            }
        } else {
            return true
        }
        return false
    }

    /**
     * 判断消息是否是缘分牵线
     */
    @JvmStatic
    fun isFateMessage(lastMessage: EMMessage): Boolean {
        if (lastMessage.type == EMMessage.Type.CUSTOM) {
            val messageBody2 = lastMessage.body as EMCustomMessageBody
            val event2 = messageBody2.event()
            if (ChatConstant.MSG_QIANXIAN == event2) {
                val params = messageBody2.params
                val content = params["content"]
                if (!TextUtils.isEmpty(content) && content?.startsWith(
                        "缘分牵线"
                    ) == true
                ) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 判断消息是否是牵线消息 包含为爱牵线和缘分牵线
     */
    @JvmStatic
    fun isQianXianMessage(lastMessage: EMMessage): Boolean {
        var loveString = lastMessage.getIntAttribute("loveString", -1)
        if (lastMessage.type == EMMessage.Type.CUSTOM) {
            val messageBody2 = lastMessage.body as EMCustomMessageBody
            val event2 = messageBody2.event()
            if (ChatConstant.MSG_QIANXIAN == event2) {
                val params = messageBody2.params
                val content = params["content"]
                if (!TextUtils.isEmpty(content) && (content!!.startsWith("为爱牵线") || content.startsWith(
                        "缘分牵线"
                    ))
                ) {
                    loveString = 1
                }
            }
        }
        return loveString == 1
    }

    /**
     * 判断消息是否是为爱牵线消息
     */
    @JvmStatic
    fun isLoveMessage(lastMessage: EMMessage): Boolean {
        var loveString = lastMessage.getIntAttribute("loveString", -1)
        if (lastMessage.type == EMMessage.Type.CUSTOM) {
            val messageBody2 = lastMessage.body as EMCustomMessageBody
            val event2 = messageBody2.event()
            if (ChatConstant.MSG_QIANXIAN == event2) {
                val params = messageBody2.params
                val content = params["content"]
                if (!TextUtils.isEmpty(content) && (content!!.startsWith("为爱牵线"))
                ) {
                    loveString = 1
                }
            }
        }
        return loveString == 1
    }

    @JvmStatic
    fun isLoveSendMessage(lastMessage: EMMessage): Boolean {
        val loveString = lastMessage.getIntAttribute("loveString", -1)
        return loveString == 1
    }

    /**
     * 发送送礼物消息
     */
    @JvmStatic
    fun sendGiftMessage(bean: GiftInfo, targetUserId: String, chatLayout: CustomEaseChatLayout?) {
        val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(ChatConstant.MSG_GIFT)
        val params: MutableMap<String, String> = java.util.HashMap()
        params["giftName"] = bean.name
        params["giftIcon"] = bean.giftIcon
        params["giftSvga"] = bean.svga
        params["giftType"] = "0"
        body.params = params
        message.body = body
        message.to = targetUserId
        if (chatLayout == null) {
            EMClient.getInstance().chatManager().sendMessage(message)
        } else {
            chatLayout.sendMessage(message)
        }
        LiveDataEventManager.sendLiveDataMessage("sendGift", bean.svga)
    }

    fun sendApplyFriend(
        conversationId: String?,
        fromNickName: String,
        targetNickName: String
    ) {
        val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(ChatConstant.MSG_ADD_FRIEND)
        val params: MutableMap<String, String> = java.util.HashMap()
        params["isApplySuccess"] = "1"
        params["fromNickName"] = fromNickName
        params["targetNickName"] = targetNickName
        body.params = params
        message.body = body
        message.setAttribute("isApplySuccess", 1)
        message.to = conversationId
        message.setStatus(EMMessage.Status.SUCCESS)
        EMClient.getInstance().chatManager().sendMessage(message)
    }

    fun getPrivateChatUnreadCount(): Int {
        return try {
            val conversations: List<EMConversation>? =
                getConversations(EMConversationType.Chat)
            if (conversations.isNullOrEmpty()) {
                return 0
            }
            var unreadCount = 0
            for (conversation in conversations) {
                 conversation.lastMessage ?: continue
                if (conversation.unreadMsgCount > 0) {
                    unreadCount += conversation.unreadMsgCount
                }
            }
            unreadCount
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

   private fun getConversations(chatType: EMConversationType?): List<EMConversation>? {
        return try {
            if (EMClient.getInstance().chatManager() == null) {
                return null
            }
            if (EMClient.getInstance().chatManager().getConversationsByType(chatType).isEmpty()) {
                null
            } else {
                EMClient.getInstance().chatManager().getConversationsByType(chatType)
            }
        } catch (e: java.lang.Exception) {
            null
        }
    }
}