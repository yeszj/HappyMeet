package cn.huanyuan.happymeet.ui.system

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.happymeet.bean.ComplaintInfo
import cn.huanyuan.happymeet.bean.SecurityInfo
import cn.huanyuan.happymeet.net.rxApi
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class SystemViewModel : BaseViewModel() {
    var complaintInfo: MutableLiveData<ComplaintInfo> = MutableLiveData()
    val complaintResultObservable = MutableLiveData<ResultState<Boolean>>()

    val securityInfoObservable = MutableLiveData<ResultState<SecurityInfo>>()
    fun complaintUser() {
        request({ rxApi.complaintUser(complaintInfo.value) }, complaintResultObservable,
            isShowDialog = true,
            loadingHasContent = true
        )
    }

    fun getSecurityInfo() {
        request({ rxApi.getSecurityInfo() }, securityInfoObservable,
            isShowDialog = true
        )
    }
}