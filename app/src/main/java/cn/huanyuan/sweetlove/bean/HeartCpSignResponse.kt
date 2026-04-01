package cn.huanyuan.sweetlove.bean

/**
 * @author: zhengjun
 * created: 2026/3/25
 * desc:
 */
data class HeartCpSignResponse(
    val myPortrait: String,
    val cpPortrait: String,
    val cpUserId: String,
    val intimacy: String,
    val consecutiveDays: String,
    val cpRoomId: String,
    val days: MutableList<HeartCpSignInfo>
)