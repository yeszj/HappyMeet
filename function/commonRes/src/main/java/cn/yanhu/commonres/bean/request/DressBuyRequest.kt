package cn.yanhu.commonres.bean.request

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:
 */
data class DressBuyRequest(
    val commodityId: Int,
    val commodityType: Int,
    val priceId: Int,
    val friendUserId: String? = ""
)