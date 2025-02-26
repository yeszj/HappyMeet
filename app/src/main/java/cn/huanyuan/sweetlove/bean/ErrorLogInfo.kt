package cn.huanyuan.sweetlove.bean

/**
 * @author: zhengjun
 * created: 2025/2/20
 * desc:
 */
data class ErrorLogInfo(
    var typeDesc: String="",
    var errorTime: String="",
    var description: String="",
    var url: String="",
    var extInfo: String=""
)