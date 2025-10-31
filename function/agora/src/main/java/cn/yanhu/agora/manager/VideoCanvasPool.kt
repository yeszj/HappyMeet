package cn.yanhu.agora.manager

import android.view.View
import cn.yanhu.baselib.utils.ext.logComToFile
import io.agora.rtc2.Constants
import io.agora.rtc2.video.VideoCanvas
import java.util.Stack
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author: zhengjun
 * created: 2025/10/29
 * desc:
 */
// VideoCanvasPool.kt
object VideoCanvasPool {
    private const val MAX_POOL_SIZE = 10
    private val availableCanvases = Stack<VideoCanvas>()
    private val inUseCanvases = mutableMapOf<Int, VideoCanvas>() // uid -> VideoCanvas
    private val creationCount = AtomicInteger(0)

    /**
     * 获取VideoCanvas（复用或创建新的）
     */
    fun obtainVideoCanvas(uid: Int, surface: View? = null): VideoCanvas {
        // 如果已在使用的，直接返回
        inUseCanvases[uid]?.let {
            it.uid = uid
            it.view = surface
            return it
        }

        val canvas = if (availableCanvases.isNotEmpty()) {
            // 复用对象
            availableCanvases.pop().apply {
                this.uid = uid
                this.view = surface
                renderMode = Constants.RENDER_MODE_HIDDEN
            }
        } else {
            // 创建新对象
            creationCount.incrementAndGet()
            VideoCanvas(surface, Constants.RENDER_MODE_HIDDEN, uid).apply {
                mirrorMode = Constants.VIDEO_MIRROR_MODE_DISABLED
            }
        }
        inUseCanvases[uid] = canvas
        logComToFile("VideoCanvasPool", "获取VideoCanvas for UID: $uid, 池状态: ${getPoolStats()}")

        return canvas
    }

    /**
     * 回收VideoCanvas
     */
    fun recycleVideoCanvas(uid: Int) {
        inUseCanvases[uid]?.let { canvas ->
            inUseCanvases.remove(uid)
            // 重置状态
            canvas.view = null
            canvas.uid = 0

            if (availableCanvases.size < MAX_POOL_SIZE) {
                availableCanvases.push(canvas)
                logComToFile("VideoCanvasPool", "VideoCanvas回收到池中 for UID: $uid")
            } else {
                // 池已满，让对象被GC回收
                logComToFile("VideoCanvasPool", "VideoCanvas池已满，对象将被GC回收")
            }
        }
    }

    /**
     * 清理所有VideoCanvas
     */
    fun clear() {
        availableCanvases.clear()
        inUseCanvases.clear()
        creationCount.set(0)
    }

    fun getPoolStats(): String {
        return "创建数: $creationCount, 使用中: ${inUseCanvases.size}, 可用: ${availableCanvases.size}"
    }
}