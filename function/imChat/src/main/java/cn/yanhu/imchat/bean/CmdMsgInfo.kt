package cn.yanhu.imchat.bean

/**
 * @author: zhengjun
 * created: 2024/7/4
 * desc:
 */
data class CmdMsgInfo(val type: String, val msg: String) {
    companion object {
        const val MSG_TYPE_CALL = "call"
    }
}