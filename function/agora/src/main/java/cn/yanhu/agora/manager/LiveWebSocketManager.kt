package cn.yanhu.agora.manager

import android.Manifest
import android.annotation.SuppressLint
import android.text.TextUtils
import androidx.annotation.RequiresPermission
import cn.yanhu.agora.bean.GiftSettleInfo
import cn.yanhu.agora.bean.SocketSeatInfo
import cn.yanhu.baselib.utils.ext.logComToFile
import cn.yanhu.baselib.utils.ext.logD
import cn.yanhu.baselib.utils.ext.logI
import cn.yanhu.commonres.bean.ChatRoomGiftMsg
import cn.yanhu.commonres.bean.RoomPkInfo
import cn.yanhu.commonres.manager.AppCacheManager
import cn.zj.netrequest.application.ApplicationProxy
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ThreadUtils
import com.neovisionaries.ws.client.*
import org.json.JSONObject
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock

class LiveWebSocketManager private constructor() {

    companion object {
        const val TYPE_REFRESH_SEAT = "combo_gift_preview"
        const val TYPE_UPDATE_PK = "pk_progress_update"
        const val TYPE_COMBO_RES = "combo_gift_preview_response"
        const val TYPE_ERROR = "error"
        const val TYPE_COMBO_SETTLE_RES = "combo_gift_settle_response"
        private const val TAG = "LiveWebSocketManager"

        @Volatile
        private var instance: LiveWebSocketManager? = null

        fun getInstance(): LiveWebSocketManager {
            return instance ?: synchronized(this) {
                instance ?: LiveWebSocketManager().also { instance = it }
            }
        }
    }

    // WebSocket 连接
    private var webSocket: WebSocket? = null
    private var webSocketFactory: WebSocketFactory? = null

    // 连接状态
    private val isConnected = AtomicBoolean(false)
    private val isConnecting = AtomicBoolean(false)
    private var roomId = ""

    // 重连管理
    private val reconnectAttempts = AtomicInteger(0)
    private val maxReconnectAttempts = 20
    private val reconnectExecutor by lazy {
        ScheduledThreadPoolExecutor(1).apply {
            removeOnCancelPolicy = true
        }
    }

    // 心跳管理
    private val heartbeatInterval = 10000L // 10秒
    private var heartbeatTask: ScheduledFuture<*>? = null

    // 消息队列（发送失败的消息）
    private val messageQueue = LinkedBlockingQueue<MessageItem>()
    private val messageExecutor by lazy {
        Executors.newSingleThreadExecutor { r ->
            Thread(r, "WebSocket-Message-Processor").apply {
                isDaemon = true
            }
        }
    }

    // 监听器管理
    private val listeners = CopyOnWriteArraySet<WebSocketListener>()
    private val listenerLock = ReentrantReadWriteLock()

    // 消息类型定义
    sealed class MessageType {
        object HEARTBEAT : MessageType()
        object JOIN_ROOM : MessageType()
        object LEAVE_ROOM : MessageType()
        object GIFT_SINGLE : MessageType()
        object GIFT_BATCH : MessageType()
        object CUSTOM : MessageType()
    }

    data class MessageItem(
        val message: String,
        val type: MessageType = MessageType.CUSTOM,
        val priority: Boolean = false,
        val retryCount: Int = 0,
        val timestamp: Long = System.currentTimeMillis()
    )

    interface WebSocketListener {
        fun onConnected()
        fun onMessage(message: String)
        fun onDisconnected(reason: String, code: Int)
        fun onError(errorCode: Int, error: String)
        fun onRefreshSeat(roomId: String, seatInfo: SocketSeatInfo)
        fun onRefreshPk(roomId: String, pkInfo: RoomPkInfo)
        fun onRefreshComboCntId(roomId: String, sendCntId: String)
        fun onComboSettle(roomId: String, settleInfo: GiftSettleInfo)


    }

    private var onNetWorkChangeListener: NetworkUtils.OnNetworkStatusChangedListener? = null

