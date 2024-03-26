package cn.zj.netrequest.ext

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.CustomException
import cn.zj.netrequest.status.ErrorCode
import cn.zj.netrequest.status.dealNetException
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ThreadUtils
import com.hjq.toast.ToastUtils
import kotlinx.coroutines.*
const val TAG = "okhttp.OkHttpClient"

fun <T> request(
    block: suspend () -> BaseBean<T>,
    listener: OnRequestResultListener<T>,
    isShowToast: Boolean = true,
) {
    GlobalScope.launch {
        kotlin.runCatching {
            block()
        }.onSuccess {
            ThreadUtils.runOnUiThread {
                if (it.code == ErrorCode.SUCCESS) {
                    listener.onSuccess(it)
                } else {
                    listener.onFail(it.code, it.msg)
                    dealNetException(CustomException(it.code, it.msg), isShowToast)
                }
            }
        }.onFailure {
            ThreadUtils.runOnUiThread {
                if (it is CustomException) {
                    listener.onFail(it.code, it.msg)
                } else {
                    listener.onFail(-1, it.message)
                }
                if (isShowToast && it !is CancellationException) {
                    ToastUtils.show(it.message)
                }
            }
        }
    }
}


fun <T> request(
    block: suspend () -> BaseBean<T>,
    listener: OnRequestResultListener<T>,
    isShowToast: Boolean = true,
    activity: FragmentActivity?,
) {
    activity?.apply {
        activity.lifecycleScope.launch {
            kotlin.runCatching {
                block()
            }.onSuccess {
                ThreadUtils.runOnUiThread {
                    if (it.code == ErrorCode.SUCCESS) {
                        listener.onSuccess(it)
                    } else {
                        listener.onFail(it.code, it.msg)
                        dealNetException(CustomException(it.code, it.msg), isShowToast)
                    }
                }
            }.onFailure {
                ThreadUtils.runOnUiThread {
                    if (it is CustomException) {
                        listener.onFail(it.code, it.msg)
                        dealNetException(CustomException(it.code, it.msg), isShowToast)
                    } else {
                        Log.d(TAG,"request：bean:${it} errorMs:${it.message}")
                        listener.onFail(-1, it.message)
                        if (isShowToast && it !is CancellationException) {
                            ToastUtils.show(it.message)
                        }
                    }
                }
            }
        }
    }
}


fun <T> request(
    block: suspend () -> BaseBean<T>,
    listener: OnBooleanResultListener,
    isShowToast: Boolean = true
) {
    val activity = ActivityUtils.getTopActivity() as FragmentActivity?
    activity?.apply {
        activity.lifecycleScope.launch {
            kotlin.runCatching {
                block()
            }.onSuccess {
                ThreadUtils.runOnUiThread {
                    if (it.code == ErrorCode.SUCCESS) {
                        if (it.data is Boolean && it.data) {
                            listener.onSuccess()
                        } else {
                            listener.onFail(ErrorCode.DATA_ERROR, it.msg)
                            dealNetException(
                                CustomException(ErrorCode.DATA_ERROR, it.msg),
                                isShowToast
                            )
                        }
                    } else {
                        listener.onFail(it.code, it.msg)
                        dealNetException(CustomException(it.code, it.msg), isShowToast)
                    }
                }
            }.onFailure {
                ThreadUtils.runOnUiThread {
                    Log.d(TAG,"request：bean:${it} errorMs:${it.message}")
                    listener.onFail(-1, it.message)
                    if (isShowToast && it !is CancellationException) {
                        ToastUtils.show(it.message)
                    }
                }
            }
        }
    }
}



interface OnRequestResultListener<T> {
    fun onSuccess(data: BaseBean<T>)
    fun onFail(code: Int?, msg: String?) {}
}

interface OnBooleanResultListener {
    fun onSuccess()
    fun onFail(code: Int?, msg: String?) {}
}
