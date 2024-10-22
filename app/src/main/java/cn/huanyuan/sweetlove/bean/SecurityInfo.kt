package cn.huanyuan.sweetlove.bean

import cn.yanhu.commonres.bean.MineMenuBean

/**
 * @author: zhengjun
 * created: 2024/4/9
 * desc:
 */
data class SecurityInfo(
    val serviceConfigs: MutableList<MineMenuBean>,
    val safetyReminderUrl: String,
    val safetyReminderImg: String,
    var violationUserIds:MutableList<String>?
)