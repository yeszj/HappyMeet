package cn.huanyuan.happymeet.bean

import cn.yanhu.commonres.bean.FilterInfo

/**
 * @author: zhengjun
 * created: 2024/3/18
 * desc:
 */
data class WalletRecordResponse(
    val list: MutableList<WalletRecordInfo>,
    val filterList: MutableList<FilterInfo>
)