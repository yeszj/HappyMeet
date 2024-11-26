package cn.huanyuan.sweetlove.ui.userinfo.dressUp

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.sweetlove.net.rxApi
import cn.yanhu.commonres.bean.TabConfigInfo
import cn.yanhu.commonres.bean.response.DressUpResponse
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:
 */
class DressUpViewModel : BaseViewModel() {
    val dressInfoObservable = MutableLiveData<ResultState<DressUpResponse>>()

    val myDressInfoObservable = MutableLiveData<ResultState<DressUpResponse>>()

    val tabListObservable = MutableLiveData<ResultState<List<TabConfigInfo>>>()


    fun getMyDressUpInfo(type: Int) {
        request({ rxApi.getMyDressUpInfo(type) }, myDressInfoObservable, true)
    }

    fun getDressUpInfo(type: Int) {
        request({ rxApi.getDressUpInfo(type) }, dressInfoObservable, true)
    }

    fun getStoreTabs() {
        request({ rxApi.getStoreTabs() }, tabListObservable, false)
    }
}