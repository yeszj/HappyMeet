package cn.huanyuan.happymeet.bean

/**
 * @author: zhengjun
 * created: 2024/3/15
 * desc:
 */
data class TaskResponse(
    var newbieTasks: MutableList<TaskInfo>,
    val dailyTasks: MutableList<TaskInfo>
)