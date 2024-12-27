package cn.yanhu.agora.bean

import cn.yanhu.commonres.bean.BaseUserInfo

/**
 * @author: zhengjun
 * created: 2024/12/23
 * desc:
 */
data class LiveStatisticTotalInfo(
    val userInfo: BaseUserInfo,
    val myData: MutableList<StatisticInfo>,
    val apprenticeData: MutableList<StatisticInfo>
)