    // 初始化
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun init(roomId: String) {
        this.roomId = roomId
        webSocketFactory = WebSocketFactory().apply {
            connectionTimeout = 10000
            socketTimeout = 15000
            setVerifyHostname(false) // 根据需求调整
        }
        connect()
        startMessageQueueProcessor()

        onNetWorkChangeListener = object : NetworkUtils.OnNetworkStatusChangedListener {
            override fun onDisconnected() {
            }

            override fun onConnected(networkType: NetworkUtils.NetworkType?) {
                if (instance==null){
                    return
                }
                logInfo("网络重新连接成功，开始重连")
                reconnectAttempts.set(0)
                scheduleReconnect()
            }
        }
        NetworkUtils.registerNetworkStatusChangedListener(onNetWorkChangeListener)
    }

    private fun connect() {
        if (isConnecting.get() || isConnected.get()) {
            logDebug("已在连接或已连接，忽略重复连接请求")
            return
        }

        isConnecting.set(true)

        try {
            val baseServerAddress = ApplicationProxy.instance.getServeAddress()
            val connectUrl = "${baseServerAddress}ws/gift/combo?userId=${AppCacheManager.userId}"
            val url = connectUrl.replace("http", "ws")

            webSocket = webSocketFactory?.createSocket(url)?.apply {
                addListener(createWebSocketListener())

                // 添加请求头
                val headers = ApplicationProxy.instance.getHead()
                headers.forEach { (key, value) ->
                    if (!TextUtils.isEmpty(value)) {
                        logDebug("添加请求头: $key=$value")
                        addHeader(key, value!!)
                    }
                }

                connectAsynchronously()
            }

            logInfo("开始WebSocket连接: $url")
        } catch (e: Exception) {
            isConnecting.set(false)
            handleConnectionError("连接初始化失败", e)
            scheduleReconnect()
        }
    }

    private fun createWebSocketListener(): WebSocketAdapter {
        return object : WebSocketAdapter() {
            override fun onConnected(
                websocket: WebSocket, headers: Map<String, List<String>>
            ) {
                isConnecting.set(false)
                isConnected.set(true)
                reconnectAttempts.set(0)

                logInfo("WebSocket连接成功")
                notifyConnected()

                // 连接成功后处理队列消息
                flushMessageQueue()
                startHeartbeat()
                joinRoom(roomId)
            }

            override fun onTextMessage(websocket: WebSocket, text: String) {
                logDebug("收到文本消息: $text")
                processMessage(text)
                notifyMessage(text)
            }

            override fun onBinaryMessage(websocket: WebSocket, binary: ByteArray) {
            }

            override fun onDisconnected(
                websocket: WebSocket,
                serverCloseFrame: WebSocketFrame,
                clientCloseFrame: WebSocketFrame,
                closedByServer: Boolean
            ) {
                isConnected.set(false)
                isConnecting.set(false)

                val closeCode = if (closedByServer) {
                    serverCloseFrame.closeCode
                } else {
                    clientCloseFrame.closeCode
                }

                val reason = if (closedByServer) {
                    "服务器关闭连接: ${serverCloseFrame.closeCode} - ${serverCloseFrame.closeReason}"
                } else {
                    "客户端关闭连接"
                }

                logError("连接断开: $reason")
                stopHeartbeat()
                notifyDisconnected(reason, closeCode)

                // 非正常关闭且非主动断开，尝试重连
                if (closeCode != 1000) {
                    scheduleReconnect()
                }
            }

            override fun onError(websocket: WebSocket, cause: WebSocketException) {
                logError("WebSocket错误", cause)
                isConnecting.set(false)
                isConnected.set(false)
                scheduleReconnect()
            }

            override fun onUnexpectedError(websocket: WebSocket, cause: WebSocketException) {
                logError("WebSocket意外错误", cause)
            }

            override fun onSendError(
                websocket: WebSocket, cause: WebSocketException, frame: WebSocketFrame
            ) {
                logError("发送消息失败", cause)
                // 重新加入队列
                if (frame.isTextFrame) {
                    val message = frame.payloadText
                    messageQueue.offer(MessageItem(message, retryCount = 1))
                }
            }
        }
    }

