package cn.yanhu.commonres.manager

import android.annotation.SuppressLint
import cn.yanhu.commonres.api.commonRxApi
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.BaseBean

/**
 * @author: zhengjun
 * created: 2024/12/25
 * desc:
 */
object AppManager {
    const val STATE_FOREGROUND = 0 // 前台在线
    const val STATE_BACKGROUND = 1 // 后台在线

    @JvmStatic
    @SuppressLint("CheckResult")
    fun setAppState(state: Int,areNotificationsEnabled:Boolean) {
        request2({commonRxApi.setAppState(state,if (areNotificationsEnabled) 1 else 0)},object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {
            }
        },isShowToast = false)
    }
}