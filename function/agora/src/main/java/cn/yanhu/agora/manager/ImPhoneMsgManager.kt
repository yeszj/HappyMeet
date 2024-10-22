package cn.yanhu.agora.manager

import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.bean.ChatCallResponseInfo
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.chat.EMMessage

/**
 * @author: zhengjun
 * created: 2024/8/14
 * desc:一对一通话自定义通话状态消息
 */
object ImPhoneMsgManager {

    @JvmStatic
    fun sendRefuseMsg(chatCallInfo: ChatCallResponseInfo?) {
        if (chatCallInfo == null) {
            return
        }
        val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(ChatConstant.MSG_PHONE)
        val params: MutableMap<String, String> = HashMap()
        params["content"] =
            "已拒绝" + if (chatCallInfo.chatType == 0) "语音通话" else "视频通话"
        params["chatType"] = chatCallInfo.chatType.toString()
        body.params = params
        message.body = body
        message.to = chatCallInfo.user.id.toString()
        EMClient.getInstance().chatManager().sendMessage(message)
    }


    @JvmStatic
    fun saveRefuseMsg(chatTYpe:Int,chatUserId:String) {
        val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(ChatConstant.MSG_PHONE)
        val params: MutableMap<String, String> = HashMap()
        params["content"] =
            "已拒绝" + if (chatTYpe == 0) "语音通话" else "视频通话"
        params["chatType"] = chatTYpe.toString()
        body.params = params
        message.body = body
        message.to = chatUserId
        EMClient.getInstance().chatManager().saveMessage(message)
    }

    @JvmStatic
    fun sendTimeOutMsg(chatType:Int,chatUserId:String) {
        val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(ChatConstant.MSG_PHONE)
        val params: MutableMap<String, String> = HashMap()
        params["content"] =
            "未应答"
        params["chatType"] = chatType.toString()
        body.params = params
        message.body = body
        message.to = chatUserId
        EMClient.getInstance().chatManager().sendMessage(message)
    }

    @JvmStatic
     fun sendCancelMsg(chatType:Int,chatUserId:String) {
        val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(ChatConstant.MSG_PHONE)
        val params: MutableMap<String, String> = HashMap()
        params["content"] =
            "已取消" + if (chatType == 0) "语音通话" else "视频通话"
        params["chatType"] = java.lang.String.valueOf(chatType)
        body.params = params
        message.body = body
        message.to = chatUserId
        EMClient.getInstance().chatManager().sendMessage(message)
    }

}