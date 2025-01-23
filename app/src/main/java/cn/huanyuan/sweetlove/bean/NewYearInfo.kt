package cn.huanyuan.sweetlove.bean

import cn.yanhu.commonres.bean.ShareInfo

/**
 * @author: zhengjun
 * created: 2025/1/16
 * desc:
 */
data class NewYearInfo(
    val remainSeconds: Int,
    val sumRoseNum: String,
    val devideDesc: String,
    val shareDTO: ShareInfo,
    val status:Int // -1未开始 0下一场 -2已结束
)