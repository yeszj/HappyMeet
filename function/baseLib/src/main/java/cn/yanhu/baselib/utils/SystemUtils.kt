package cn.yanhu.baselib.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils

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
        return android.os.Build.BRAND
    }

    fun getDeviceName(context: Context): String? {
        return Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
    }

    fun encodeHeadInfo(headInfo: String?): String {
        return if (TextUtils.isEmpty(headInfo)) {
            ""
        } else {
            val stringBuffer = StringBuffer()
            var i = 0
            val length = headInfo!!.length
            while (i < length) {
                val c = headInfo[i]
                if (c <= '\u001f' || c >= '\u007f') {
                    stringBuffer.append(String.format("\\u%04x", c.code))
                } else {
                    stringBuffer.append(c)
                }
                i++
            }
            stringBuffer.toString()
        }
    }
}