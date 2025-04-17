package cn.huanyuan.sweetlove.bean

import cn.yanhu.commonres.bean.BaseUserInfo

/**
 * @author: zhengjun
 * created: 2024/3/18
 * desc:
 */
data class WalletRecordInfo(
    val title: String,
    val userInfo: BaseUserInfo?,
    val time: String,
    val num: String,
    val desc: String,
    val list: MutableList<WalletRecordInfo>,
    val withDrawInfo: WithDrawInfo,
) {
    data class WithDrawInfo(
        val status: Int,//0提现中 1提现成功 2提现被驳回
        val balanceDesc: String //余额描述 当前余额：122.24元
    ){
        fun getStatusDesc():String {
            return when (status) {
                1 -> "提现成功"
                2 -> "提现被驳回"
                else -> "提现中"
            }
        }
    }

    fun isIncome(): Boolean {
        return num.startsWith("+")
    }
}