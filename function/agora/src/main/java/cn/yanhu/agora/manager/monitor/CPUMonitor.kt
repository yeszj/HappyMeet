package cn.yanhu.agora.manager.monitor

import cn.yanhu.baselib.utils.ext.logcom
import java.io.RandomAccessFile

/**
 * @author: zhengjun
 * created: 2025/10/16
 * desc:
 */
/**
 * CPU 使用率监控
 */
class CPUMonitor {

    private var lastCpuTime: Long = 0
    private var lastAppCpuTime: Long = 0

    /**
     * 获取当前CPU使用率
     */
    fun getCurrentCPUUsage(): Double {
        return try {
            // 方法1: 读取/proc/stat获取总CPU使用率
            val totalCpuUsage = getTotalCPUUsage()

            // 方法2: 获取当前进程的CPU使用率
            val processCpuUsage = getProcessCPUUsage()

            // 返回较大的值
            maxOf(totalCpuUsage, processCpuUsage)
        } catch (e: Exception) {
            logcom("CPUMonitor", "获取CPU使用率失败")
            0.0
        }
    }

    /**
     * 获取总CPU使用率
     */
    private fun getTotalCPUUsage(): Double {
        return try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val line = reader.readLine()
            reader.close()

            val parts = line.split("\\s+".toRegex())
            if (parts.size >= 5) {
                val user = parts[1].toLong()
                val nice = parts[2].toLong()
                val system = parts[3].toLong()
                val idle = parts[4].toLong()

                val total = user + nice + system + idle
                val usage = (user + nice + system).toDouble() / total.toDouble() * 100.0
                usage
            } else {
                0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }

    /**
     * 获取进程CPU使用率
     */
    private fun getProcessCPUUsage(): Double {
        return try {
            val pid = android.os.Process.myPid()
            val statFile = RandomAccessFile("/proc/$pid/stat", "r")
            val statLine = statFile.readLine()
            statFile.close()

            val parts = statLine.split("\\s+".toRegex())
            if (parts.size >= 15) {
                val utime = parts[13].toLong() // 用户态时间
                val stime = parts[14].toLong() // 内核态时间
                val cutime = parts[15].toLong() // 子进程用户态时间
                val cstime = parts[16].toLong() // 子进程内核态时间

                val appCpuTime = utime + stime + cutime + cstime
                val currentTime = System.currentTimeMillis()

                val usage = if (lastCpuTime > 0 && lastAppCpuTime > 0) {
                    val elapsedTime = currentTime - lastCpuTime
                    val elapsedAppTime = appCpuTime - lastAppCpuTime
                    (elapsedAppTime.toDouble() / elapsedTime.toDouble()) * 100.0
                } else {
                    0.0
                }

                lastCpuTime = currentTime
                lastAppCpuTime = appCpuTime

                usage
            } else {
                0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }

    /**
     * 获取设备CPU核心数
     */
    fun getCPUCoreCount(): Int {
        return Runtime.getRuntime().availableProcessors()
    }
}