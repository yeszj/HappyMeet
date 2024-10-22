package cn.yanhu.imchat.ui.conversation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.ViewModelProvider
import cn.yanhu.baselib.utils.DateUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.ConfigParamsManager
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.config.SpCacheKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.imchat.ImChatViewModel
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.bean.CacheConversationInfo
import cn.yanhu.imchat.bean.ConversationFinalMessageInfo
import cn.yanhu.imchat.bean.request.GetChatUsersRequest
import cn.yanhu.imchat.custom.chat.CustomConversationInfo
import cn.yanhu.imchat.custom.chat.EaseCommonUtils
import cn.yanhu.imchat.db.ChatUserInfoManager
import cn.yanhu.imchat.manager.ImMsgManager
import cn.yanhu.imchat.ui.chat.ImChatActivity
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.TimeUtils
import com.hyphenate.EMMessageListener
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMCursorResult
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.chat.EMFetchMessageOption
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo
import com.hyphenate.easeui.modules.conversation.model.EaseConversationSetStyle
import com.hyphenate.util.EMLog
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlin.math.min

import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.imchat.pop.ImChatDialog

class IMConversationListFrg : CustomEaseConversationListFragment() {
    var mContext: Activity? = null
    private var infoList: MutableList<EaseConversationInfo> = mutableListOf()
    private lateinit var viewModel: ImChatViewModel
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context as Activity
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        viewModel = ViewModelProvider(this)[ImChatViewModel::class.java]
        conversationListLayout.setTitleTextSize(0)
        conversationListLayout.showUnreadDotPosition(EaseConversationSetStyle.UnreadDotPosition.RIGHT)
    }


    override fun initData() {
        infoList = ArrayList()
        if (type != TYPE_FOLD) {
            refreshAllList()
            registerLiveDataListener()
        }
    }


    private fun registerLiveDataListener() {
    }


    private fun getPrivateChatList() {
        getConversationFromDb()
    }

    private fun getConversationFromDb() {
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<List<EMConversation>>() {
            override fun doInBackground(): List<EMConversation> {
                val allConversations = EMClient.getInstance().chatManager().allConversations
                return ArrayList(allConversations.values)
            }

            override fun onSuccess(result: List<EMConversation>) {
                if (result.isEmpty()) {
                    getConversationFromServer()
                } else {
                    upDataList(result)
                }
            }
        })
    }

    private var serverConversationList = mutableListOf<EMConversation>()
    private fun getConversationFromServer(cursor: String = "") {
        if (TextUtils.isEmpty(cursor)) {
            serverConversationList.clear()
        }
        EMClient.getInstance().chatManager().asyncFetchConversationsFromServer(50,
            cursor,
            object : EMValueCallBack<EMCursorResult<EMConversation>> {
                override fun onSuccess(result: EMCursorResult<EMConversation>) {
                    val nextCursor = result.cursor
                    // 获取到的会话列表
                    val conversations = result.data
                    loadServerMsgToDb(conversations)
                    serverConversationList.addAll(conversations)
                    if (conversations.size > 0) {
                        val finalConversation = conversations[conversations.size - 1]
                        val lastMessage = finalConversation.lastMessage
                        if (lastMessage != null && DateUtils.isToday(lastMessage.msgTime)) {
                            if (TextUtils.isEmpty(nextCursor)) {
                                runOnUiThread { upDataList(serverConversationList) }
                            } else {
                                getConversationFromServer(nextCursor)
                            }
                        } else {
                            runOnUiThread { upDataList(serverConversationList) }
                        }
                    } else {
                        runOnUiThread { upDataList(serverConversationList) }
                    }
                }

                override fun onError(error: Int, errorMsg: String) {
                    runOnUiThread { upDataList(mutableListOf()) }
                }
            })
    }

    private fun loadServerMsgToDb(
        conversations: MutableList<EMConversation>
    ) {
        val emFetchMessageOption = EMFetchMessageOption()
        emFetchMessageOption.setIsSave(true)
        conversations.forEach { it ->
            EMClient.getInstance().chatManager()
                .asyncFetchHistoryMessages(it.conversationId(),
                    EMConversation.EMConversationType.Chat,
                    10,
                    "",
                    emFetchMessageOption,
                    object : EMValueCallBack<EMCursorResult<EMMessage>?> {
                        override fun onSuccess(value: EMCursorResult<EMMessage>?) {
                        }

                        override fun onError(error: Int, errorMsg: String) {
                        }
                    })
        }
    }

    private fun addCacheConversationInfo(
        info: EMConversation, conversationInfo: CustomConversationInfo
    ) {
        val cacheMessage = getCustomMessage(info.lastMessage)
        val cacheConversationInfo = CacheConversationInfo(
            info.unreadMsgCount, info.conversationId(), cacheMessage
        )
        conversationInfo.cacheConversationInfo = cacheConversationInfo
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        userIdList.clear()
        loadData()
    }

    private fun refreshAllList() {
        if (type == TYPE_ALL) {
            loadData()
        }
    }

    @Synchronized
    private fun loadData() {
        getPrivateChatList()
    }

    /*
     * 更新会话列表
     * */

    private var conversationListTask: ThreadUtils.SimpleTask<MutableList<EaseConversationInfo>>? =
        null

    @Synchronized
    private fun upDataList(conversations: List<EMConversation>) {
        if (conversationListTask != null) {
            ThreadUtils.cancel(conversationListTask)
        }
        conversationListTask =
            object : ThreadUtils.SimpleTask<MutableList<EaseConversationInfo>>() {
                override fun doInBackground(): MutableList<EaseConversationInfo> {
                    if (conversations.isEmpty()) {
                        return mutableListOf()
                    }
                    val conversationList: MutableList<EaseConversationInfo> = ArrayList()

                    for (conversation in conversations) {
                        if (conversation.type == EMConversation.EMConversationType.GroupChat) {
                            continue
                        }
                        if (conversation.type == EMConversation.EMConversationType.ChatRoom) {
                            continue
                        }
                        val lastMessage = conversation.lastMessage ?: continue
                        // if (deleteHistoryConversation(conversation, lastMessage)) continue
                        if (filterSystemSendMsg(conversation, lastMessage)) continue
                        val info = EaseConversationInfo()
                        info.info = conversation
                        info.isTop = conversation.isPinned
                        info.timestamp =
                            if (conversation.lastMessage == null) 0 else conversation.lastMessage.msgTime
                        conversationList.add(info)
                    }
                    return conversationList
                }

                override fun onSuccess(conversationList: MutableList<EaseConversationInfo>) {
                    if (conversationList.isEmpty()) {
                        ConfigParamsManager.HAS_LOAD_CHAT = true
                        conversationListLayout.setData(listOf())
                    } else {
                        //conversationListLayout.setData(conversationList)
                        getUserInfoList(conversationList)
                    }
                }
            }
        ThreadUtils.executeByIo(conversationListTask)
    }

    /**
     * 删除5天内未聊过天的会话
     */
    private fun deleteHistoryConversation(
        conversation: EMConversation, lastMessage: EMMessage
    ): Boolean {
        if (conversation.type != EMConversation.EMConversationType.Chat) {
            return false
        }
        val startTime = DateUtils.getYestodyStr(-5, "yyyy-MM-dd")
        val timeMillis = TimeUtils.date2Millis(TimeUtils.string2Date(startTime, "yyyy-MM-dd"))
        if (lastMessage.msgTime < timeMillis) {
            val conversationId = conversation.conversationId()
            EMLog.e(
                "EMUnRead",
                "deleteHistoryConversation，消息全部设为已读，会话ID：" + conversationId + " 未读消息数：" + conversation.unreadMsgCount + "会话类型：" + conversation.type
            )
            conversation.markAllMessagesAsRead()
            EMClient.getInstance().chatManager()
                .deleteConversation(conversationId, false)
            if (!deleterConversationList.contains(conversationId)) {
                deleterConversationList.add(conversation.conversationId())
            }
            return true
        }
        return false
    }

    /**
     * 过滤牵线会话 判断最后一条消息是否是牵线消息(自定义消息event为MSG_QIANXIAN，loveString参数为1)
     * 1.删除一个月以前收到的牵线消息且还未建立关系的会话
     * 2.系统替自己发送的牵线消息且对方未回复不显示
     */
    private fun filterSystemSendMsg(
        conversation: EMConversation, lastMessage: EMMessage
    ): Boolean {
        if (lastMessage.type == EMMessage.Type.CUSTOM) {
            val messageBody = lastMessage.body as EMCustomMessageBody
            val event = messageBody.event()
            if (ChatConstant.MSG_QIANXIAN == event) {
                //系统发送的自定义牵线消息
                val params = messageBody.params
                val content = params["content"]
                if (!TextUtils.isEmpty(content) && (content!!.contains("为爱牵线") || content.contains(
                        "缘分牵线"
                    )) && lastMessage.from == AppCacheManager.userId
                ) {
                    val allMessages = conversation.searchMsgFromDB(
                        System.currentTimeMillis(), 5, EMConversation.EMSearchDirection.UP
                    )
                    return isAllQianXianMsg(allMessages)
                }
            }
        } else {
            //牵线时系统替自己发送的文本消息
            val loveString = lastMessage.getIntAttribute("loveString", -1)
            if (loveString == 1 && lastMessage.from == AppCacheManager.userId) {
                //最后一条是为爱牵线系统自动发的文本消息 且是我自己发的,总消息数量小于2条(一条是牵线提示消息，一条系统发的文本消息) 则会话记录不显示
                val allMessages = conversation.searchMsgFromDB(
                    System.currentTimeMillis(), 3, EMConversation.EMSearchDirection.UP
                )
                return allMessages.size <= 2
            }
        }
        return false
    }

    private fun isAllQianXianMsg(allMessages: MutableList<EMMessage>): Boolean {
        allMessages.forEach {
            if (!isQianXianMsg(it) || it.from != AppCacheManager.userId) {
                return false
            }
        }
        return true
    }

    private fun isQianXianMsg(emMessage: EMMessage): Boolean {
        var loveString = emMessage.getIntAttribute("loveString", -1)
        if (emMessage.type == EMMessage.Type.CUSTOM) {
            val messageBody2 = emMessage.body as EMCustomMessageBody
            val event2 = messageBody2.event()
            if (ChatConstant.MSG_QIANXIAN == event2) {
                loveString = 1
            }
        }
        return loveString == 1
    }

    private var userIdList = mutableListOf<String>()

    @Synchronized
    private fun getUserInfoList(list: MutableList<EaseConversationInfo>) {
        // 获取一个或多个用户的所有属性，一次调用用户 ID 数量不超过 100。
        val userArrays = ArrayList<String>()
        for (i in list.indices) {
            val emConversation = list[i].info as EMConversation
            val conversationId = emConversation.conversationId()
            if (!userIdList.contains(conversationId) && !TextUtils.isEmpty(conversationId) && conversationId != "null") {
                userIdList.add(conversationId)
                userArrays.add((conversationId))
            }
        }
        if (userArrays.size <= 0) {
            updateFinalConversationList(mutableListOf(), list)
        } else {
            val users = StringBuilder()
            for (i in userArrays.indices) {
                val s = userArrays[i]
                if (!TextUtils.isEmpty(s)) {
                    users.append(userArrays[i])
                }
                if (i + 1 < userArrays.size) {
                    users.append(",")
                }
            }
            updateConversationUserInfo(users, list)
        }


    }

    private fun updateConversationUserInfo(
        users: StringBuilder,
        list: MutableList<EaseConversationInfo>
    ) {
        val request = GetChatUsersRequest()
        request.userIds = users.toString()
        request(
            { imChatRxApi.getUserList(request) },
            object : OnRequestResultListener<MutableList<UserDetailInfo>> {
                override fun onSuccess(data: BaseBean<MutableList<UserDetailInfo>>) {
                    val userList = data.data
                    ConfigParamsManager.HAS_LOAD_CHAT = true
                    if (userList.isNullOrEmpty()) {
                        return
                    }
                    updateFinalConversationList(userList, list)
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    updateFinalConversationList(listOf(), list)
                }
            },
            false
        )
    }

    private var finalConversationListTask: ThreadUtils.SimpleTask<MutableList<EaseConversationInfo>>? =
        null

    @Synchronized
    private fun updateFinalConversationList(
        data: List<UserDetailInfo>, list: List<EaseConversationInfo>
    ) {
        if (finalConversationListTask != null) {
            ThreadUtils.cancel(finalConversationListTask)
        }
        finalConversationListTask =
            object : ThreadUtils.SimpleTask<MutableList<EaseConversationInfo>>() {
                override fun doInBackground(): MutableList<EaseConversationInfo> {
                    return getFinalConversationList(data, list)
                }

                override fun onSuccess(conversationList: MutableList<EaseConversationInfo>) {
                    conversationListLayout.setData(conversationList)
                    ConfigParamsManager.HAS_LOAD_CHAT = true
                }
            }
        ThreadUtils.executeByIo(finalConversationListTask)
    }


    private fun getFinalConversationList(
        data: List<UserDetailInfo>, list: List<EaseConversationInfo>
    ): MutableList<EaseConversationInfo> {
        infoList.clear()
        for (datum in data) {
            ChatUserInfoManager.saveUserInfo(datum)
        }
        var unReadCount = 0
        for (easeConversationInfo in list) {
            val dataBean =
                ChatUserInfoManager.getUserInfo((easeConversationInfo.info as EMConversation).conversationId())

            if (dataBean != null && dataBean.status != -2) {
                val conversationInfo = CustomConversationInfo()

                val emConversation = easeConversationInfo.info as EMConversation
                conversationInfo.info = emConversation
                conversationInfo.isClearUnReadCount = false
                conversationInfo.timestamp = easeConversationInfo.timestamp
                conversationInfo.isSelected = easeConversationInfo.isSelected
                conversationInfo.isGroup = easeConversationInfo.isGroup
                conversationInfo.isTop = easeConversationInfo.isTop
                //dataBean.selfPortrait = BaseApplication.getUserIcon()
                conversationInfo.dataBean = dataBean
                addCacheConversationInfo(emConversation, conversationInfo)
                infoList.add(conversationInfo)
                unReadCount += emConversation.unreadMsgCount
            }
        }
        LiveDataEventManager.sendLiveDataMessage(
            EventBusKeyConfig.UNREAD_COUNT, unReadCount
        )
        return infoList
    }

    override fun loadDataFinish(data: MutableList<EaseConversationInfo>?) {
        super.loadDataFinish(data)
        if (data != null) {
            saveConversationCache(data)
        }
    }


    private fun saveConversationCache(conversationList: MutableList<EaseConversationInfo>) {
        val subList = if (conversationList.size > 0) {
            conversationList.subList(
                0, min(10, conversationList.size)
            )
        } else {
            mutableListOf()
        }
        val toJson = GsonUtils.toJson(subList)
        SPUtils.getInstance().put(SpCacheKeyConfig.KEY_IM_CONVERSATION_LIST, toJson)
    }


    private fun getCustomMessage(
        emMessage: EMMessage
    ): ConversationFinalMessageInfo {
        val finalMessage = ConversationFinalMessageInfo()
        finalMessage.msgId = emMessage.msgId
        finalMessage.msgTime = emMessage.msgTime
        val body = emMessage.body
        if (body is EMCustomMessageBody) {
            val event = body.event()
            if (event == ImMsgManager.MSG_CUSTOM_EMOJI) {
                finalMessage.custom_msg = emMessage.getStringAttribute(
                    ChatConstant.CUSTOM_MSG, ""
                )
            } else {
                val messageDigest = EaseCommonUtils.getMessageDigest(emMessage, context)
                finalMessage.content = messageDigest
            }
        } else {
            val messageDigest = EaseCommonUtils.getMessageDigest(emMessage, context)
            finalMessage.content = messageDigest
        }
        return finalMessage

    }


    override fun onItemClick(view: View, position: Int) {
        super.onItemClick(view, position)
        if (position == -1) {
            showToast("该聊天不存在，请刷新重试")
            return
        }
        val item = conversationListLayout.getItem(position)
        if (item is CustomConversationInfo) {
            item.isClearUnReadCount = true
        }
        conversationListLayout.listAdapter.notifyItemChanged(position)
        if (item.isGroup) {
            //是群组
            // toChatGroup(item)
        } else {
            toPrivateChat(item)
        }
    }

    private fun toPrivateChat(item: EaseConversationInfo) {
        var conversationId = ""
        if (item is CustomConversationInfo && item.isLoadCache) {
            conversationId = item.cacheConversationInfo.conversationId
        } else {
            val info = item.info
            if (info is EMConversation) {
                conversationId = info.conversationId()
                //sendReadImUnReadMsgCmd(info, conversationId)
            }
        }
        try {

            val intent = ImChatActivity.getImChatIntent(mContext, conversationId)
            val isPop = requireArguments().getBoolean(IntentKeyConfig.IS_SHOW, false)
            if (isPop) {
                showChatDialog(intent)
            } else {
                mContext.startActivity(intent)
            }

        } catch (e: Exception) {
            showToast("该聊天不存在，请刷新重试")
        }
    }

    private fun showChatDialog(intent: Intent) {
        val extras = intent.extras
        val chatListDialog = ImChatDialog(extras)
        chatListDialog.showNow(childFragmentManager, "show_chat_im")
    }

    override fun onResume() {
        super.onResume()
        if (EMClient.getInstance().isSdkInited) {
            // 注册消息监听
            EMClient.getInstance().chatManager().addMessageListener(msgListener)
        }
        if (ConfigParamsManager.HAS_LOAD_CHAT) {
            refreshAllList()
        }
    }


    override fun onStop() {
        super.onStop()
        // 解除消息接收监听
        EMClient.getInstance().chatManager().removeMessageListener(msgListener)
    }

    //接受消息监听
    private val msgListener: EMMessageListener = object : EMMessageListener {
        // 收到消息，遍历消息队列，解析和显示。
        override fun onMessageReceived(messages: List<EMMessage>) {
            refreshConversationList()
        }

        override fun onMessageRecalled(messages: List<EMMessage>) {
            refreshConversationList()
        }
    }

    /**
     * 在当前页面时收到新消息 更新列表
     * 因为如果是在其它页面 返回时会在onResume方法中执行刷新操作
     * 减少接口调用频次
     */
    private fun refreshConversationList() {
        runOnUiThread {
            if (ActivityUtils.getTopActivity().localClassName.contains("MainActivity")) {
                refreshAllList()
            }
        }
    }


    private val deleterConversationList = mutableListOf<String>()

    companion object {
        const val TYPE_ALL = 1
        const val TYPE_WAIT_REPLY = 2
        const val TYPE_SEVEN_DAY_IN = 3
        const val TYPE_INTIMACY_ALL = 4
        const val TYPE_FOLD = 5 //折叠的消息
        fun newsInstance(isDialog: Boolean, type: Int): IMConversationListFrg {
            val imListFragment = IMConversationListFrg()
            val bundle = Bundle()
            bundle.putBoolean(IntentKeyConfig.IS_SHOW, isDialog)
            bundle.putInt(IntentKeyConfig.TYPE, type)
            imListFragment.arguments = bundle
            return imListFragment
        }
    }
}