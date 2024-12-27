package cn.yanhu.agora.ui.statics

import androidx.lifecycle.MutableLiveData
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.LiveIncomeDetailInfo
import cn.yanhu.agora.bean.LiveRecordResponse
import cn.yanhu.agora.bean.LiveStatisticTotalInfo
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/3/15
 * desc:
 */
class LiveStatisticsViewModel : BaseViewModel() {
    val liveStatisticLivedata = MutableLiveData<ResultState<LiveStatisticTotalInfo>>()

    val liveRecordLivedata = MutableLiveData<ResultState<LiveRecordResponse>>()
    val incomeDetailLivedata = MutableLiveData<ResultState<LiveIncomeDetailInfo>>()

    fun getLiveStatisticInfo() {
        request({ agoraRxApi.getLiveStatisticInfo() }, liveStatisticLivedata, true)
    }

    fun getRoomIncomeDetail(roomId:String) {
        request({ agoraRxApi.getRoomIncomeDetail(roomId) }, incomeDetailLivedata, true)
    }

    fun getMyLiveRecord(filterId:Int,page:Int) {
        request({ agoraRxApi.getMyLiveRecord(filterId,page) }, liveRecordLivedata, true)
    }
}