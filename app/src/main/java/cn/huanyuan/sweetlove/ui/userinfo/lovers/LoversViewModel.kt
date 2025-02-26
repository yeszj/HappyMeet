package cn.huanyuan.sweetlove.ui.userinfo.lovers

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.sweetlove.net.rxApi
import cn.yanhu.commonres.bean.response.LoversResponse
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/3/5
 * desc:
 */
class LoversViewModel : BaseViewModel() {


    val loversResponseObservable = MutableLiveData<ResultState<LoversResponse>>()
    fun getLoversIndex(chatUserId: String) {
        request({ rxApi.getLoversIndex(chatUserId) }, loversResponseObservable, true)
    }

    val cancelLoversObservable = MutableLiveData<ResultState<String>>()
    fun cancelLovers(cancelUserId:String) {
        request({ rxApi.cancelLovers(cancelUserId) }, cancelLoversObservable,
            isShowDialog = false, isShowToast = false
        )
    }

    val bindLoversObservable = MutableLiveData<ResultState<String>>()
    fun bindLovers(bindUserId:String, giftId:String) {
        request({ rxApi.bindLovers(bindUserId,giftId) }, bindLoversObservable,
            isShowDialog = false, isShowToast = false
        )
    }
}