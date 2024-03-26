package cn.yanhu.baselib.queue

/**
 * @author: zhengjun
 * created: 2023/5/29
 * desc: 队列管理
 */
object TaskQueueManager  : ITaskQueueManager {

    private val taskMap = mutableMapOf<String, IQueueTask>()

    override fun addTask(task: IQueueTask): Boolean {
        return if (hasTask(task)) {
            false
        } else {
            taskMap[task.getTaskName()] = task
            true
        }
    }

    override fun hasTask(task: IQueueTask): Boolean {
        return hasTask(task.getTaskName())
    }

    override fun hasTask(taskName: String): Boolean {
        return taskMap.containsKey(taskName)
    }

    override fun removeTask(task: IQueueTask) {
        removeTask(task.getTaskName())
    }

    override fun removeTask(taskName: String) {
        taskMap.remove(taskName)
    }

    override fun getTaskQueueSize() = taskMap.size

    override fun getTaskQueue(): Collection<IQueueTask> {
        return taskMap.values
    }

    override fun clearTaskQueue() {
        taskMap.clear()
    }
}

interface ITaskQueueManager {
    fun addTask(task: IQueueTask): Boolean
    fun hasTask(task: IQueueTask): Boolean
    fun hasTask(taskName: String): Boolean
    fun removeTask(task: IQueueTask)
    fun removeTask(taskName: String)
    fun getTaskQueueSize(): Int
    fun getTaskQueue(): Collection<IQueueTask>
    fun clearTaskQueue()
}