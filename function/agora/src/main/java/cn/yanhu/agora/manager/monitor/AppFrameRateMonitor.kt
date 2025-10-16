package cn.yanhu.agora.manager.monitor

import android.view.Choreographer
import cn.yanhu.baselib.utils.ext.logcom

/**
 * @author: zhengjun
 * created: 2025/10/16
 * desc:
 */
/**
 * 应用帧率监控器
 */
class AppFrameRateMonitor {

    private var frameCount = 0
    private var lastFrameTime = 0L
    private var currentFPS = 0
    private var isMonitoring = false

    private val choreographer = Choreographer.getInstance()
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            frameCount++

            val currentTime = System.currentTimeMillis()
            if (lastFrameTime == 0L) {
                lastFrameTime = currentTime
            }

            val elapsed = currentTime - lastFrameTime
            if (elapsed >= 1000) { // 每秒钟计算一次FPS
                currentFPS = (frameCount * 1000 / elapsed).toInt()
                frameCount = 0
                lastFrameTime = currentTime

                // 回调通知
                onFPSUpdate?.invoke(currentFPS)
            }

            if (isMonitoring) {
                choreographer.postFrameCallback(this)
            }
        }
    }

    var onFPSUpdate: ((fps: Int) -> Unit)? = null

    /**
     * 开始监控帧率
     */
    fun startMonitoring() {
        if (isMonitoring) return

        isMonitoring = true
        frameCount = 0
        lastFrameTime = 0L
        currentFPS = 0

        choreographer.postFrameCallback(frameCallback)
        logcom("FrameRateMonitor", "开始监控应用帧率")
    }

    /**
     * 停止监控帧率
     */
    fun stopMonitoring() {
        isMonitoring = false
        choreographer.removeFrameCallback(frameCallback)
        logcom("FrameRateMonitor", "停止监控应用帧率")
    }

    /**
     * 获取当前帧率
     */
    fun getCurrentFPS(): Int {
        return currentFPS
    }

    /**
     * 获取平均帧率（从开始监控到现在）
     */
    fun getAverageFPS(): Double {
        // 这里可以扩展实现平均帧率计算
        return currentFPS.toDouble()
    }

    /**
     * 检查是否掉帧（低于阈值）
     */
    fun isDroppingFrames(threshold: Int = 50): Boolean {
        return currentFPS < threshold
    }
}