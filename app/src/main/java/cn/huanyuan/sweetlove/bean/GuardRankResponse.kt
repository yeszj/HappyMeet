package cn.huanyuan.sweetlove.bean


/**
 * @author: zhengjun
 * created: 2024/3/18
 * desc:
 */
data class GuardRankResponse (val guardUsers: MutableList<RankInfo>,val noGuardInfo:RankInfo,val myInfoRes:RankInfo)