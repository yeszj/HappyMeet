package cn.huanyuan.happymeet.ui.userinfo

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.happymeet.bean.GuardRankResponse
import cn.huanyuan.happymeet.bean.UserLevelResponse
import cn.huanyuan.happymeet.net.rxApi
import cn.yanhu.commonres.bean.AuthCenterInfo
import cn.yanhu.commonres.bean.EditUserInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.dynamic.api.momentRxApi
import cn.yanhu.dynamic.bean.DynamicInfo
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/3/5
 * desc:
 */
class UserViewModel : BaseViewModel() {

    val dynamicObservable = MutableLiveData<ResultState<MutableList<DynamicInfo>>>()

    val userDetailObservable = MutableLiveData<ResultState<UserDetailInfo>>()

    val authCenterInfoObservable = MutableLiveData<ResultState<AuthCenterInfo>>()
    val editInfoObservable = MutableLiveData<ResultState<EditUserInfo>>()

    val guardRankObservable = MutableLiveData<ResultState<GuardRankResponse>>()

    val userLevelObservable = MutableLiveData<ResultState<UserLevelResponse>>()

    fun getDynamicByUserId(page: Int, userId: String) {
        request({ momentRxApi.getDynamicByUserId(page, userId) }, dynamicObservable, false)
    }

    fun getUserInfo(userId: String) {
        request({ rxApi.getUserInfoByUserId(userId) }, userDetailObservable, true)
    }

    fun getAuthCenterInfo() {
        request({ rxApi.getAuthCenterInfo() }, authCenterInfoObservable, true)
    }

    fun getEditInfo() {
        request({ rxApi.getEditInfo() }, editInfoObservable, true)
    }

    fun getGuardRankList(userId: String) {
        request({ rxApi.getGuardRankList(userId) }, guardRankObservable, true)
    }

    fun getUserLevelInfo() {
        request({ rxApi.getUserLevelInfo() }, userLevelObservable, true)
    }
}