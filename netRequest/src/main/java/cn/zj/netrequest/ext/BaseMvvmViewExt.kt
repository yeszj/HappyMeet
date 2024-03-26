package cn.zj.netrequest.ext

import cn.zj.netrequest.status.CustomException
import cn.zj.netrequest.status.ErrorCode.DATA_EMPTY
import cn.zj.netrequest.status.ResultState


/**
 * 显示页面状态，这里有个技巧，成功回调在第一个，其后两个带默认值的回调可省
 * @param resultState 接口返回值
 * @param onLoading 加载中
 * @param onSuccess 成功回调
 * @param onError 失败回调
 *
 */
fun <T> parseState(
    resultState: ResultState<T>?,
    onSuccess: (T) -> Unit,
    onError: ((CustomException) -> Unit)? = null
) {
    resultState?.apply {
        when (resultState) {
            is ResultState.Success -> {
                if (resultState.data != null) {
                    onSuccess(resultState.data)
                } else onError?.let {
                    it(
                        CustomException(DATA_EMPTY, "数据为空")
                    )
                }
            }
            is ResultState.Error -> {
                val e = resultState.error
                onError?.run { this(e) }
            }
        }
    }

}

