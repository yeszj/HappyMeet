package cn.yanhu.commonres.utils

import android.content.Context
import android.provider.Settings
import com.blankj.utilcode.util.DeviceUtils

/**
 * 设备信息工具类
 */
object DeviceInfoUtil {


    fun isCpu64():Boolean{
        val abIs = DeviceUtils.getABIs()
        return abIs.contains("arm64-v8a")
    }

    /**
     * 获取Androidid
     */
    fun getAndroidId(context: Context): String {
        return Settings.System.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}