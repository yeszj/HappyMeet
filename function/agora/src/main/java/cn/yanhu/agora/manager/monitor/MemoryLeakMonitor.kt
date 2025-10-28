package cn.yanhu.agora.manager.monitor

import android.os.Handler
import android.os.Looper
import cn.yanhu.baselib.utils.ext.logComToFile
import java.util.WeakHashMap

/**
 * @author: zhengjun
 * created: 2025/10/27
 * desc:
 */
// MemoryLeakMonitor.kt
object MemoryLeakMonitor {
    private val watchedObjects = WeakHashMap<Any, String>()
    private val handler = Handler(Looper.getMainLooper())

    fun watchObject(obj: Any, tag: String) {
        watchedObjects[obj] = "$tag - ${System.currentTimeMillis()}"
    }

    fun checkLeaks() {
        val iterator = watchedObjects.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key == null) {
                iterator.remove()
            }
        }
        logComToFile("MemoryLeakMonitor", "Watched objects: ${watchedObjects.size}")
    }

    fun startMonitoring() {
        handler.postDelayed({
            checkLeaks()
            startMonitoring()
        }, 30000) // 每30秒检查一次
    }

    fun stopMonitoring() {
        handler.removeCallbacksAndMessages(null)
        watchedObjects.clear()
    }
}