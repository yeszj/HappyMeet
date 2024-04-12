package cn.yanhu.imchat.config

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.imchat.custom.message.CustomChatViewHolderFactory
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient
import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties


/**
 * @author: zhengjun
 * created: 2024/2/2
 * desc:
 */
object ChatUiConfig {
    fun initConfig() {
        var chatUIConfig = ChatKitClient.getChatUIConfig()
        if (chatUIConfig == null) {
            chatUIConfig = ChatUIConfig()
        }
        val instance = CustomChatViewHolderFactory.getInstance()
        chatUIConfig.chatFactory = instance
        val messageProperties = MessageProperties()
        messageProperties.userNickTextSize = CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.sp_13)
        messageProperties.selfMessageRes =
            com.netease.yunxin.kit.chatkit.ui.R.drawable.chat_message_self_bg
        messageProperties.receiveMessageRes =
            com.netease.yunxin.kit.chatkit.ui.R.drawable.chat_message_other_bg
        messageProperties.showTitleBar = false
        messageProperties.chatViewBackground =
            ColorDrawable(Color.parseColor("#ffffff"))
        messageProperties.avatarCornerRadius =
            CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_30).toFloat()
        chatUIConfig.messageProperties = messageProperties
        ChatKitClient.setChatUIConfig(chatUIConfig)
    }
}