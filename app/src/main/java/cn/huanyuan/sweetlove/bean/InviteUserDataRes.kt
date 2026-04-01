package cn.huanyuan.sweetlove.bean

import cn.yanhu.agora.bean.StatisticInfo
import cn.yanhu.commonres.bean.BaseUserInfo

/**
 * @author: zhengjun
 * created: 2026/3/27
 * desc:
 */
data class InviteUserDataRes(val userInfo: InviteRecordInfo, val normalData: MutableList<StatisticInfo>,val monthData: MutableList<StatisticInfo>,val weekData: MutableList<StatisticInfo>)
