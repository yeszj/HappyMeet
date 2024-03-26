package cn.yanhu.imchat

import androidx.lifecycle.MutableLiveData
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.bean.GroupBean
import cn.yanhu.imchat.bean.GroupDetailInfo
import cn.yanhu.imchat.bean.GroupMemberData
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
    fun getRecommendGroupList(page: Int) {
        request({ imChatRxApi.recommendGroupList(page) }, groupListObserver, true)
    }

    fun getGroupDetail(groupId: String) {
        request({ imChatRxApi.getGroupDetail(groupId) }, groupDetailObserver, true)
    }

    fun getGroupMember(groupId: String) {
        request({ imChatRxApi.getGroupMember(groupId) }, groupMemberObserver, true)
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
}