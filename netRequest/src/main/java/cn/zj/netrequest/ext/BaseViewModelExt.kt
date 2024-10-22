package cn.zj.netrequest.ext

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.CustomException
import cn.zj.netrequest.status.ErrorCode
import cn.zj.netrequest.status.ResultState
import cn.zj.netrequest.status.paresException
import cn.zj.netrequest.status.paresResult
import kotlinx.coroutines.*


/**
 * net request 不校验请求结果数据是否是成功
 * @param block 请求体方法
 * @param resultState 请求回调的ResultState数据
 * @param isShowDialog 是否显示加载框
 * @param loadingHasContent 加载时当前页面的view是否显示
 */

fun <T> BaseViewModel.request(
    block: suspend () -> BaseBean<T>,
    resultState: MutableLiveData<ResultState<T>>,
    isShowDialog: Boolean = false,
    loadingHasContent: Boolean = false,
    isShowToast: Boolean = true
): Job {
    return viewModelScope.launch {
        val isShow =
            if (!loadingHasContent) isShowDialog && (resultState.value == null || resultState.value is ResultState.Error)
            else isShowDialog
        runCatching {
            if (isShow) {
                loadingChange.showDialog.postValue(loadingHasContent)
            }
            block()
        }.onSuccess {
            if (isShow) {
                loadingChange.dismissDialog.postValue(true)
            }
            Log.d(TAG, "request：bean:${it} code:${it.code} msg:${it.msg}")
            resultState.paresResult(it)
        }.onFailure {
            if (it is CustomException) {
                if (isShow) {
                    if (it.code == ErrorCode.NO_MORE) {
                        loadingChange.dismissDialog.postValue(true)
                    } else {
                        loadingChange.dismissDialog.postValue(loadingHasContent)
                        if (!loadingHasContent) {
                            loadingChange.errorMsgDialog.postValue(it.msg)
                        }
                    }
                }
            } else {
                if (isShow) {
                    loadingChange.dismissDialog.postValue(loadingHasContent)
                }
            }
            resultState.paresException(it, isShowToast)
        }
    }
}


/**
 * 带有返回值的请求
 *
 * @param T
 * @param block
 * @param resultState
 * @param isShowDialog
 * @param loadingMessage
 * @return
 */
fun <T> BaseViewModel.request2(
    block: suspend () -> T,
    resultState: MutableLiveData<ResultState<T>>,
    isShowDialog: Boolean = false,
    loadingHasContent: Boolean = false,
    isShowToast: Boolean = true,
): Job {
    return  viewModelScope.launch {
        val isShow =
            isShowDialog && (resultState.value == null || resultState.value is ResultState.Error)
        runCatching {
            if (isShow) loadingChange.showDialog.postValue(loadingHasContent)
            block()
        }.onSuccess {
            if (isShow) loadingChange.dismissDialog.postValue(true)
            resultState.paresResult(it)
        }.onFailure {
            if (it is CustomException) {
                if (isShow) {
                    if (it.code == ErrorCode.NO_MORE) {
                        loadingChange.dismissDialog.postValue(true)
                    } else {
                        loadingChange.dismissDialog.postValue(loadingHasContent)
                    }
                }
            } else {
                if (isShow) {
                    loadingChange.dismissDialog.postValue(loadingHasContent)
                }
            }
            resultState.paresException(it, isShowToast)
        }
    }
}
