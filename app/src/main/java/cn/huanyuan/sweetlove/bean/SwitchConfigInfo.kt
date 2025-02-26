package cn.huanyuan.sweetlove.bean

/**
 * @author: zhengjun
 * created: 2024/4/9
 * desc:
 */
data class SwitchConfigInfo(
    val title: String,
    val desc: String,
    var status: Int,
    val key: String,
    val subTitle: String,
    val statusDesc: String
)