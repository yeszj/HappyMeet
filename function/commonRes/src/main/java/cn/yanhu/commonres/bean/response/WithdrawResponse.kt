package cn.yanhu.commonres.bean.response

import cn.yanhu.commonres.bean.WithDrawInfo

/**
 * @author: zhengjun
 * created: 2024/3/8
 * desc:
 */
data class WithdrawResponse(
    val balance: String,
    val list: MutableList<WithDrawInfo>,
    val withdrawalAgreement: String,
    val withdrawalRule: String,
    val aliAccount: String?,
    val wxNickName: String?,
    val wxAvatar:String?,
    var realName: String
)