package cn.yanhu.commonres.bean.response

import cn.yanhu.commonres.bean.RoseRechargeBean

/**
 * @author: zhengjun
 * created: 2024/3/8
 * desc:
 */
data class RoseRechargeResponse(
    val roseBalance: String,
    val isFirstRecharge: Boolean,
    val rechargeAgreement:String,
    val list: MutableList<RoseRechargeBean>
)