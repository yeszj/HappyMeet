package cn.huanyuan.sweetlove.ui.recommend

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.sweetlove.bean.RecommendRoomResponse
import cn.huanyuan.sweetlove.net.rxApi
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2025/7/18
 * desc:
 */
class RecommendRoomViewModel : BaseViewModel() {

    val recommendRoomObservable = MutableLiveData<ResultState<RecommendRoomResponse>>()
    fun getRecommendRoom() {
        request({ rxApi.getRecommendRoom() }, recommendRoomObservable, false)
    }
}