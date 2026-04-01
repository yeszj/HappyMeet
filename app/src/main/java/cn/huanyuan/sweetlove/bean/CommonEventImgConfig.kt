package cn.huanyuan.sweetlove.bean

import cn.yanhu.commonres.bean.HeartCpUserInfo

/**
 * @author: zhengjun
 * created: 2025/1/20
 * desc:
 */
data class CommonEventImgConfig(
    val bgImg: String,
    val bgColor: BgColor,
    val ruleTag: String,
    val ruleUrl: String,
    val eventImgs: MutableList<String>,
    val eventName: String,
    val times:String,
    val copyrightColor: String,
    val rewardPool: RewardPool,
    val signInTag: String?="",
    val needSignIn: Boolean = false,
    val myCp: HeartCpUserInfo?=null
) {
    data class BgColor(val start: String, val end: String)
    data class RewardPool(
        val activeNumColor: BgColor,
        val bgImg: String,
        val descImg: String,
        val value: String,
        val cbgImg: String,
        val cdescImg:String
    )
}