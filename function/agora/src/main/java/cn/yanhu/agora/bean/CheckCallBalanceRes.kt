package cn.yanhu.agora.bean

/**
 * @author: zhengjun
 * created: 2024/8/20
 * desc:
 */
data class CheckCallBalanceRes(
    var ifBalanceEnough: Boolean,
    val ifUserVideoCard: Boolean,
    val rechargeInfo: CallRechargeInfo
) {
    data class CallRechargeInfo(val id: String, val rechargeMoney: String, val callSecond: String)
}