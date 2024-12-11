package cn.huanyuan.sweetlove.ui.teenage

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.sweetlove.net.rxApi
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class TeenAgeViewModel : BaseViewModel() {
    var openTeenAgeModeResult = MutableLiveData<ResultState<Boolean>>()
    var closeTeenAgeModeResult = MutableLiveData<ResultState<Boolean>>()

    fun checkIsOpenJuvenileMode(onRequestResultListener: OnRequestResultListener<Boolean>) {
        request({ rxApi.checkIsOpenJuvenileMode() }, onRequestResultListener)
    }
    fun openTeenAgeMode(password: String) {
        request(
            { rxApi.openJuvenileMode(password) }, openTeenAgeModeResult
        )
    }
    fun closeTeenAgeMode(password: String) {
        request(
            { rxApi.closeJuvenileMode(password) }, closeTeenAgeModeResult
        )
    }

}