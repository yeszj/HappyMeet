package cn.yanhu.commonres.bean.request

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:
 */
data class DressUpRequest(
    val commodityId: Int,
    val commodityType: Int,
    val operation: Int, //0=卸下装扮；1=装备装扮
)