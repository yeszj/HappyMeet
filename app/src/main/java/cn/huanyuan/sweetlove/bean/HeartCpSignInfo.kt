package cn.huanyuan.sweetlove.bean

/**
 * @author: zhengjun
 * created: 2026/3/25
 * desc:
 */
data class HeartCpSignInfo(
    val date: String,
    val signStatus: Int, //0双方未签 / 1对方已签 / 2我已签 / 3双方已签
    val afterBind: Boolean,
    val rewardStatus: Int, // 0未领取 / 1可领取 / 2已领取
    val icon: String,
    val rewardDesc: String,
    val intimacyDiff: String
)