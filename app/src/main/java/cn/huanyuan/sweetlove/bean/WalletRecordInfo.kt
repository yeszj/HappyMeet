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
    val withDrawInfo: WithDrawInfo?,
) {
    data class WithDrawInfo(
        val status: Int,//0提现中 1提现成功 2提现被驳回 3待确认
        val statusDesc: String,
        val balanceDesc: String, //余额描述 当前余额：122.24元
        val reason: String,
        val url: String
    )

    fun isIncome(): Boolean {
        return num.startsWith("+")
    }
}