package cn.huanyuan.happymeet.bean

/**
 * @author: zhengjun
 * created: 2024/3/14
 * desc:
 */
data class InviteRecordResponse(
    val totalIncome: String,
    val totalInviteCount: Int,
    val list: MutableList<InviteRecordInfo>
)