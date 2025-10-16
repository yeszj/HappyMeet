package cn.yanhu.agora.manager.monitor

import android.content.Context
import cn.yanhu.baselib.utils.ext.logcom

/**
 * @author: zhengjun
 * created: 2025/10/16
 * desc:
 */
/**
 * 完整的应用帧率监控管理器
 */
class ComprehensiveFrameRateMonitor(private val context: Context) {

    private val appFrameRateMonitor = AppFrameRateMonitor()
    private val fpsHistory = mutableListOf<FPSData>()
    private val maxHistorySize = 60 // 保存60秒的历史数据

    private var isSystemWideMonitoring = false

    data class FPSData(
        val timestamp: Long,
        val fps: Int,
        val monitorType: MonitorType
    )

    enum class MonitorType {
        APP_WIDE, SURFACE_VIEW, CUSTOM
    }

    /**
     * 开始全面监控
     */
    fun startComprehensiveMonitoring() {
        // 启动应用级帧率监控
        appFrameRateMonitor.onFPSUpdate = { fps ->
            recordFPSData(fps, MonitorType.APP_WIDE)
            logcom("FrameRateMonitor", "应用帧率: $fps FPS")
            logcom("FrameRateMonitor", "应用帧率: ${getFPSReport()}")
        }
        appFrameRateMonitor.startMonitoring()

        isSystemWideMonitoring = true
        logcom("FrameRateMonitor", "开始全面帧率监控")
    }

    /**
     * 停止全面监控
     */
    fun stopComprehensiveMonitoring() {
        appFrameRateMonitor.stopMonitoring()
        isSystemWideMonitoring = false
        logcom("FrameRateMonitor", "停止全面帧率监控")
    }


    /**
     * 记录帧率数据
     */
    private fun recordFPSData(fps: Int, type: MonitorType) {
        val data = FPSData(
            timestamp = System.currentTimeMillis(),
            fps = fps,
            monitorType = type
        )

        fpsHistory.add(data)

        // 限制历史数据大小
        if (fpsHistory.size > maxHistorySize) {
            fpsHistory.removeAt(0)
        }
    }

    /**
     * 获取当前应用帧率
     */
    fun getCurrentAppFPS(): Int {
        return appFrameRateMonitor.getCurrentFPS()
    }

    /**
     * 获取平均帧率
     */
    fun getAverageFPS(durationSeconds: Int = 10): Double {
        val now = System.currentTimeMillis()
        val cutoffTime = now - (durationSeconds * 1000)

        val recentData = fpsHistory.filter { it.timestamp >= cutoffTime }
        if (recentData.isEmpty()) return 0.0

        return recentData.map { it.fps }.average()
    }

    /**
     * 获取帧率稳定性（标准差）
     */
    fun getFPSStability(durationSeconds: Int = 10): Double {
        val now = System.currentTimeMillis()
        val cutoffTime = now - (durationSeconds * 1000)

        val recentData = fpsHistory.filter { it.timestamp >= cutoffTime }
        if (recentData.isEmpty()) return 0.0

        val average = recentData.map { it.fps }.average()
        val variance = recentData.map {
            Math.pow(it.fps - average, 2.0)
        }.average()

        return Math.sqrt(variance)
    }

    /**
     * 获取帧率报告
     */
    fun getFPSReport(): FPSReport {
        val currentFPS = getCurrentAppFPS()
        val averageFPS = getAverageFPS(30) // 30秒平均
        val stability = getFPSStability(30)

        return FPSReport(
            currentFPS = currentFPS,
            averageFPS = averageFPS,
            stability = stability,
            isStable = stability < 5.0, // 标准差小于5认为稳定
            frameTimeMs = 1000.0 / currentFPS,
            recommendation = getRecommendation(currentFPS, stability)
        )
    }

    private fun getRecommendation(currentFPS: Int, stability: Double): String {
        return when {
            currentFPS < 30 -> "帧率过低，建议优化性能"
            currentFPS < 50 -> "帧率一般，可能有卡顿"
            stability > 10 -> "帧率不稳定，建议检查资源使用"
            else -> "帧率良好"
        }
    }

    data class FPSReport(
        val currentFPS: Int,
        val averageFPS: Double,
        val stability: Double,
        val isStable: Boolean,
        val frameTimeMs: Double,
        val recommendation: String
    ) {
        override fun toString(): String {
            return """
                当前帧率: $currentFPS FPS
                平均帧率: ${"%.1f".format(averageFPS)} FPS
                稳定性: ${"%.2f".format(stability)} (${if (isStable) "稳定" else "不稳定"})
                帧时间: ${"%.2f".format(frameTimeMs)} ms
                建议: $recommendation
            """.trimIndent()
        }
    }

    /**
     * 清理资源
     */
    fun release() {
        stopComprehensiveMonitoring()
        fpsHistory.clear()
    }
}