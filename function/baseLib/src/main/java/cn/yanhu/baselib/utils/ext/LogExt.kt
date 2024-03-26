package cn.yanhu.baselib.utils.ext

import android.util.Log
import cn.zj.netrequest.BuildConfig
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils

private var lastToastTime = 0L
private var lastToastContent: CharSequence = ""
private var isShowLog = BuildConfig.DEBUG

/**
 * 自定义toast
 */
fun showToast(msg: String?, isLong: Boolean = false) {
    if (msg.isNullOrBlank()) return
    if (msg == "success"){
        return
    }
    if (lastToastContent == msg && System.currentTimeMillis() - lastToastTime < 2000) {
        return
    } else {
        lastToastTime = System.currentTimeMillis()
        lastToastContent = msg
    }
    try {
        com.hjq.toast.ToastUtils.show(msg)
    } catch (e: Exception) {
        ToastUtils.showShort(msg)
    }

}


fun showToast(stringId: Int) {
    try {
        showToast(Utils.getApp().getString(stringId))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 常规日志打印
 * 过滤关键字 logcom
 */
fun logcom(msg: String?) {
    if (BuildConfig.DEBUG && msg != null) {
        Log.v("LogCom", msg)
    }
}

fun logcom(tag: String, msg: String?) {
    if (BuildConfig.DEBUG && msg != null) Log.v(tag, msg)
}


fun logComToFile(tag: String, msg: String?) {
    LogUtils.file(tag, msg)
    if (BuildConfig.DEBUG && msg != null) Log.v(tag, msg)
}

/**
 * 流程日志打印
 * 过滤关键字 logprocess
 */
fun logprocess(msg: String?) {
    if (BuildConfig.DEBUG && msg != null) Log.i("LogProcess", msg)

}