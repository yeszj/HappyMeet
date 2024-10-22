package cn.yanhu.imchat.bean

/**
 * @author: zhengjun
 * created: 2024/3/22
 * desc:
 */
data class GroupCenterTipMsgInfo(val iconUrl: String, val content: String, val type: Int) {
    companion object {
        //领取红包
        const val TYPE_RECEIVE_RED_PACKET = 1

        //红包过期
        const val TYPE_RED_PACKET_TIMEOUT = 2
    }
}