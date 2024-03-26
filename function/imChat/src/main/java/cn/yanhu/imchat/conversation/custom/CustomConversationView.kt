package cn.yanhu.imchat.conversation.custom

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.FragmentActivity
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.bean.ChatUserInfo
import cn.yanhu.imchat.bean.GetChatUsersRequest
import cn.yanhu.imchat.conversation.ImConversationFragment
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.GsonUtils
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean
import com.netease.yunxin.kit.conversationkit.ui.view.ConversationView
import com.netease.yunxin.kit.corekit.im.model.UserInfo

/**
 * @author: zhengjun
 * created: 2024/2/2
 * desc:
 * ConversationBean
 */
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
        super.setData(resetList(data,false))
    }

    override fun addData(data: List<ConversationBean>) {
        super.addData(resetList(data))
    }

    override fun update(data: List<ConversationBean>) {
        super.update(resetList(data))
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


    private fun resetList(data: List<ConversationBean>,getUser:Boolean = true) :MutableList<ConversationBean>{
        val list = mutableListOf<ConversationBean>()
        val contactIds = mutableListOf<String>()
        data.forEach {
            val contactId = it.infoData.contactId
            contactIds.add(contactId)
            if (type == ImConversationFragment.TYPE_GROUP) {
                //只显示群组
                if (it.viewType == ConversationConstant.ViewType.TEAM_VIEW) {
                    list.add(it)
                }
            } else {
                //只显示私聊
                if (it.viewType == ConversationConstant.ViewType.CHAT_VIEW) {
                    if(getUser){
                        list.add(it)
                    }else{
                        if (type == ImConversationFragment.TYPE_SINGLE_ALL) {
                            list.add(it)
                        } else {
                            val extension = it.infoData.userInfo?.extension
                            val userInfo = GsonUtils.fromJson(extension, ChatUserInfo::class.java)
                            if(userInfo!=null){
                                val intimate =
                                    userInfo.intimate
                                if (intimate > 0) {
                                    list.add(it)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (type == ImConversationFragment.TYPE_GROUP) {
            LiveDataEventManager.sendLiveDataMessage(
                LiveDataEventManager.CHAT_GROUP_MSG,
                list.isEmpty()
            )
        }else if(type == ImConversationFragment.TYPE_SINGLE_INTIMATE){
            LiveDataEventManager.sendLiveDataMessage(
                LiveDataEventManager.SINGLE_INTIMATE_MSG,
                list.isEmpty()
            )
        }

        if (contactIds.size > 0 && getUser) {
            if(type==ImConversationFragment.TYPE_GROUP){

            }else{
                getUserInfoByIds(contactIds.joinToString(separator = ","),data)
            }
        }
        return list
    }

    private fun getUserInfoByIds(
        userIds: String,conversationList: List<ConversationBean>
    ) {
        val body = GetChatUsersRequest()
        body.userIds = userIds
        request(
            { imChatRxApi.getUsersInfo(body) },
            object : OnRequestResultListener<MutableList<UserInfo>> {
                override fun onSuccess(data: BaseBean<MutableList<UserInfo>>) {
                    updateUserInfo(data.data)
                    if (type == ImConversationFragment.TYPE_SINGLE_INTIMATE){
                        resetData(conversationList)
                    }
                }
            },
            isShowToast = false,
            activity = ActivityUtils.getTopActivity() as FragmentActivity
        )
    }
}