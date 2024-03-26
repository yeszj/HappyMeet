package cn.yanhu.baselib.queue

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import java.util.*
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author: zhengjun
 * created: 2023/5/29
 * desc:队列任务管理 外部使用
 */
class TaskQueueManagerImpl {
    companion object {
        const val TAG = "OptimusTaskManager"
        internal val cacheTaskNameList = mutableListOf<String>()

        var currRunningTask: IQueueTask? = null
    }

    private var channel = Channel<IQueueTask>(Channel.UNLIMITED)

    //使用SupervisorJob 的 coroutineScope, 异常不会取消父协程
    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val atomicInteger = AtomicInteger()
    private val deferred = PriorityBlockingQueue<Int>()

    private val handler = CoroutineExceptionHandler { _, exception ->
        Log.d(TAG, "handlerCatch -> $exception")
    }

    init {
        loop()
    }

    fun getRunningTask() = currRunningTask

    fun addTask(task: IQueueTask) {
        scope.launch(handler) { addTaskSuspend(task) }
    }

    private suspend fun addTaskSuspend(task: IQueueTask) {
        task.setDeferred(deferred)
        Log.d(TAG, "addTask name = " + task.getTaskName())
        if (checkChannelActive()) {
            sendTask(task)
        } else {
            reset()
            reSendTaskIfNeed() //重新发送缓存任务
            sendTask(task)     //最后发送当前任务
        }
    }

    private suspend fun sendTask(task: IQueueTask): Boolean {
        return runCatching {
            if (checkChannelActive()) {
                task.setSequence(atomicInteger.incrementAndGet())
                TaskQueueManager.addTask(task)
                channel.send(task)
                Log.d(TAG, "send task -> ${task.getTaskName()}")
                true
            } else {
                Log.d(TAG, "Channel is not Active，removeTask -> ${task.getTaskName()}")
                TaskQueueManager.removeTask(task)
                cacheTaskNameList.remove(task.getTaskName())
                return false
            }
        }.onFailure {
            Log.d(TAG, "addTaskCatch -> $it")
            TaskQueueManager.removeTask(task)
            cacheTaskNameList.remove(task.getTaskName())
        }.getOrElse { false }
    }

    private fun loop() {
        scope.launch(handler) {
            channel.consumeEach {
                tryToHandlerTask(it)
            }
        }
    }

    /**
     * 重置数据
     */
    private fun reset() {
        channel = Channel(Channel.BUFFERED)
        loop()
    }

    private suspend fun reSendTaskIfNeed() {
        runCatching {
            if (TaskQueueManager.getTaskQueueSize() > 0) {
                val list = Collections.synchronizedList(mutableListOf<IQueueTask>())
                TaskQueueManager.getTaskQueue().forEach { list.add(it) }
                TaskQueueManager.clearTaskQueue()
                list.forEach { sendTask(it) }
            }
        }
    }

    private suspend fun tryToHandlerTask(it: IQueueTask) {
        try {
            Log.d(TAG, "tryToHandlerTask -> ${it.getTaskName()}")
            currRunningTask = it
            withContext(Dispatchers.Main) { it.doTask() }
            if (it.getDuration() != 0L) {
                delay(it.getDuration())
                withContext(Dispatchers.Main) { it.finishTask() }
                currRunningTask = null

                Log.d(TAG, "tryToHandlerTask finish，removeTask -> ${it.getTaskName()}")
                TaskQueueManager.removeTask(it)
                cacheTaskNameList.remove(it.getTaskName())
            } else {
                withContext(Dispatchers.IO) {
                    deferred.take()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.d(TAG, "handlerTaskCatch -> $ex")
        }
    }

    /**
     * 检查管道是否激活
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun checkChannelActive(): Boolean {
        return !channel.isClosedForReceive && !channel.isClosedForSend
    }


    fun clear() {
        runCatching {
            deferred.add(1)
            channel.close()
            currRunningTask?.finishTask()
            cacheTaskNameList.clear()
            TaskQueueManager.clearTaskQueue()
            currRunningTask = null
        }
    }
}