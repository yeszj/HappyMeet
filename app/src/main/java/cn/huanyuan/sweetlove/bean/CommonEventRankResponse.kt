package cn.huanyuan.sweetlove.bean

/**
 * @author: zhengjun
 * created: 2025/1/20
 * desc:
 */
data class CommonEventRankResponse(
    val leftButton: String,
    val rightButton: String,
    val bgImg:String,
    val topThreeIcon:TopThreeIcon,
    val itemBgImg:String,
    val list:MutableList<CommonEventRankInfo>,
    val myInfo:CommonEventRankInfo
){
    data class TopThreeIcon(val one:String,val two:String,val three:String)
}