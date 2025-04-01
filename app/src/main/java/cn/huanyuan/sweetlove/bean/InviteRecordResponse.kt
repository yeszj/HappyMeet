package cn.huanyuan.sweetlove.bean

import cn.yanhu.commonres.bean.FilterInfo

/**
 * @author: zhengjun
 * created: 2024/3/14
 * desc:
 */
data class InviteRecordResponse(
    val totalIncome: String,
    val totalInviteCount: Int,
    val numDesc:String,
    val list: MutableList<InviteRecordInfo>,
    val filterList: MutableList<FilterInfo>

)