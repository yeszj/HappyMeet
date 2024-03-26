package cn.yanhu.agora.bean

/**
 * @author: zhengjun
 * created: 2024/3/20
 * desc:
 */
data class RoomConfigInfo(
    val bannerUrl: String,
    val typeList: MutableList<RoomTypeInfo>,
    val name: String,
    val welcomeMsg: String,
    val rechargeAgreement:String,
    val timePriceList: MutableList<LiveTimePriceInfo>
)