    private fun processMessage(message: String) {
        try {
            val json = JSONObject(message)
            val type = json.optString("type")

            when (type) {
                TYPE_REFRESH_SEAT -> {
                    handleRefreshSeatMessage(json)
                }

                TYPE_UPDATE_PK -> {
                    handleRefreshPkMessage(json)
                }

                TYPE_COMBO_RES -> {
                    //连击响应
                    handleComboMessage(json)
                }

                TYPE_COMBO_SETTLE_RES -> {
                    //送礼结算
                    handleComboSettleMessage(json)
                }

                TYPE_ERROR -> {
                    handleErrorMessage(json)
                }

                else -> {
                    logDebug("无需处理的消息类型: $type")
                }
            }
        } catch (e: Exception) {
            logError("处理消息失败", e)
        }
    }

    private fun handleRefreshSeatMessage(json: JSONObject) {
        try {
            val dataJson = json.optJSONObject("data") ?: return logError("座位信息数据为空")

            val seatInfo = GsonUtils.fromJson<SocketSeatInfo>(
                dataJson.toString(), SocketSeatInfo::class.java
            )
            val roomId = json.optString("roomId")
            seatInfo?.let { info ->
                notifyRefreshSeat(roomId, info)
            } ?: logError("座位信息解析失败")
        } catch (e: Exception) {
            logError("处理座位信息失败", e)
        }
    }

    private fun handleComboSettleMessage(json: JSONObject) {
        try {
            val dataJson = json.optJSONObject("data") ?: return logError("连击信息数据为空")
            val totalNum =
                GsonUtils.fromJson<GiftSettleInfo>(dataJson.toString(), GiftSettleInfo::class.java)
            notifyComboSettle(totalNum)
        } catch (e: Exception) {
            logError("处理连击信息失败", e)
        }
    }

    private fun handleComboMessage(json: JSONObject) {
        try {
            val dataJson = json.optJSONObject("data") ?: return logError("连击信息数据为空")
            val sendCntId = dataJson.optString("sendCntId")
            notifyComboCntId(roomId, sendCntId)
        } catch (e: Exception) {
            logError("处理连击信息失败", e)
        }
    }

    private fun handleRefreshPkMessage(json: JSONObject) {
        try {
            val dataJson = json.optJSONObject("data") ?: return logError("PK信息数据为空")

            val pkInfo = GsonUtils.fromJson<RoomPkInfo>(
                dataJson.toString(), RoomPkInfo::class.java
            )
            val roomId = json.optString("roomId")
            pkInfo?.let {
                notifyRefreshPk(roomId, it)
            } ?: logError("PK信息解析失败")
        } catch (e: Exception) {
            logError("处理PK信息失败", e)
        }
    }

    private fun handleErrorMessage(json: JSONObject) {
        val errorCode = json.optInt("code", -1)
        val errorMsg = json.optString("message", "未知错误")
        notifyError(errorCode, errorMsg)
        logError("服务器返回错误: code=$errorCode, message=$errorMsg")
    }

    // 发送消息
    fun sendMessage(message: String, priority: Boolean = false): Boolean {
        return if (isConnected.get()) {
            try {
                webSocket?.sendText(message)
                true
            } catch (e: Exception) {
                logError("发送消息失败", e)
                // 加入队列重试
                messageQueue.offer(MessageItem(message, priority = priority, retryCount = 1))
                false
            }
        } else {
            // 未连接，加入队列
            messageQueue.offer(MessageItem(message, priority = priority))
            false
        }
    }

    // 发送连击
    fun sendComboGift(chatRoomGiftMsg: ChatRoomGiftMsg): Boolean {
        val message = JSONObject().apply {
            put("action", "combo_gift_send")
            put("giftId", chatRoomGiftMsg.giftInfo.id)
            put("source", chatRoomGiftMsg.source)
            put("roomId", roomId)
            put("toUid", chatRoomGiftMsg.targetUserInfo.userId)
            put("sendCntId", chatRoomGiftMsg.sendCntId)
        }
        logDebug("发送连击消息: $message")
        return sendMessage(message.toString())
    }

    // 发送连击礼物结算
    fun sendComboGiftSettle(sendCntId: String): Boolean {
        val message = JSONObject().apply {
            put("action", "combo_gift_settle")
            put("sendCntId", sendCntId)
        }
        logDebug("发送连击结算消息: $message")
        return sendMessage(message.toString())
    }

    // 加入直播间
    private fun joinRoom(roomId: String) {
        val message = JSONObject().apply {
            put("action", "subscribe")
            put("roomId", roomId)
            put("timestamp", System.currentTimeMillis())
        }
        sendMessage(message.toString(), priority = true)
    }

