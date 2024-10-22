package cn.huanyuan.sweetlove.ui.setting

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.sweetlove.bean.AppCheckItemInfo
import cn.huanyuan.sweetlove.bean.AppVersionInfo
import cn.huanyuan.sweetlove.bean.SwitchConfigInfo
import cn.huanyuan.sweetlove.net.rxApi
import cn.yanhu.commonres.bean.ChatPriceItemInfo
import cn.yanhu.imchat.api.imChatRxApi
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

    val checkVersionObservable = MutableLiveData<ResultState<AppVersionInfo?>>()

    fun checkVersion() {
        request({ rxApi.checkVersion() }, checkVersionObservable, false)
    }

    fun getAppCheckInfo(hasCameraPermission:Boolean,hasMicrophonePermission: Boolean) {
        request({ rxApi.getAppCheckInfo(hasCameraPermission,hasMicrophonePermission) }, checkInfoObservable, true)
    }

    fun getSwitchSetInfo() {
        request({ rxApi.getSwitchSetInfo() }, switchInfoObservable, true)
    }

    val priceConfigObservable = MutableLiveData<ResultState<MutableList<ChatPriceItemInfo>>>()
    fun getPriceConfig(chatUserId:Int = 0) {
        request({ imChatRxApi.getPriceConfig(chatUserId) }, priceConfigObservable, false)
    }

    val setPriceObservable = MutableLiveData<ResultState<Boolean>>()
    fun setPrice(chatUserId:Int = 0,type:String,id:Int) {
        request({ imChatRxApi.setPrice(chatUserId.toString(),type,id) }, setPriceObservable, false)
    }
}