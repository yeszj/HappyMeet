package com.pcl.sdklib.listener

/**
 * @author: zhengjun
 * created: 2023/11/13
 * desc:
 */
interface OnAuthResultListener {
    fun onAuthSuccess()
    fun onAuthFail(){}
}