package cn.yanhu.dynamic.ui

import androidx.lifecycle.MutableLiveData
import cn.yanhu.dynamic.api.momentRxApi
import cn.yanhu.dynamic.bean.DynamicInfo
import cn.yanhu.dynamic.bean.response.DynamicDetailInfo
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/2/23
 * desc:
 */
class DynamicViewModel: BaseViewModel() {
    val dynamicListObserver = MutableLiveData<ResultState<MutableList<DynamicInfo>>>()

    val pubDynamicObserver = MutableLiveData<ResultState<Boolean>>()

    val dynamicDetailObserver = MutableLiveData<ResultState<DynamicDetailInfo>>()
    fun getDynamicList(page:Int) {
        request({ momentRxApi.getDynamicList(page,50) }, dynamicListObserver, true)
    }

    fun pubDynamic(city:String?,info:String?,images:String?) {
        request({ momentRxApi.pubDynamic(city,info,images) }, pubDynamicObserver,
            isShowDialog = true,
            loadingHasContent = true
        )
    }

    fun getDynamicDetail(dynamicId:String,page:Int) {
        request({ momentRxApi.getDynamicDetail(dynamicId,page) }, dynamicDetailObserver, true)
    }

}