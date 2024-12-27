package cn.huanyuan.sweetlove.ui.userinfo

import androidx.lifecycle.MutableLiveData
import cn.huanyuan.sweetlove.bean.GuardRankResponse
import cn.huanyuan.sweetlove.bean.UserLevelResponse
import cn.huanyuan.sweetlove.net.rxApi
import cn.yanhu.commonres.bean.AuthCenterInfo
import cn.yanhu.commonres.bean.EditUserInfo
import cn.yanhu.commonres.bean.MineMenuBean
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.dynamic.api.momentRxApi
import cn.yanhu.dynamic.bean.DynamicInfo
import cn.yanhu.imchat.api.imChatRxApi
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState
import com.pcl.sdklib.bean.CheckBaiduFaceResult
import com.pcl.sdklib.bean.PostBaiduAuthBean

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
    val editUserInfoObservable = MutableLiveData<ResultState<String>>()

    val myPageInfoObservable = MutableLiveData<ResultState<UserDetailInfo>>()

    val myServiceObservable = MutableLiveData<ResultState<MutableList<MineMenuBean>>>()
    val searchUserObservable = MutableLiveData<ResultState<MutableList<UserDetailInfo>>>()
    val addFriendObservable = MutableLiveData<ResultState<String>>()
    val addFriendRoseObservable = MutableLiveData<ResultState<String>>()

    fun addFriend(chatUserId: String) {
        request({ imChatRxApi.addFriend(chatUserId) }, addFriendObservable, false)
    }
    fun becomeFriendRose(chatUserId: String) {
        request({ imChatRxApi.becomeFriendRose(chatUserId) }, addFriendRoseObservable, false, isShowToast = false)
    }
    val cancelFriendObservable = MutableLiveData<ResultState<String>>()
    fun cancelFriends(chatUserId: String) {
        request({ imChatRxApi.cancelFriends(chatUserId) }, cancelFriendObservable, false)
    }
    fun searchUserList(content: String) {
        request({ rxApi.searchUserList(content) }, searchUserObservable, true,
            loadingHasContent = true
        )
    }

    fun getMyPageInfo() {
        request({ rxApi.getMyPageInfo() }, myPageInfoObservable, true)
    }

    fun getMyService() {
        request({ rxApi.getMyService() }, myServiceObservable, false)
    }
    fun getDynamicByUserId(page: Int, userId: String, loadingHasContent: Boolean) {
        request(
            { momentRxApi.getDynamicByUserId(page, userId) },
            dynamicObservable,
            false,
            loadingHasContent
        )
    }

    fun updatePersonalPageSingle(type: Int, content: String,onRequestResultListener: OnRequestResultListener<String>) {
        request(
            { rxApi.updatePersonalPageSingle(type, content) }, onRequestResultListener
        )
    }

    fun getUserInfo(userId: String, loadingHasContent: Boolean) {
        request({ rxApi.getUserInfoByUserId(userId) }, userDetailObservable, !loadingHasContent)
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

    val realNameObservable = MutableLiveData<ResultState<String>>()
    fun realNameProve(realName:String,idCard:String) {
        request({ rxApi.realNameProve(realName,idCard) }, realNameObservable,
            isShowDialog = true, loadingHasContent = true
        )
    }
     val checkFaceResultObservable = MutableLiveData<ResultState<CheckBaiduFaceResult>>()
    fun checkBaiduStep() {
        request({ rxApi.checkBaiduFace() }, checkFaceResultObservable,
            isShowDialog = false
        )
    }

    val canBaidFaceObservable = MutableLiveData<ResultState<Boolean>>()
    fun ifCanBaiduFace() {
        request({ rxApi.ifCanBaiduFace() }, canBaidFaceObservable,
            isShowDialog = false, isShowToast = false
        )
    }

    val postBaidFaceObservable = MutableLiveData<ResultState<String>>()
    fun submitBaiduFace(postBaiduAuthBean: PostBaiduAuthBean) {
        request({ rxApi.submitBaiduFace(postBaiduAuthBean) }, postBaidFaceObservable,
            isShowDialog = false
        )
    }
}