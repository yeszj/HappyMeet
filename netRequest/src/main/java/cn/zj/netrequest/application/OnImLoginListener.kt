package cn.zj.netrequest.application

/**
 * @author: zhengjun
 * created: 2024/12/27
 * desc:
 */
interface OnImLoginListener {
    fun onSuccess()
    fun onError(code: Int, error: String?)
}