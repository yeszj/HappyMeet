package cn.yanhu.agora.manager.monitor

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.blankj.utilcode.util.ActivityUtils
import java.io.File
import java.io.RandomAccessFile

/**
 * @author: zhengjun
 * created: 2025/10/16
 * desc:
 */
/**
 * 设备温度监控
 */
class TemperatureMonitor {

    /**
     * 获取当前设备温度（摄氏度）
     */
    fun getCurrentTemperature(): Double {
        return try {
            // 方法1: 尝试从系统文件读取温度
            val thermalFile = File("/sys/class/thermal/thermal_zone0/temp")
            if (thermalFile.exists()) {
                val reader = RandomAccessFile(thermalFile, "r")
                val tempStr = reader.readLine()
                reader.close()
                tempStr.toDouble() / 1000.0 // 转换为摄氏度
            } else {
                // 方法2: 使用电池温度作为参考
                getBatteryTemperature()
            }
        } catch (e: Exception) {
            // 方法3: 返回安全值
            30.0
        }
    }

    /**
     * 获取电池温度
     */
     fun getBatteryTemperature(): Double {
        return try {
            val batteryIntent = ActivityUtils.getTopActivity()?.registerReceiver(null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val temperature = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            if (temperature != -1) temperature / 10.0 else 30.0
        } catch (e: Exception) {
            30.0
        }
    }
}