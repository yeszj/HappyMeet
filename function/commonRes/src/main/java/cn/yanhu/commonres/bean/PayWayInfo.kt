package cn.yanhu.commonres.bean

/**
 * @author: zhengjun
 * created: 2024/3/7
 * desc:
 */
data class PayWayInfo(
    var payName: String,
    val payType: Int,
    val iconId: Int,
    val icon: String?=""
){
    companion object{
        const val TYPE_ALIPAY = 0
        const val TYPE_WXPAY = 1
        const val TYPE_BANK = 2
    }
}