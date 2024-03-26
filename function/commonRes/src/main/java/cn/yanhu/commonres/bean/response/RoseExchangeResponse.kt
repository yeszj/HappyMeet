package cn.yanhu.commonres.bean.response

import cn.yanhu.commonres.bean.MineMenuBean
import cn.yanhu.commonres.bean.RoseRechargeBean

/**
 * @author: zhengjun
 * created: 2024/3/8
 * desc:
 */
data class RoseExchangeResponse(
    val balance: String,
    val list: MutableList<RoseRechargeBean>,
    val privileges: MutableList<MineMenuBean>
)