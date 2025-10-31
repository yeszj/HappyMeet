package cn.yanhu.agora.ui.liveRoom

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import cn.yanhu.agora.adapter.liveRoom.LiveRoomChatMessageAdapter
import cn.yanhu.agora.bean.ChatRoomMsgInfo
import cn.yanhu.agora.databinding.FrgBaseLiveRoomBinding
import cn.yanhu.agora.listener.LiveRoomComponent
import cn.yanhu.agora.pop.SendMessagePop
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.logComToFile
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.ChatRoomGiftMsg
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.imchat.manager.ImUserManager
import com.blankj.utilcode.util.GsonUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.hyphenate.EMCallBack
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMTextMessageBody
import com.lxj.xpopup.core.BasePopupView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import cn.yanhu.agora.R
import cn.yanhu.agora.manager.monitor.MemoryLeakMonitor
import com.hyphenate.chat.EMChatRoom
import com.lxj.xpopup.interfaces.SimpleCallback
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.Stack
import java.util.WeakHashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author: zhengjun
 * created: 2025/10/27
 * desc:
 */
// OptimizedChatMessageManager.kt
class ChatMessageManager(
    private val context: Context,
    private val viewModel: LiveRoomViewModel,
    private val binding: FrgBaseLiveRoomBinding
) : LiveRoomComponent {

    companion object {
        private const val MAX_CHAT_MESSAGES = 500
        private const val MESSAGE_POOL_SIZE = 50
        private const val CLEANUP_INTERVAL = 60000L // 1分钟
        private const val PENDING_MESSAGE_TIMEOUT = 120000L // 2分钟
        private const val CALLBACK_TIMEOUT = 300000L // 5分钟
    }

    private lateinit var roomInfo: RoomDetailInfo
    private val chatRoomMsgAdapter by lazy { LiveRoomChatMessageAdapter() }
    private var messageDialog: SendMessagePop? = null
    private var ifMute = false

    // 使用弱引用避免内存泄漏
    private val messageCallbacks = WeakHashMap<EMMessage, EMCallBack>()
    private val pendingMessages = ConcurrentHashMap<String, EMMessage>()

    // 消息池，避免频繁创建对象
    private val messagePool = MessagePool(MESSAGE_POOL_SIZE)

    // 复用 Handler 对象
    private val mainHandler = Handler(Looper.getMainLooper())
    private val cleanupRunnable = Runnable { cleanupResources() }

    // 消息处理队列
    private val messageProcessor = MessageProcessor()

    // Coroutine Scope
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 监听器
    private var onMessageListener: OnMessageListener? = null

    // 性能监控
    private var lastMessageProcessTime = 0L
    private var processedMessageCount = 0

    interface OnMessageListener {
        fun onUserClicked(userId: String)
        fun onUserLongClicked(userInfo: BaseUserInfo)
        fun onGiftMessageReceived(giftMsg: ChatRoomGiftMsg)
        fun onWelcomeMessageReceived(userInfo: UserDetailInfo)
        fun onChatRoomDestroyed()
        fun onInputStateChanged(isShowing: Boolean, height: Int)
        fun onMessageProcessed(count: Int, duration: Long)
    }

    fun setOnMessageListener(listener: OnMessageListener) {
        this.onMessageListener = listener
    }

    override fun initialize(roomInfo: RoomDetailInfo) {
        this.roomInfo = roomInfo
        setupChatAdapter()
        checkIfMute()
        initChatMsgAdapterData()
        startCleanupTask()
        MemoryLeakMonitor.watchObject(this, "ChatMessageManager")

        logComToFile("ChatMessageManager", "Initialized for room: ${roomInfo.roomId}")
    }

    private fun setupChatAdapter() {
        binding.rvChat.adapter = chatRoomMsgAdapter
        binding.rvChat.itemAnimator?.changeDuration = 0

        // 优化RecyclerView性能
        binding.rvChat.setHasFixedSize(true)
        binding.rvChat.setItemViewCacheSize(20)
        binding.rvChat.recycledViewPool.setMaxRecycledViews(0, 20)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        chatRoomMsgAdapter.addOnItemChildClickListener(R.id.userAvatar,
            object : BaseQuickAdapter.OnItemChildClickListener<ChatRoomMsgInfo> {
                override fun onItemClick(
                    adapter: BaseQuickAdapter<ChatRoomMsgInfo, *>,
                    view: View,
                    position: Int
                ) {
                    val item = chatRoomMsgAdapter.getItem(position) ?: return
                    item.sendUserInfo?.userId?.let { userId ->
                        onMessageListener?.onUserClicked(userId)
                    }
                }
            })

        chatRoomMsgAdapter.addOnItemChildLongClickListener(R.id.userAvatar,
            object : BaseQuickAdapter.OnItemChildLongClickListener<ChatRoomMsgInfo> {
                override fun onItemLongClick(
                    adapter: BaseQuickAdapter<ChatRoomMsgInfo, *>,
                    view: View,
                    position: Int
                ): Boolean {
                    val item = chatRoomMsgAdapter.getItem(position) ?: return true
                    item.sendUserInfo?.let { userInfo ->
                        onMessageListener?.onUserLongClicked(userInfo)
                    }
                    return true
                }
            })
    }

    private fun checkIfMute() {
        EMClient.getInstance().chatroomManager()
            .asyncCheckIfInMuteList(roomInfo.uid, object : EMValueCallBack<Boolean> {
                override fun onSuccess(inMuteList: Boolean) {
                    ifMute = inMuteList
                    logComToFile("ChatMessageManager", "Mute status: $ifMute")
                }

                override fun onError(error: Int, errorMsg: String) {
                    logComToFile("ChatMessageManager", "Check mute error: $error - $errorMsg")
                }
            })
    }

    private fun initChatMsgAdapterData() {
        val list: MutableList<ChatRoomMsgInfo> = mutableListOf()
        list.add(ChatRoomMsgInfo(
            ChatRoomMsgInfo.ITEM_SYSTEM_TYPE,
            "平台公示：平台只提供交友介绍认识的服务，我们提倡文明直播、积极阳光交友，严禁涉黄、涉政、涉恐、低俗、辱骂等行为。发现违规行为将被封禁。保护网络绿色环境，从你我做起。",
            null
        ))
        chatRoomMsgAdapter.submitList(list)
    }

    fun updateReceivedMsg(messages: List<EMMessage>) {
        if (messages.isEmpty()) return

        val startTime = System.currentTimeMillis()
        messageProcessor.processMessages(messages) { processedMessages ->
            val processTime = System.currentTimeMillis() - startTime
            processedMessageCount += processedMessages.size

            // 使用复用的 mainHandler
            mainHandler.post {
                addMessagesToAdapter(processedMessages)
                onMessageListener?.onMessageProcessed(processedMessages.size, processTime)
            }
        }
    }

    private fun addMessagesToAdapter(newMessages: List<ChatRoomMsgInfo>) {
        if (newMessages.isEmpty()) return

        val currentList = chatRoomMsgAdapter.items.toMutableList()

        // 限制总消息数量，防止内存无限增长
        if (currentList.size + newMessages.size > MAX_CHAT_MESSAGES) {
            val overflow = (currentList.size + newMessages.size) - MAX_CHAT_MESSAGES
            if (overflow > 0) {
                // 回收被移除的消息对象
                currentList.subList(0, overflow).forEach { messagePool.recycleMessage(it) }
                currentList.subList(0, overflow).clear()
            }
        }

        currentList.addAll(newMessages)
        chatRoomMsgAdapter.submitList(currentList)

        scrollChatToBottom(50)
    }

    fun showInputDialog(user: BaseUserInfo?, isKeyboard: Boolean) {
        if (CommonUtils.isPopShow(messageDialog)) return

        val currentUser = user
        messageDialog = SendMessagePop.showDialog(
            context,
            isKeyboard,
            currentUser,
            object : SendMessagePop.OnMessageSendListener {
                override fun onShowEmoji(height: Int) {
                    onMessageListener?.onInputStateChanged(true, height)
                }

                override fun onSendMessage(content: String, hasAlt: Boolean) {
                    sendMessage(content, if (hasAlt) currentUser else null)
                }

                override fun onSendEmoji(url: String) {
                    sendEmoji(url)
                }
            },
            object : SimpleCallback() {
                override fun onDismiss(popupView: BasePopupView) {
                    onMessageListener?.onInputStateChanged(false, 0)
                    messageDialog = null
                }

                override fun onKeyBoardStateChanged(popupView: BasePopupView, height: Int) {
                    onMessageListener?.onInputStateChanged(height != 0, height)
                }
            })

        MemoryLeakMonitor.watchObject(messageDialog!!, "SendMessagePop")
    }

    fun sendMessage(content: String, altUser: BaseUserInfo? = null) {
        if (ifMute) {
            showToast("你已被禁言")
            return
        }

        if (content.trim().isEmpty()) {
            return
        }

        coroutineScope.launch(Dispatchers.IO) {
            val message = EMMessage.createTextSendMessage(content, roomInfo.uid)
            try {
                val messageId = message.msgId

                // 保存到待处理列表，避免重复发送
                if (pendingMessages.containsKey(messageId)) {
                    return@launch
                }
                pendingMessages[messageId] = message

                setMessageAttributes(message, altUser, ChatRoomMsgInfo.ITEM_DEFAULT_TYPE)
                setupOptimizedMessageCallback(message, content, altUser)

                EMClient.getInstance().chatManager().sendMessage(message)

            } catch (e: Exception) {
                // 切换到主线程显示Toast
                mainHandler.post {
                    pendingMessages.remove(message?.msgId)
                    logComToFile("SendMessageError", "Send message error: ${e.message}")
                    showToast("发送失败，请重试")
                }
            }
        }
    }

    private fun sendEmoji(url: String) {
        sendMessage(url, null, ChatRoomMsgInfo.ITEM_EMOJI_TYPE)
    }

    private fun sendMessage(content: String, altUser: BaseUserInfo?, sendType: Int) {
        coroutineScope.launch(Dispatchers.IO) {
            val message = EMMessage.createTextSendMessage(content, roomInfo.uid)
            try {
                val messageId = message.msgId

                if (pendingMessages.containsKey(messageId)) {
                    return@launch
                }
                pendingMessages[messageId] = message

                setMessageAttributes(message, altUser, sendType)
                setupOptimizedMessageCallback(message, content, altUser)

                EMClient.getInstance().chatManager().sendMessage(message)

            } catch (e: Exception) {
                mainHandler.post {
                    pendingMessages.remove(message?.msgId)
                    logComToFile("SendMessageError", "Send emoji error: ${e.message}")
                }
            }
        }
    }

    private fun setMessageAttributes(
        message: EMMessage,
        altUser: BaseUserInfo?,
        sendType: Int
    ) {
        if (altUser != null) {
            message.setAttribute(ChatConstant.ATE_USER_INFO, GsonUtils.toJson(altUser))
        }

        val selfUserInfo = ImUserManager.getSelfUserInfo()
        message.setAttribute(ChatConstant.CUSTOM_SEND_USER_INFO, GsonUtils.toJson(selfUserInfo))
        message.setAttribute(ChatConstant.CUSTOM_SEND_TYPE, sendType)
        message.chatType = EMMessage.ChatType.ChatRoom
    }

    private fun setupOptimizedMessageCallback(
        message: EMMessage,
        content: String,
        altUser: BaseUserInfo?
    ) {
        val callback = object : EMCallBack {
            override fun onSuccess() {
                pendingMessages.remove(message.msgId)
                messageCallbacks.remove(message)

                // 使用复用的 mainHandler
                mainHandler.post {
                    addSentMessageToAdapter(message, content, altUser)
                }
            }

            override fun onError(code: Int, error: String) {
                pendingMessages.remove(message.msgId)
                messageCallbacks.remove(message)

                // 使用复用的 mainHandler
                mainHandler.post {
                    handleSendError(code, error)
                }
            }
        }

        messageCallbacks[message] = callback
        message.setMessageStatusCallback(callback)
    }

    private fun addSentMessageToAdapter(message: EMMessage, content: String, altUser: BaseUserInfo?) {
        val selfUserInfo = ImUserManager.getSelfUserInfo()
        val chatRoomMsgInfo = messagePool.obtainMessage(
            message.getIntAttribute(ChatConstant.CUSTOM_SEND_TYPE, 0),
            content,
            selfUserInfo,
            altUser
        )

        chatRoomMsgAdapter.add(chatRoomMsgInfo)
        scrollChatToBottom(50)

        // 延迟回收消息对象，避免立即回收导致显示问题
        mainHandler.postDelayed({
            messagePool.recycleMessage(chatRoomMsgInfo)
        }, 10000) // 10秒后回收
    }

    private fun handleSendError(code: Int, error: String) {
        logComToFile("SendMessageError", "Code: $code, Error: $error")

        when (code) {
            702 -> {
                // 需要重新加入聊天室
                showToast("发送失败，正在重新连接...")
                rejoinChatRoom()
            }
            else -> {
                showToast("发送失败: $error")
            }
        }
    }

    private fun rejoinChatRoom() {
        // 实现重新加入聊天室的逻辑
        coroutineScope.launch(Dispatchers.IO) {
            try {
                EMClient.getInstance().chatroomManager().joinChatRoom(roomInfo.uid,
                    object : EMValueCallBack<EMChatRoom> {
                        override fun onSuccess(value: EMChatRoom?) {
                            mainHandler.post {
                                showToast("重新连接成功")
                            }
                        }
                        override fun onError(error: Int, errorMsg: String?) {
                            mainHandler.post {
                                showToast("重新连接失败")
                            }
                        }
                    })
            } catch (e: Exception) {
                mainHandler.post {
                    showToast("重新连接异常")
                }
            }
        }
    }

    fun scrollChatToBottom(duration: Long) {
        mainHandler.postDelayed({
            if (chatRoomMsgAdapter.itemCount > 0) {
                binding.rvChat.scrollToPosition(chatRoomMsgAdapter.itemCount - 1)
            }
        }, duration)
    }

    fun setUnReadMsgCount() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val unreadMessageCount = EMClient.getInstance().chatManager().unreadMessageCount
                mainHandler.post {
                    if (unreadMessageCount > 0) {
                        val count = if (unreadMessageCount > 99) "99+" else unreadMessageCount.toString()
                        binding.tvChatUnReadCount.text = count
                        binding.tvChatUnReadCount.visibility = View.VISIBLE
                    } else {
                        binding.tvChatUnReadCount.visibility = View.INVISIBLE
                    }
                }
            } catch (e: Exception) {
                logComToFile("UnreadCountError", "Get unread count error: ${e.message}")
            }
        }
    }

    private fun startCleanupTask() {
        mainHandler.postDelayed(cleanupRunnable, CLEANUP_INTERVAL)
    }

    private fun cleanupResources() {
        val now = System.currentTimeMillis()

        // 清理过期的回调
        val callbackIterator = messageCallbacks.entries.iterator()
        var removedCallbacks = 0
        while (callbackIterator.hasNext()) {
            val entry = callbackIterator.next()
            if (now - entry.key.msgTime > CALLBACK_TIMEOUT) {
                callbackIterator.remove()
                removedCallbacks++
            }
        }

        // 清理超时的待处理消息
        val pendingIterator = pendingMessages.entries.iterator()
        var removedPending = 0
        while (pendingIterator.hasNext()) {
            val entry = pendingIterator.next()
            if (now - entry.value.msgTime > PENDING_MESSAGE_TIMEOUT) {
                pendingIterator.remove()
                removedPending++
            }
        }

        // 强制垃圾回收（在后台线程执行）
        if (removedCallbacks > 0 || removedPending > 0) {
            coroutineScope.launch(Dispatchers.IO) {
                System.gc()
            }
        }

        if (removedCallbacks > 0 || removedPending > 0) {
            logComToFile("Cleanup", "Removed $removedCallbacks callbacks, $removedPending pending messages")
        }

        // 重新启动清理任务
        startCleanupTask()
    }

    override fun onResume() {
        // 恢复时检查未读消息
        setUnReadMsgCount()
    }

    override fun onPause() {
        // 暂停时清理一些临时资源
        messageDialog?.dismiss()
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(cleanupRunnable)
        messageDialog?.dismiss()
        messageDialog = null
    }

    override fun release() {
        mainHandler.removeCallbacksAndMessages(null)
        coroutineScope.cancel() // 取消所有协程

        // 清理所有资源
        messageCallbacks.clear()
        pendingMessages.clear()
        messagePool.clear()

        logComToFile("ChatMessageManager", "Released all resources")
    }

    // 消息处理器 - 修复 Handler 复用问题
    private inner class MessageProcessor {

        private val processorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        fun processMessages(messages: List<EMMessage>, callback: (List<ChatRoomMsgInfo>) -> Unit) {
            processorScope.launch {
                val processedMessages = ArrayList<ChatRoomMsgInfo>()
                var processedCount = 0

                for (message in messages) {
                    if (processedCount >= 50) { // 限制单次处理数量
                        break
                    }

                    if (message.conversationId() != roomInfo.uid) {
                        continue
                    }

                    if (message.body is EMTextMessageBody) {
                        val chatMsg = createChatMessage(message)
                        chatMsg?.let {
                            processedMessages.add(it)
                            processedCount++
                        }
                    }
                }

                callback(processedMessages)
            }
        }

        private fun createChatMessage(message: EMMessage): ChatRoomMsgInfo? {
            return try {
                val content = (message.body as EMTextMessageBody).message
                val sendType = message.getIntAttribute(ChatConstant.CUSTOM_SEND_TYPE)
                val userInfoJson = message.getStringAttribute(ChatConstant.CUSTOM_SEND_USER_INFO)
                val altInfo = message.getStringAttribute(ChatConstant.ATE_USER_INFO, "")

                val sendUserInfo = parseUserInfoSafely(userInfoJson)
                val altUserInfo = if (!TextUtils.isEmpty(altInfo)) {
                    parseUserInfoSafely(altInfo)
                } else null

                val chatMsg = messagePool.obtainMessage(sendType, content, sendUserInfo, altUserInfo)

                // 处理特殊消息
                processSpecialMessage(chatMsg)

                chatMsg
            } catch (e: Exception) {
                logComToFile("ChatMessageError", "Create message error: ${e.message}")
                null
            }
        }

        private fun parseUserInfoSafely(json: String): UserDetailInfo? {
            return try {
                GsonUtils.fromJson(json, UserDetailInfo::class.java)
            } catch (e: Exception) {
                logComToFile("ParseUserError", "Parse user info error: ${e.message}")
                null
            }
        }

        private fun processSpecialMessage(message: ChatRoomMsgInfo) {
            when (message.type) {
                ChatRoomMsgInfo.ITEM_GIFT_TYPE -> {
                    processGiftMessage(message)
                }
                ChatRoomMsgInfo.ITEM_WELCOME_TYPE -> {
                    processWelcomeMessage(message)
                }
            }
        }

        private fun processGiftMessage(message: ChatRoomMsgInfo) {
            try {
                val giftMsg = GsonUtils.fromJson(message.content, ChatRoomGiftMsg::class.java)
                // 使用复用的 mainHandler 切换到主线程调用监听器
                mainHandler.post {
                    onMessageListener?.onGiftMessageReceived(giftMsg)
                }
            } catch (e: Exception) {
                logComToFile("GiftMessageError", "Parse gift message error: ${e.message}")
            }
        }

        private fun processWelcomeMessage(message: ChatRoomMsgInfo) {
            message.sendUserInfo?.let { userInfo ->
                // 使用复用的 mainHandler 切换到主线程调用监听器
                mainHandler.post {
                    onMessageListener?.onWelcomeMessageReceived(userInfo)
                }
            }
        }
    }

    // 消息对象池
    private class MessagePool(private val maxPoolSize: Int) {
        private val pool = Stack<ChatRoomMsgInfo>()
        private val createdCount = AtomicInteger(0)
        private val recycledCount = AtomicInteger(0)

        fun obtainMessage(
            type: Int,
            content: String,
            userInfo: UserDetailInfo?,
            altUser: BaseUserInfo?
        ): ChatRoomMsgInfo {
            val message = if (pool.isNotEmpty()) {
                pool.pop().apply {
                    this.type = type
                    this.content = content
                    this.sendUserInfo = userInfo
                    this.altUser = altUser
                }
            } else {
                createdCount.incrementAndGet()
                ChatRoomMsgInfo(type, content, userInfo, altUser)
            }

            return message
        }

        fun recycleMessage(message: ChatRoomMsgInfo) {
            if (pool.size < maxPoolSize) {
                // 重置消息内容，避免持有引用
                message.content = ""
                message.sendUserInfo = null
                message.altUser = null
                pool.push(message)
                recycledCount.incrementAndGet()
            }
        }

        fun clear() {
            pool.clear()
            logComToFile("MessagePool", "Cleared pool. Created: $createdCount, Recycled: $recycledCount")
        }

        fun getStats(): String {
            return "Pool: ${pool.size}/$maxPoolSize, Created: $createdCount, Recycled: $recycledCount"
        }
    }

    // 工具方法
    private fun showToast(message: String) {
        mainHandler.post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // 性能监控方法
    fun getPerformanceStats(): String {
        return """
            ChatMessageManager Stats:
            - Processed Messages: $processedMessageCount
            - Pending Messages: ${pendingMessages.size}
            - Active Callbacks: ${messageCallbacks.size}
            - Message Pool: ${messagePool.getStats()}
            - Adapter Items: ${chatRoomMsgAdapter.itemCount}
        """.trimIndent()
    }

    fun forceCleanup() {
        cleanupResources()
    }
}