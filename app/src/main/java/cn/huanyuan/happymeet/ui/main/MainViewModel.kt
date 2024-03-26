package cn.huanyuan.happymeet.ui.main

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.happymeet.bean.TabEntity
import cn.huanyuan.happymeet.bean.WalletInfo
import cn.huanyuan.happymeet.net.rxApi
import cn.yanhu.commonres.bean.MineMenuBean
import cn.yanhu.commonres.bean.UserDetailInfo
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

    val myPageInfoObservable = MutableLiveData<ResultState<UserDetailInfo>>()

    val myServiceObservable = MutableLiveData<ResultState<MutableList<MineMenuBean>>>()

    val roomListObservable = MutableLiveData<ResultState<RoomListResponse>>()

    val sameCityUserObservable = MutableLiveData<ResultState<SameCityUserResponse>>()

    val friendObservable = MutableLiveData<ResultState<FriendsResponse>>()

    val walletObservable = MutableLiveData<ResultState<WalletInfo>>()

    fun getMainTabInfo() {
        request({ rxApi.getMainTabInfo() }, tabInfoObservable, true)
    }

    fun getMyPageInfo() {
        request({ rxApi.getMyPageInfo() }, myPageInfoObservable, true)
    }

    fun getMyService() {
        request({ rxApi.getMyService() }, myServiceObservable, false)
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