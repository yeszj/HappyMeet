package cn.yanhu.agora.manager.refreshRate

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * @author: zhengjun
 * created: 2025/10/20
 * desc:
 */
object RefreshRateManager {

    /**
     * 获取设备支持的刷新率列表
     */
    fun getSupportedRefreshRates(activity: Activity): List<Float> {
        return try {
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.display
            } else {
                activity.windowManager.defaultDisplay
            }
            val supportedModes = display?.supportedModes ?: return emptyList()

            val refreshRates = mutableSetOf<Float>()
            supportedModes.forEach { mode ->
                refreshRates.add(mode.refreshRate)
            }
            refreshRates.toList().sortedDescending()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 获取当前刷新率
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun getCurrentRefreshRate(activity: Activity): Float {
        return try {
            activity.display?.refreshRate ?: 60f
        } catch (e: Exception) {
            60f
        }
    }

    /**
     * 设置刷新率（Android 10+）
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun setRefreshRate(activity: Activity, refreshRate: Float): Boolean {
        return try {
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.display
            } else {
                activity.windowManager.defaultDisplay
            }
            val supportedModes = display?.supportedModes ?: return false

            // 查找匹配的显示模式
            val targetMode = supportedModes.find { mode ->
                mode.refreshRate == refreshRate
            }

            targetMode?.let { mode ->
                val params = activity.window.attributes
                params.preferredDisplayModeId = mode.modeId
                activity.window.attributes = params
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 设置最高可用刷新率
     */
    fun setHighestRefreshRate(activity: Activity): Boolean {
        val supportedRates = getSupportedRefreshRates(activity)
        return if (supportedRates.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setRefreshRate(activity, supportedRates.first())
            } else {
                false
            }
        } else {
            false
        }
    }

    /**
     * 设置标准刷新率（60Hz）
     */
    fun setStandardRefreshRate(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setRefreshRate(activity, 60f)
        } else {
            false
        }
    }
}