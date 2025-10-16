package cn.yanhu.agora.manager.monitor

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import cn.yanhu.baselib.utils.ext.logcom

object MemoryMonitor {

    data class SimpleMemoryInfo(
        val javaHeapUsed: Long,      // Java 堆已使用 (MB)
        val javaHeapMax: Long,       // Java 堆最大 (MB)
        val nativeMemory: Long,      // Native 内存 (MB)
        val totalMemory: Long,       // 总内存 (MB)
        val usagePercent: Double,    // 使用百分比
        val isLowMemory: Boolean     // 是否低内存
    )

    fun getSimpleMemoryInfo(context: Context): SimpleMemoryInfo {
        val runtime = Runtime.getRuntime()
        val debug = Debug.MemoryInfo()
        Debug.getMemoryInfo(debug)

        // Java 内存
        val javaHeapUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val javaHeapMax = runtime.maxMemory() / (1024 * 1024)
        val usagePercent = javaHeapUsed.toDouble() / javaHeapMax.toDouble() * 100

        // Native 内存
        val nativeMemory = debug.nativePss / 1024L

        // 总内存
        val totalMemory = debug.totalPss / 1024L

        // 系统内存状态
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        return SimpleMemoryInfo(
            javaHeapUsed = javaHeapUsed,
            javaHeapMax = javaHeapMax,
            nativeMemory = nativeMemory,
            totalMemory = totalMemory,
            usagePercent = usagePercent,
            isLowMemory = memoryInfo.lowMemory
        )
    }

    fun isMemoryCritical(context: Context): Boolean {
        val info = getSimpleMemoryInfo(context)
        return info.usagePercent > 85.0 || info.isLowMemory
    }
    private val temperatureMonitor = TemperatureMonitor()
    private val cpuMonitor = CPUMonitor()
    fun logMemorySnapshot(context: Context) {
        val info = getSimpleMemoryInfo(context)
        val temperature = temperatureMonitor.getCurrentTemperature()
        val cpuUsage = cpuMonitor.getCurrentCPUUsage()
        val batteryTemp = temperatureMonitor.getBatteryTemperature()
        logcom(
            "memoryInfo", """
            📱 内存快照:
            ▫️ Java堆: ${info.javaHeapUsed}MB/${info.javaHeapMax}MB (${"%.1f".format(info.usagePercent)}%)
            ▫️ Native: ${info.nativeMemory}MB
            ▫️ 内存总计: ${info.totalMemory}MB
            ▫️ 低内存: ${info.isLowMemory}
            ▫️ 内存状态: ${if (isMemoryCritical(context)) "⚠️紧张" else "✅正常"}
            ▫️ 设备温度: ${"%.1f".format(temperature)}°C
            ▫️ CPU使用率: ${"%.1f".format(cpuUsage)}% 
            ▫️ 电池温度: ${"%.1f".format(batteryTemp)}°C
        """.trimIndent()
        )
    }

}