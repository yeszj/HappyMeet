package cn.huanyuan.happymeet.ui.invite

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.happymeet.bean.InviteInfo
import cn.huanyuan.happymeet.bean.InviteRecordResponse
import cn.huanyuan.happymeet.net.rxApi
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.request
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

    fun getMyInviteUser(page:Int) {
        request({ rxApi.getMyInviteUser(page) }, myInviteInfoObservable, true)
    }

    fun getInviteInfo() {
        request({ rxApi.getInviteInfo() }, invitePageInfoObservable, true)
    }
}