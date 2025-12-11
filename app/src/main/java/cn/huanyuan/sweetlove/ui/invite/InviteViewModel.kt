package cn.huanyuan.sweetlove.ui.invite

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.sweetlove.bean.InviteInfo
import cn.huanyuan.sweetlove.bean.InviteRecordResponse
import cn.huanyuan.sweetlove.net.rxApi
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/3/14
 * desc:
 */
class InviteViewModel:BaseViewModel() {
    val userInfoObservable = MutableLiveData<ResultState<BaseUserInfo>>()
    val myInviteInfoObservable = MutableLiveData<ResultState<InviteRecordResponse>>()
    val invitePageInfoObservable = MutableLiveData<ResultState<InviteInfo>>()
    fun getInviteMyUser() {
        request({ rxApi.getInviteMyUser() }, userInfoObservable, true)
    }

    fun getMyInviteUser(page:Int,filterId:String,inviteFilterId:String) {
        request({ rxApi.getMyInviteUser(page,filterId,inviteFilterId) }, myInviteInfoObservable, true,myInviteInfoObservable.value!=null)
    }

    fun getInviteInfo() {
        request({ rxApi.getInviteInfo() }, invitePageInfoObservable, true)
    }

    fun shareSuccess(source: Int= 1 , onRequestResultListener: OnRequestResultListener<String>) {
        request2({ rxApi.shareSuccess(source) },onRequestResultListener)
    }
}