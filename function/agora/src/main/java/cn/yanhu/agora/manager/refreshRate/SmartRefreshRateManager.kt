package cn.yanhu.agora.manager.refreshRate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import cn.yanhu.baselib.utils.ext.logcom
import android.os.Build
import android.os.PowerManager

/**
 * @author: zhengjun
 * created: 2025/10/20
 * desc:
 */
object SmartRefreshRateManager {

    private var batteryLevel: Int = 100
    private var isPowerSavingMode: Boolean = false

    /**
     * 智能设置刷新率（平衡性能与功耗）
     */
    fun setupSmartRefreshRate(activity: Activity) {
        updateBatteryStatus(activity)

        val supportedRates = RefreshRateManager.getSupportedRefreshRates(activity)
        val optimalRate = calculateOptimalRefreshRate(supportedRates)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RefreshRateManager.setRefreshRate(activity, optimalRate)
        }

        logcom("智能刷新率设置: ${optimalRate}Hz, 电量: $batteryLevel%, 省电模式: $isPowerSavingMode")
    }

    private fun updateBatteryStatus(context: Context) {
        try {
            val batteryStatus = context.registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            batteryStatus?.let { intent ->
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                batteryLevel = (level * 100 / scale.toFloat()).toInt()

                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

                isPowerSavingMode =
                    status == BatteryManager.BATTERY_STATUS_NOT_CHARGING || powerManager?.isPowerSaveMode == true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateOptimalRefreshRate(supportedRates: List<Float>): Float {
        return when {
            // 省电模式或电量低时使用低刷新率
            isPowerSavingMode || batteryLevel < 20 -> {
                supportedRates.minOrNull() ?: 60f
            }

            // 中等电量时使用中等刷新率
            batteryLevel < 50 -> {
                supportedRates.find { it in 60f..90f } ?: supportedRates.firstOrNull() ?: 60f
            }

            // 电量充足时使用高刷新率
            else -> {
                supportedRates.firstOrNull() ?: 60f
            }
        }
    }
}