package cn.huanyuan.sweetlove.bean

/**
 * @author: zhengjun
 * created: 2024/3/14
 * desc:
 */
data class WalletInfo(
    val balance: String,
    val totalIncome: String,
    val monthIncome: String,
    val inviteIncome: String,
    val totalInviteCount: String,
    val monthInviteCount: String,
    val userId: String,
    val invitePortrait: String?,
    val inviteNickName: String?
)