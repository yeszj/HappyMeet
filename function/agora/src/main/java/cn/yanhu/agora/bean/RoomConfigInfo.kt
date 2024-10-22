package cn.yanhu.agora.bean

import cn.yanhu.commonres.bean.BannerBean

/**
 * @author: zhengjun
 * created: 2024/3/20
 * desc:
 */
data class RoomConfigInfo(
    val banners: MutableList<BannerBean>,
    val types: MutableList<RoomTypeInfo>,
    val name: String,
    val welcomeMsg:String,
    val liveAgreement: String,
    val timePriceList: MutableList<LiveTimePriceInfo>
)