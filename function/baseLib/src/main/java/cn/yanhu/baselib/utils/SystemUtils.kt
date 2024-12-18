package cn.yanhu.baselib.utils

import android.os.Build

/**
 * @author: witness
 * created: 2023/1/12
 * desc:
 */
object SystemUtils {
    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    fun getSystemVersion(): String? {
        return Build.VERSION.RELEASE
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    fun getSystemModel(): String? {
        return Build.BRAND
    }


}