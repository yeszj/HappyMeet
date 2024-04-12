package cn.yanhu.imchat.custom.conversation

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.FragmentActivity
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.bean.GetChatGroupRequest
import cn.yanhu.imchat.bean.GroupBean
import cn.yanhu.imchat.manager.ImTeamManager
import cn.yanhu.imchat.ui.conversation.ImConversationFragment
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.GsonUtils
import com.netease.nimlib.sdk.team.model.Team
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean
import com.netease.yunxin.kit.conversationkit.ui.view.ConversationView

/**
 * @author: zhengjun
 * created: 2024/2/2
 * desc:
 * ConversationBean
 */
@Suppress("LABEL_NAME_CLASH")
class CustomConversationView : ConversationView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var type = ImConversationFragment.TYPE_SINGLE_ALL
    fun setShowType(showGroup: Int) {
        type = showGroup
    }

    override fun setData(data: List<ConversationBean>) {
        super.setData(resetList(data))
    }

    fun resetData(data: List<ConversationBean>) {
        super.setData(data)
    }

    override fun addData(data: List<ConversationBean>) {
        super.addData(resetList(data))
    }

    override fun update(data: List<ConversationBean>) {
        if (type == ImConversationFragment.TYPE_GROUP) {
            currentTeamList.forEach { teamInfo ->
                data.forEach {
                    if (it.infoData.contactId == teamInfo.id) {
                        it.infoData.teamInfo = teamInfo
                        return@forEach
                    }
                }
            }
            super.update(resetList(data, currentTeamList.size != data.size))
        } else {
            super.update(resetList(data, false))
        }

    }

    override fun update(data: ConversationBean) {
        if (type == ImConversationFragment.TYPE_GROUP) {
            if (data.viewType == ConversationConstant.ViewType.TEAM_VIEW) {
                super.update(data)
            }
        } else {
            if (data.viewType == ConversationConstant.ViewType.CHAT_VIEW) {
                super.update(data)
            }
        }

    }


    private fun resetList(
        data: List<ConversationBean>,
        getUser: Boolean = true
    ): MutableList<ConversationBean> {
        val list = mutableListOf<ConversationBean>()
        val contactIds = mutableListOf<String>()
        data.forEach {
            val contactId = it.infoData.contactId
            if (type == ImConversationFragment.TYPE_GROUP) {
                //只显示群组
                if (it.viewType == ConversationConstant.ViewType.TEAM_VIEW) {
                    list.add(it)
                    contactIds.add(contactId)
                }
            } else {
                //只显示私聊
                if (it.viewType == ConversationConstant.ViewType.CHAT_VIEW) {
                    contactIds.add(contactId)
                    list.add(it)
                }
            }
        }
        sendEmptyEvent(list)
        if (contactIds.size > 0 && getUser) {
            if (type == ImConversationFragment.TYPE_GROUP) {
                getGroupInfoByIds(contactIds.joinToString(separator = ","))
            }
        }
        if (type == ImConversationFragment.TYPE_SINGLE_INTIMATE) {
            if (contactIds.size > 0) {
                getIntimateUsers(list)
            }
            return mutableListOf()
        }
        return list
    }

    private fun sendEmptyEvent(list: MutableList<ConversationBean>) {
        if (type == ImConversationFragment.TYPE_GROUP) {
            LiveDataEventManager.sendLiveDataMessage(
                LiveDataEventManager.CHAT_GROUP_MSG,
                list.isEmpty()
            )
        }
    }


    private var currentTeamList = mutableListOf<Team>()
    private fun getGroupInfoByIds(
        groupIds: String
    ) {
        val body = GetChatGroupRequest()
        body.groupIds = groupIds
        request(
            { imChatRxApi.getGroupsInfo(body) },
            object : OnRequestResultListener<MutableList<GroupBean>> {
                override fun onSuccess(data: BaseBean<MutableList<GroupBean>>) {
                    val groupList = data.data
                    currentTeamList = mutableListOf()
                    groupList?.forEach {
                        val teamInfo = ImTeamManager.getTeamInfo(it.groupId)
                        teamInfo.extension = GsonUtils.toJson(it)
                        currentTeamList.add(teamInfo)
                    }
                    updateTeamInfo(currentTeamList)
                }
            },
            isShowToast = false,
            activity = ActivityUtils.getTopActivity() as FragmentActivity
        )
    }

    /**
     * 获取所有跟我有亲密关系的用户
     */
    private fun getIntimateUsers(
        conversationList: List<ConversationBean>
    ) {
        request(
            { imChatRxApi.getIntimateUsers() },
            object : OnRequestResultListener<MutableList<String>> {
                override fun onSuccess(data: BaseBean<MutableList<String>>) {
                    val userIdList = data.data
                    val intimateConversationList: MutableList<ConversationBean> = mutableListOf()
                    if (type == ImConversationFragment.TYPE_SINGLE_INTIMATE) {
                        if (userIdList != null && userIdList.size > 0) {
                            conversationList.forEach {
                                if (userIdList.contains(it.infoData.contactId)) {
                                    intimateConversationList.add(it)
                                }
                            }
                        }
                        resetData(intimateConversationList)
                        LiveDataEventManager.sendLiveDataMessage(
                            LiveDataEventManager.SINGLE_INTIMATE_MSG,
                            intimateConversationList.isEmpty()
                        )
                    }
                }
            },
            isShowToast = false,
            activity = ActivityUtils.getTopActivity() as FragmentActivity
        )
    }
}