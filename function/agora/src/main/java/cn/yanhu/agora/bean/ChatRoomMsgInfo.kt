package cn.yanhu.agora.bean

/**
 * @author: zhengjun
 * created: 2024/4/2
 * desc:
 */
data class ChatRoomMsgInfo(val type: Int, val content: String){
    companion object{
        const val TYPE_NOTICE = 0
        const val TYPE_TEXT_MSG = 1
        const val TYPE_WELCOME = 2
        const val TYPE_GIFT = 3
    }
}