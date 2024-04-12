package cn.huanyuan.happymeet.bean

/**
 * @author: zhengjun
 * created: 2024/3/18
 * desc:
 */
data class LevelPrivilegeInfo(
    val id: Int,
    val privilegeIcon: String,
    val privilegeTitle: String,
    val lockLevel: String,
    val lockDesc: String,
    val hasLock: Boolean,
    val privilegeDesc: String,
    val privilegeLevelAllStyleIcon: String,
    val privilegeShowExampleIcon: String
)