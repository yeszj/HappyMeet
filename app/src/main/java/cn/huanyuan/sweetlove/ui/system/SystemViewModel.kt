package cn.huanyuan.sweetlove.ui.system

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.sweetlove.bean.ComplaintInfo
import cn.huanyuan.sweetlove.bean.SecurityInfo
import cn.huanyuan.sweetlove.net.rxApi
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.ReportConfigInfo
import cn.yanhu.commonres.bean.SystemMessageInfo
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

    val msgInfoObservable = MutableLiveData<ResultState<MutableList<SystemMessageInfo>>>()
    val blackUserListObservable = MutableLiveData<ResultState<MutableList<BaseUserInfo>>>()



    fun getBlackUserList() {
        request({ rxApi.getBlackUserList() }, blackUserListObservable,
            isShowDialog = true
        )
    }

    fun getAllSystemMsg() {
        request({ rxApi.getAllSystemMsg() }, msgInfoObservable,
            isShowDialog = true
        )
    }

    val readMsgObservable = MutableLiveData<ResultState<String>>()
    fun readSystemMsg() {
        request({ rxApi.readSystemMsg() }, readMsgObservable,
            isShowDialog = false
        )
    }


    fun complaintUser() {
        request({ rxApi.complaintUser(complaintInfo.value) }, complaintResultObservable,
            isShowDialog = true,
            loadingHasContent = true
        )
    }

    val reportConfigObservable = MutableLiveData<ResultState<MutableList<ReportConfigInfo>>>()
    fun getReportConfigs() {
        request({ rxApi.getReportConfigs() }, reportConfigObservable,
            isShowDialog = true
        )
    }

    fun getSecurityInfo() {
        request({ rxApi.getSecurityInfo() }, securityInfoObservable,
            isShowDialog = true
        )
    }
}