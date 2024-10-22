package cn.zj.netrequest.status

import androidx.lifecycle.MutableLiveData
import cn.zj.netrequest.R
import com.blankj.utilcode.util.StringUtils.getString
import cn.zj.netrequest.application.ApplicationProxy
import com.hjq.toast.ToastUtils
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


/**
 * 自定义结果集封装类
 */
sealed class ResultState<out T> {
    companion object {
        fun <T> onAppSuccess(data: T?): ResultState<T> = Success(data)
        fun <T> onAppError(error: CustomException): ResultState<T> = Error(error)
    }

    data class Success<out T>(val data: T?) : ResultState<T>()
    data class Error(val error: CustomException) : ResultState<Nothing>()
}


/**
 * 处理返回值
 * @param result 请求结果
 */
fun <T> MutableLiveData<ResultState<T>>.paresResult(result: BaseBean<T>) {
    value = if (result.code == ErrorCode.SUCCESS) {
        ResultState.onAppSuccess(result.data)
    } else {
        ResultState.onAppError(CustomException(result.code, result.msg))
    }
}

/**
 * 不处理返回值 直接返回请求结果
 * @param result 请求结果
 */
fun <T> MutableLiveData<ResultState<T>>.paresResult(result: T) {
    value = if (result is BaseBean<*>) {
        if (result.code == ErrorCode.SUCCESS) {
            ResultState.onAppSuccess(result)
        } else {
            ResultState.onAppError(CustomException(result.code, result.msg))
        }
    } else {
        ResultState.onAppSuccess(result)
    }
}

/**
 * 异常转换异常处理
 */
fun <T> MutableLiveData<ResultState<T>>.paresException(e: Throwable, isShowToast: Boolean = true) {
    if (e is CustomException) {
        if ((e.code == ErrorCode.TOKEN_INVALID || e.code == ErrorCode.TOKEN_FAIL || e.code == ErrorCode.UNLOGIN || e.code == ErrorCode.CODE_DEVICE_CHANGE || e.code == ErrorCode.ACCOUNT_BLOCK)) {
            //token失效，重新登录
            ApplicationProxy.instance.loginInvalid()
            if (isShowToast) {
                ToastUtils.show(e.msg)
            }
        }else {
            if (isShowToast) {
                ToastUtils.show(e.msg)
            }
        }
        this.value = ResultState.onAppError(e)
    } else {
        if (isShowToast) {
            e.printStackTrace()
            var message = e.message
            if (e is UnknownHostException) {
                message = getString(R.string.net_load_error)
            } else if (e is SocketTimeoutException || e is ConnectException) {
                message = getString(R.string.tips_timeout)
            }
            if ("Job was cancelled" != message) {
                ToastUtils.show(message)
            }
        }
        this.value = ResultState.onAppError(CustomException(e))
    }
}

fun dealNetException(e: Throwable, isShowToast: Boolean = true) {
    if (e is CustomException) {
        if ((e.code == ErrorCode.TOKEN_INVALID || e.code == ErrorCode.TOKEN_FAIL || e.code == ErrorCode.UNLOGIN)) {
            //token失效，重新登录
            ApplicationProxy.instance.loginInvalid()
            if (isShowToast) {
                ToastUtils.show(e.msg)
            }
        }else {
            if (isShowToast) {
                ToastUtils.show(e.msg)
            }
        }
    } else {
        if (isShowToast) {
            e.printStackTrace()
            var message = e.message
            if (e is UnknownHostException) {
                message =getString(R.string.net_load_error)
            } else if (e is SocketTimeoutException || e is ConnectException) {
                message = getString(R.string.tips_timeout)
            }
            if ("Job was cancelled" != message) {
                ToastUtils.show(message)
            }
        }
    }
}

