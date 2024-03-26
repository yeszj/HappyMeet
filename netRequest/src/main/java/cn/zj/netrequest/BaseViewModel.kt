package cn.zj.netrequest

import androidx.lifecycle.ViewModel

/**
 * viewmodel基类
 *
 */
abstract class BaseViewModel :ViewModel() {

    val loadingChange: UiLoadingChange by lazy { UiLoadingChange() }

    /**
     * 可通知Activity/fragment 显示隐藏加载框
     */
    inner class UiLoadingChange {
        //显示加载框
        val showDialog by lazy { EventLiveData<Boolean>() }
        //隐藏
        val dismissDialog by lazy { EventLiveData<Boolean>() }

        val errorMsgDialog by lazy { EventLiveData<String>() }
    }
}