    // 离开直播间
    fun leaveRoom() {
        val message = JSONObject().apply {
            put("action", "unsubscribe")
            put("roomId", roomId)
            put("timestamp", System.currentTimeMillis())
        }
        sendMessage(message.toString())
    }

    // 心跳管理
    private fun startHeartbeat() {
        stopHeartbeat()

        heartbeatTask = reconnectExecutor.scheduleWithFixedDelay({
            if (isConnected.get()) {
                val heartbeat = JSONObject().apply {
                    put("action", "ping")
                    put("timestamp", System.currentTimeMillis())
                }
                sendMessage(heartbeat.toString())
                logDebug("发送心跳")
            }
        }, 0, heartbeatInterval, TimeUnit.MILLISECONDS)
    }

    private fun stopHeartbeat() {
        heartbeatTask?.let {
            it.cancel(true)
            heartbeatTask = null
        }
    }

    // 重连机制
    @SuppressLint("MissingPermission")
    private fun scheduleReconnect() {
        if (reconnectExecutor.isShutdown){
            return
        }
        if (!NetworkUtils.isConnected()) {
            isConnecting.set(false)
            logInfo("网络不可用，停止重连")
            return
        }
        if (reconnectAttempts.get() >= maxReconnectAttempts) {
            isConnecting.set(false)
            logInfo("达到最大重连次数($maxReconnectAttempts)，停止重连")
            return
        }

        val attempt = reconnectAttempts.incrementAndGet()
        val delay = calculateReconnectDelay(attempt)

        logInfo("第${attempt}次重连，${delay}秒后尝试...")

        reconnectExecutor.schedule({
            if (!isConnected.get() && !isConnecting.get()) {
                logInfo("执行重连")
                connect()
            }
        }, delay, TimeUnit.SECONDS)

    }

    private fun calculateReconnectDelay(attempt: Int): Long {
        return when (attempt) {
            1 -> 1L
            2 -> 2L
            3 -> 5L
            else -> 10L
        }
    }

