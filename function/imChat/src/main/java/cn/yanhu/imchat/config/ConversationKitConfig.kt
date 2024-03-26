package cn.yanhu.imchat.config

import android.graphics.drawable.ColorDrawable
import cn.yanhu.baselib.utils.CommonUtils
import com.netease.yunxin.kit.conversationkit.ui.ConversationKitClient
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConfig

/**
 * @author: zhengjun
 * created: 2024/2/1
 * desc:
 */
object ConversationKitConfig {
    fun initConfig(){
        val conversationUIConfig = ConversationUIConfig()
        conversationUIConfig.showTitleBar = false
        conversationUIConfig.itemTitleColor = CommonUtils.getColor(cn.yanhu.baselib.R.color.fontTextColor)
        conversationUIConfig.itemContentColor = CommonUtils.getColor(cn.yanhu.baselib.R.color.fontGrayColor)
        conversationUIConfig.itemDateColor =  CommonUtils.getColor(cn.yanhu.baselib.R.color.fontGrayColor)
        conversationUIConfig.itemTitleSize = 15
        conversationUIConfig.itemContentSize = 13
        conversationUIConfig.itemDateSize = 10
        conversationUIConfig.itemBackground =  ColorDrawable(CommonUtils.getColor(cn.yanhu.baselib.R.color.transparent))
        conversationUIConfig.itemStickTopBackground = ColorDrawable(CommonUtils.getColor(cn.yanhu.baselib.R.color.transparent))
        ConversationKitClient.setConversationUIConfig(conversationUIConfig)
    }
}