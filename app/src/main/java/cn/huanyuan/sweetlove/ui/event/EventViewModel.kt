package cn.huanyuan.sweetlove.ui.event

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import cn.huanyuan.sweetlove.bean.CommonEventImgConfig
import cn.huanyuan.sweetlove.bean.CommonEventRankResponse
import cn.huanyuan.sweetlove.bean.NewYearInfo
import cn.huanyuan.sweetlove.bean.NewYearRankResponse
import cn.huanyuan.sweetlove.net.rxApi
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/3/8
 * desc:
 */
class EventViewModel : BaseViewModel() {
    val newYearInfoLivedata = MutableLiveData<ResultState<NewYearInfo>>()
    val eventConfigLivedata = MutableLiveData<ResultState<CommonEventImgConfig>>()


    fun getCommonEventConfig(activityId:String) {
        request(
            { rxApi.getCommonEventConfig(activityId) }, eventConfigLivedata,
            isShowDialog = false
        )
    }

    fun getCommonEventRank(type:Int,activityId:String,onRequestResultListener: OnRequestResultListener<CommonEventRankResponse>) {
        request(
            { rxApi.getCommonEventRank(type,activityId) }, onRequestResultListener
        )
    }

    fun getNewYearInfo() {
        request(
            { rxApi.getNewYearInfo() }, newYearInfoLivedata,
            isShowDialog = false
        )
    }

    fun getNewYearRank(type:Int,onRequestResultListener: OnRequestResultListener<NewYearRankResponse>) {
        request(
            { rxApi.getNewYearRank(type) }, onRequestResultListener
        )
    }

    fun addVal(activity:FragmentActivity,onRequestResultListener: OnRequestResultListener<String>) {
        request(
            { rxApi.addVal() }, onRequestResultListener,true,activity
        )
    }
}