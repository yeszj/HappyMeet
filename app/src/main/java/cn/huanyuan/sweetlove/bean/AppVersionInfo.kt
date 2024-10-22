package cn.huanyuan.sweetlove.bean

/**
 * @author: zhengjun
 * created: 2024/10/12
 * desc:
 */
data class AppVersionInfo(
    val title: String,
    var content: String,
    val versionNum: String,
    val pageUrl: String,
    val forceUpdates: Int
)