    // 消息队列处理
    private fun startMessageQueueProcessor() {
        messageExecutor.submit {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val item = messageQueue.take()

                    // 检查消息是否过期（超过5分钟）
                    if (System.currentTimeMillis() - item.timestamp > 5 * 60 * 1000L) {
                        logDebug("丢弃过期消息: ${item.message.take(100)}...")
                        continue
                    }

                    // 检查重试次数
                    if (item.retryCount > 3) {
                        logDebug("超过最大重试次数，丢弃消息")
                        continue
                    }

                    if (isConnected.get()) {
                        webSocket?.sendText(item.message)
                        logDebug("发送队列消息: ${item.message.take(100)}...")

                        // 控制发送频率
                        Thread.sleep(if (item.priority) 10L else 50L)
                    } else {
                        // 未连接，重新加入队列
                        messageQueue.offer(item.copy(retryCount = item.retryCount + 1))
                        Thread.sleep(1000)
                    }
                } catch (e: InterruptedException) {
                    logInfo("消息队列处理器被中断")
                    break
                } catch (e: Exception) {
                    logError("处理消息队列失败", e)
                    Thread.sleep(1000)
                }
            }
        }
    }

    private fun flushMessageQueue() {
        var count = 0
        val tempList = mutableListOf<MessageItem>()

        // 先取出所有消息
        while (messageQueue.isNotEmpty() && count < 50) {
            messageQueue.poll()?.let { tempList.add(it) }
            count++
        }

        // 按优先级排序并发送
        tempList.sortedByDescending { it.priority }.forEach { item ->
            if (isConnected.get()) {
                try {
                    webSocket?.sendText(item.message)
                    Thread.sleep(20)
                } catch (e: Exception) {
                    // 发送失败，重新加入队列
                    messageQueue.offer(item.copy(retryCount = item.retryCount + 1))
                }
            }
        }
    }

    // 断开连接
    fun disconnect() {
        if (!isConnected.get() && !isConnecting.get()) {
            return
        }

        logInfo("开始断开WebSocket连接")

        leaveRoom()

        isConnecting.set(false)
        isConnected.set(false)

        stopHeartbeat()

        webSocket?.disconnect(1000, "Normal closure")
        webSocket = null

        // 清空队列
        messageQueue.clear()

        // 清理资源
        clearResources()

        logInfo("WebSocket连接已断开")
    }

    private fun clearResources() {
        listeners.clear()
        heartbeatTask = null
        reconnectAttempts.set(0)
    }

    // 监听器管理
    fun addListener(listener: WebSocketListener) {
        listenerLock.writeLock().lock()
        try {
            listeners.add(listener)
        } finally {
            listenerLock.writeLock().unlock()
        }
    }

    fun removeListener(listener: WebSocketListener) {
        listenerLock.writeLock().lock()
        try {
            listeners.remove(listener)
        } finally {
            listenerLock.writeLock().unlock()
        }
    }

    fun removeAllListeners() {
        listenerLock.writeLock().lock()
        try {
            listeners.clear()
        } finally {
            listenerLock.writeLock().unlock()
        }
    }

    // 通知方法
    private fun notifyConnected() {
        listenerLock.readLock().lock()
        try {
            listeners.forEach { safeRun { it.onConnected() } }
        } finally {
            listenerLock.readLock().unlock()
        }
    }

    private fun notifyMessage(message: String) {
        listenerLock.readLock().lock()
        try {
            listeners.forEach { safeRun { it.onMessage(message) } }
        } finally {
            listenerLock.readLock().unlock()
        }
    }

    private fun notifyRefreshSeat(roomId: String, socketSeatInfo: SocketSeatInfo) {
        listenerLock.readLock().lock()
        try {
            listeners.forEach { safeRun { it.onRefreshSeat(roomId, socketSeatInfo) } }
        } finally {
            listenerLock.readLock().unlock()
        }
    }

    private fun notifyRefreshPk(roomId: String, pkInfo: RoomPkInfo) {
        listenerLock.readLock().lock()
        try {
            listeners.forEach { safeRun { it.onRefreshPk(roomId, pkInfo) } }
        } finally {
            listenerLock.readLock().unlock()
        }
    }

    private fun notifyComboCntId(roomId: String, sendCntId: String) {
        listenerLock.readLock().lock()
        try {
            listeners.forEach { safeRun { it.onRefreshComboCntId(roomId, sendCntId) } }
        } finally {
            listenerLock.readLock().unlock()
        }
    }

    private fun notifyDisconnected(reason: String, code: Int) {
        listenerLock.readLock().lock()
        try {
            listeners.forEach { safeRun { it.onDisconnected(reason, code) } }
        } finally {
            listenerLock.readLock().unlock()
        }
    }

    private fun notifyError(errorCode: Int, error: String) {
        listenerLock.readLock().lock()
        try {
            listeners.forEach { safeRun { it.onError(errorCode, error) } }
        } finally {
            listenerLock.readLock().unlock()
        }
    }

    private fun notifyComboSettle(settleInfo: GiftSettleInfo) {
        listenerLock.readLock().lock()
        try {
            listeners.forEach { safeRun { it.onComboSettle(roomId, settleInfo) } }
        } finally {
            listenerLock.readLock().unlock()
        }
    }

    // 辅助方法
    private inline fun safeRun(crossinline block: () -> Unit) {
        try {
            ThreadUtils.runOnUiThread {
                block()
            }
        } catch (e: Exception) {
            logError("执行监听器回调失败", e)
        }
    }

    private fun handleConnectionError(message: String, e: Exception) {
        logError(message, e)
    }

    private fun logDebug(msg: String) {
        logD(TAG, msg)
    }

    private fun logInfo(msg: String) {
        logI(TAG, msg)
    }

    private fun logError(msg: String, e: Exception? = null) {
        val errorMsg = if (e != null) "$msg: ${e.message}" else msg
        logComToFile(TAG, errorMsg)
    }

    fun isConnected(): Boolean = isConnected.get()

    fun isConnecting(): Boolean = isConnecting.get()

    fun getRoomId(): String = roomId

    // 销毁方法
    fun destroy() {
        if (onNetWorkChangeListener != null) {
            NetworkUtils.unregisterNetworkStatusChangedListener(onNetWorkChangeListener)
        }
        disconnect()
        messageExecutor.shutdownNow()
        reconnectExecutor.shutdownNow()
        removeAllListeners()
        instance = null
    }
}