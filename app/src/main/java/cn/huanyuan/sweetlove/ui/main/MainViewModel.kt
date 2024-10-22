package cn.huanyuan.sweetlove.ui.main

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.sweetlove.bean.AppVersionInfo
import cn.huanyuan.sweetlove.bean.TabEntity
import cn.huanyuan.sweetlove.bean.WalletInfo
import cn.huanyuan.sweetlove.net.rxApi
import cn.yanhu.commonres.bean.response.FriendsResponse
import cn.yanhu.commonres.bean.response.RoomListResponse
import cn.yanhu.commonres.bean.response.SameCityUserResponse
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/1/30
 * desc:
 */
class MainViewModel : BaseViewModel() {
    val tabInfoObservable = MutableLiveData<ResultState<MutableList<TabEntity>>>()



    val roomListObservable = MutableLiveData<ResultState<RoomListResponse>>()

    val sameCityUserObservable = MutableLiveData<ResultState<SameCityUserResponse>>()

    val friendObservable = MutableLiveData<ResultState<FriendsResponse>>()

    val walletObservable = MutableLiveData<ResultState<WalletInfo>>()
    val checkVersionObservable = MutableLiveData<ResultState<AppVersionInfo?>>()

    fun checkVersion() {
        request({ rxApi.checkVersion() }, checkVersionObservable, false)
    }

    fun getMainTabInfo() {
        request({ rxApi.getMainTabInfo() }, tabInfoObservable, true)
    }



    fun getRoomList(type:Int,page:Int) {
        request({ rxApi.getRoomList(type,page) }, roomListObservable, true)
    }

    fun getSameCityUserList(ages:String,province:String,page:Int) {
        request({ rxApi.getSameCityUserList(ages,province,page,50) }, sameCityUserObservable, true)
    }

    fun getFriendList(page:Int) {
        request({ rxApi.getFriendList(page) }, friendObservable, true)
    }
    fun getWalletInfo() {
        request({ rxApi.getWalletInfo() }, walletObservable, true)
    }

}