package cn.huanyuan.happymeet.ui.setting

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.happymeet.bean.AppCheckItemInfo
import cn.huanyuan.happymeet.bean.SwitchConfigInfo
import cn.huanyuan.happymeet.net.rxApi
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class SettingViewModel : BaseViewModel() {
    val checkInfoObservable = MutableLiveData<ResultState<MutableList<AppCheckItemInfo>>>()
    val switchInfoObservable = MutableLiveData<ResultState<MutableList<SwitchConfigInfo>>>()
    fun getAppCheckInfo(hasCameraPermission:Boolean,hasMicrophonePermission: Boolean) {
        request({ rxApi.getAppCheckInfo(hasCameraPermission,hasMicrophonePermission) }, checkInfoObservable, true)
    }

    fun getSwitchSetInfo() {
        request({ rxApi.getSwitchSetInfo() }, switchInfoObservable, true)
    }
}