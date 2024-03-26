package cn.huanyuan.happymeet.bean


/**
 * @author: zhengjun
 * created: 2024/3/18
 * desc:
 */
data class UserLevelResponse(
    val portrait: String,
    val userLevel: Int,
    val wealthValue: Int,
    val needWealthValue: Int,
    val progressTotal: Int,
    val progress: Int,
    val totalPrivilegeCount:Int,
    val hasLockCount:Int,
    val privilegeList:MutableList<LevelPrivilegeInfo>
)