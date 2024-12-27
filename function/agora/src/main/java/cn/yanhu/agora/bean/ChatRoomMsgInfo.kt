package cn.yanhu.agora.bean

import cn.yanhu.commonres.bean.BaseUserInfo

/**
 * @author: zhengjun
 * created: 2024/4/2
 * desc:
 */
data class ChatRoomMsgInfo(val type: Int, val content: String,val sendUserInfo:BaseUserInfo?,val altUser:BaseUserInfo?=null){
    companion object{
        const val ITEM_NEW_ADD_TYPE = -1 //新增的消息 老版本不支持

        const val ITEM_SYSTEM_TYPE = 0 //系统消息

        const val ITEM_WELCOME_TYPE = 1 //欢迎消息

        const val ITEM_DEFAULT_TYPE = 3 //默认消息

        const val ITEM_GIFT_TYPE = 4 //礼物消息

        const val ITEM_EMOJI_TYPE = 5 //表情消息

    }
}