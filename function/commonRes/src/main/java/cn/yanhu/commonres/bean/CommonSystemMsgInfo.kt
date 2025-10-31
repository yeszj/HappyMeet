package cn.yanhu.commonres.bean

/**
 * @author: zhengjun
 * created: 2025/10/17
 * desc:
 */
data class CommonSystemMsgInfo(
    val sendShowInfo: SystemMsgInfo,
    val receiveShowInfo: SystemMsgInfo?
){
    data class SystemMsgInfo(
        val title: String,
        val url: String,
        val content: String,
        val clickContent: String
    )
}