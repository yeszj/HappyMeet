package cn.yanhu.imchat

import androidx.lifecycle.MutableLiveData
import cn.yanhu.commonres.bean.ChatPriceItemInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.bean.GroupBean
import cn.yanhu.imchat.bean.GroupChatPageInfo
import cn.yanhu.imchat.bean.GroupDetailInfo
import cn.yanhu.imchat.bean.GroupMemberData
import cn.yanhu.imchat.bean.SystemMsgUnReadInfo
import cn.yanhu.imchat.bean.request.GetChatUsersRequest
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.OnBooleanResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/2/2
 * desc:
 */
class ImChatViewModel : BaseViewModel() {
    val groupListObserver = MutableLiveData<ResultState<MutableList<GroupBean>>>()
    val groupDetailObserver = MutableLiveData<ResultState<GroupDetailInfo>>()
    val groupMemberObserver = MutableLiveData<ResultState<GroupMemberData>>()
    val userInfoObserver = MutableLiveData<ResultState<UserDetailInfo>>()
    val groupPageInfoObserver = MutableLiveData<ResultState<GroupChatPageInfo>>()
    val userListObserver = MutableLiveData<ResultState<MutableList<UserDetailInfo>>>()
    val systemMsgObserver = MutableLiveData<ResultState<SystemMsgUnReadInfo>>()

    fun getSystemMsg() {
        request({ imChatRxApi.getSystemMsg() }, systemMsgObserver, false)
    }

    fun getUserList(userIds: String) {
        val request = GetChatUsersRequest()
        request.userIds = userIds
        request({ imChatRxApi.getUserList(request) }, userListObserver, false)
    }

    fun getRecommendGroupList(page: Int) {
        request({ imChatRxApi.recommendGroupList(page) }, groupListObserver, true)
    }

    fun getGroupDetail(groupId: String) {
        request({ imChatRxApi.getGroupDetail(groupId) }, groupDetailObserver, true)
    }

    fun getGroupMember(groupId: String) {
        request({ imChatRxApi.getGroupMember(groupId) }, groupMemberObserver, true)
    }

    fun getUserInfo(userId: String) {
        request({ imChatRxApi.getUserInfoByUserId(userId) }, userInfoObserver, false)
    }

    fun getImGroupInfo(groupId: String) {
        request({ imChatRxApi.getImGroupInfo(groupId) }, groupPageInfoObserver, true)
    }

    fun setAdmin(
        groupId: String,
        type: Int,
        userId: String,
        onBooleanResultListener: OnBooleanResultListener
    ) {
        request(
            { imChatRxApi.setAdmin(groupId, type, userId) },
            onBooleanResultListener
        )
    }

    fun skipUser(
        groupId: String,
        userId: String,
        onBooleanResultListener: OnBooleanResultListener
    ) {
        request(
            { imChatRxApi.skipUser(groupId, userId) },
            onBooleanResultListener
        )
    }

    fun setUserLimit(
        groupId: String,
        visitorEnterRule: Int,
        onBooleanResultListener: OnBooleanResultListener
    ) {
        request(
            { imChatRxApi.setUserLimit(groupId, visitorEnterRule) },
            onBooleanResultListener
        )
    }

    fun exitGroup(
        groupId: String,
        type: Int,
        onBooleanResultListener: OnBooleanResultListener
    ) {
        request(
            { imChatRxApi.exitGroup(groupId, type) },
            onBooleanResultListener
        )
    }

    val priceConfigObservable = MutableLiveData<ResultState<MutableList<ChatPriceItemInfo>>>()
    fun getPriceConfig(chatUserId: Int = 0) {
        request({ imChatRxApi.getPriceConfig(chatUserId) }, priceConfigObservable, false)
    }

    val setPriceObservable = MutableLiveData<ResultState<Boolean>>()
    fun setPrice(chatUserId: Int = 0, type: String, id: Int) {
        request(
            { imChatRxApi.setPrice(chatUserId.toString(), type, id) },
            setPriceObservable,
            false
        )
    }


    val addFriendObservable = MutableLiveData<ResultState<String>>()
    fun addFriend(chatUserId: String) {
        request({ imChatRxApi.addFriend(chatUserId) }, addFriendObservable, false)
    }
}