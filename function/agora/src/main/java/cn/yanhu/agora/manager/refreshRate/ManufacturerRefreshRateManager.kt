package cn.yanhu.agora.manager.refreshRate

import android.app.Activity
import android.os.Build

/**
 * @author: zhengjun
 * created: 2025/10/20
 * desc:
 */
object ManufacturerRefreshRateManager {

    /**
     * 获取设备制造商特定的刷新率设置
     */
    fun setupManufacturerSpecificRefreshRate(activity: Activity) {
        when {
            isXiaomiDevice() -> setupXiaomiRefreshRate(activity)
            isOppoDevice() -> setupOppoRefreshRate(activity)
            isVivoDevice() -> setupVivoRefreshRate(activity)
            isSamsungDevice() -> setupSamsungRefreshRate(activity)
            else -> setupGenericRefreshRate(activity)
        }
    }

    private fun isXiaomiDevice(): Boolean {
        return Build.MANUFACTURER.equals("xiaomi", ignoreCase = true) ||
                Build.MANUFACTURER.equals("redmi", ignoreCase = true)
    }

    private fun isOppoDevice(): Boolean {
        return Build.MANUFACTURER.equals("oppo", ignoreCase = true) ||
                Build.MANUFACTURER.equals("realme", ignoreCase = true) ||
                Build.MANUFACTURER.equals("oneplus", ignoreCase = true)
    }

    private fun isVivoDevice(): Boolean {
        return Build.MANUFACTURER.equals("vivo", ignoreCase = true) ||
                Build.MANUFACTURER.equals("iqoo", ignoreCase = true)
    }

    private fun isSamsungDevice(): Boolean {
        return Build.MANUFACTURER.equals("samsung", ignoreCase = true)
    }

    private fun setupXiaomiRefreshRate(activity: Activity) {
        // 小米设备可能有特殊的刷新率设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RefreshRateManager.setHighestRefreshRate(activity)
        }
    }

    private fun setupOppoRefreshRate(activity: Activity) {
        // OPPO/Realme/OnePlus设备
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val supportedRates = RefreshRateManager.getSupportedRefreshRates(activity)
            // OPPO设备通常支持智能切换
            val smartRate = supportedRates.find { it == 120f } ?: supportedRates.firstOrNull() ?: 60f
            RefreshRateManager.setRefreshRate(activity, smartRate)
        }
    }

    private fun setupVivoRefreshRate(activity: Activity) {
        // Vivo/iQOO设备
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RefreshRateManager.setHighestRefreshRate(activity)
        }
    }

    private fun setupSamsungRefreshRate(activity: Activity) {
        // 三星设备
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val supportedRates = RefreshRateManager.getSupportedRefreshRates(activity)
            // 三星可能支持自适应刷新率
            val adaptiveRate = supportedRates.find { it == 120f } ?: supportedRates.firstOrNull() ?: 60f
            RefreshRateManager.setRefreshRate(activity, adaptiveRate)
        }
    }

    private fun setupGenericRefreshRate(activity: Activity) {
        // 通用设备设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RefreshRateManager.setHighestRefreshRate(activity)
        }
    }
}