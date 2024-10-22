package cn.huanyuan.sweetlove.ui.history

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.sweetlove.net.rxApi
import cn.yanhu.commonres.bean.response.SeenMeHistoryResponse
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/3/4
 * desc:
 */
class HistoryViewModel:BaseViewModel() {
    val seenMeHistoryObservable = MutableLiveData<ResultState<SeenMeHistoryResponse>>()

    fun getHistoryViewPageInfo(page:Int) {
        request({ rxApi.getHistoryViewPageInfo(page) }, seenMeHistoryObservable, true)
    }
}