package cn.huanyuan.sweetlove

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.http.HttpResponseCache
import android.os.Build
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.sweetlove.func.ApplicationRouterImpl
import cn.huanyuan.sweetlove.func.manager.LoginResultManager
import cn.huanyuan.sweetlove.func.task.AppPopTask
import cn.huanyuan.sweetlove.func.task.ImChatMsgNotifyTask
import cn.huanyuan.sweetlove.net.HttpHeadInterceptor
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.agora.pop.ReceiveImCallInBgPop
import cn.yanhu.agora.pop.ReceiveImCallPop
import cn.yanhu.agora.ui.imphone.VideoPhoneActivity
import cn.yanhu.baselib.crash.CrashUtils
import cn.yanhu.baselib.crash.ExceptionHandler
import cn.yanhu.baselib.queue.TaskQueueManagerImpl
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.refresh.SmartRefreshProcessor
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.logComToFile
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.widget.router.ARouterWrapper
import cn.yanhu.commonres.bean.AppMsgNotifyInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.CmdMsgTypeConfig
import cn.yanhu.commonres.config.ConfigParamsManager
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.imchat.custom.chat.EaseCommonUtils
import cn.yanhu.imchat.db.ChatUserInfoManager
import cn.yanhu.imchat.manager.EMInitUtils
import cn.yanhu.imchat.manager.EaseHelper.initEaseUI
import cn.zj.netrequest.RetrofitUtil
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.factory.CustomizeGsonConverterFactory
import cn.zj.netrequest.https.CertificateManageHelper
import cn.zj.netrequest.intercept.HttpCacheInterceptor
import cn.zj.netrequest.intercept.HttpCommonInterceptor
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ProcessUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.gzuliyujiang.oaid.DeviceIdentifier
import com.hjq.toast.style.BlackToastStyle
import com.hyphenate.EMMessageListener
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMUserInfo
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.util.VersionUtils
import com.opensource.svgaplayer.SVGAParser
import com.pcl.sdklib.manager.SdkParamsManager
import com.umeng.commonsdk.UMConfigure
import com.umeng.socialize.PlatformConfig
import okhttp3.Interceptor
import org.litepal.LitePal
import xyz.doikki.videoplayer.ijk.IjkPlayerFactory
import xyz.doikki.videoplayer.player.VideoViewConfig
import xyz.doikki.videoplayer.player.VideoViewManager
import java.io.File


@Suppress("DEPRECATION")
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (ProcessUtils.isMainProcess()) {
            Utils.init(this)
            ApplicationProxy.instance = ApplicationRouterImpl.getInstance()
            init()
            if (!BuildConfig.DEBUG) {
                checkException()
            }
            registerAppStatusListener()
            registerActivityLifecycleCallBack()
            if (!AppCacheManager.isFirstOpenApp) {
                initSdk()
            }
        }
    }


    private fun registerAppStatusListener() {
        AppUtils.registerAppStatusChangedListener(object : Utils.OnAppStatusChangedListener {
            override fun onForeground(activity: Activity) {
            }

            override fun onBackground(activity: Activity) {
            }
        })
    }

    private fun registerActivityLifecycleCallBack() {
        ActivityUtils.addActivityLifecycleCallbacks(object : Utils.ActivityLifecycleCallbacks() {
            override fun onActivityCreated(activity: Activity) {
                super.onActivityCreated(activity)
                val activityName: String = activity::class.java.simpleName
                logcom("activityCreated", "启动页面：activityName=$activityName")
            }
        })
    }

    private fun checkException() {
        val sysExcepHandler = Thread.getDefaultUncaughtExceptionHandler()
        CrashUtils.install(this, object : ExceptionHandler() {
            override fun onUncaughtExceptionHappened(thread: Thread, throwable: Throwable) {
                logComToFile(
                    "AndroidRuntime",
                    "--->onUncaughtExceptionHappened:$thread<---${throwable.printStackTrace()}"
                )
            }

            override fun onBandageExceptionHappened(throwable: Throwable?) {
                throwable?.printStackTrace();//打印警告级别log，该throwable可能是最开始的bug导致的，无需关心
            }

            override fun onEnterSafeMode() {
                //DebugSafeModeUI.showSafeModeUI();
            }

            override fun onMayBeBlackScreen(e: Throwable?) {
                super.onMayBeBlackScreen(e)
                val thread = Looper.getMainLooper().thread
                logcom(
                    "AndroidRuntime",
                    "--->onUncaughtExceptionHappened:$thread<---${e?.printStackTrace()}"
                )
                //黑屏时建议直接杀死app
                sysExcepHandler?.uncaughtException(thread, RuntimeException("black screen"))
            }

        })

    }


    private fun setSvgCache() {
        val cacheDir = File(this.applicationContext.cacheDir, "http")
        HttpResponseCache.install(cacheDir, 1024 * 1024 * 128)
    }

    private fun init() {
        LitePal.initialize(this);
        //设置LOG开关，默认为false
        UMConfigure.setLogEnabled(false)
        //预初始化友盟
        UMConfigure.preInit(
            this,
            "6709deac667bfe33f3be884e",
            BuildConfig.FLAVOR
        )
        ARouterWrapper.init(this)
        initRetrofit()
        setVideoFactory()
        Looper.myQueue().addIdleHandler {
            setSvgCache()
            initToastStyle()
            false
        }
    }

    private fun setVideoFactory() {
        val create = IjkPlayerFactory.create()
        VideoViewManager.setConfig(
            VideoViewConfig.newBuilder()
                //使用使用IjkPlayer解码
                .setPlayerFactory(create).build()
        )
    }


    fun initSdk() {
        DeviceIdentifier.register(this)
        initImConfig()
        initUm()
        SVGAParser.shareParser().init(this)
    }

    /*
     * 初始化友盟
     * */
    private fun initUm() {
        UMConfigure.init(
            this,
            "6709deac667bfe33f3be884e",
            BuildConfig.FLAVOR,
            UMConfigure.DEVICE_TYPE_PHONE,
            ""
        )
        // 微信设置
        PlatformConfig.setWeixin(SdkParamsManager.WX_APP_ID, SdkParamsManager.WX_APP_SECRET)
        PlatformConfig.setWXFileProvider(BuildConfig.APPLICATION_ID + ".fileprovider")
        UMConfigure.getOaid(this) { oaid: String? ->
            if (!TextUtils.isEmpty(oaid)) {
                AppCacheManager.oaid = oaid!!
            }
//            if (isFirst) {
//                TurboAgent.onAppActive()
//                val map =
//                    HashMap<String, Any?>()
//                map["event_id"] = oaid
//                TraceUtils.getInstance().onEventObject("app_launch_first", map)
//            }
        }
    }


    private fun initImConfig() {
        EMInitUtils.initIM(this)
        initEaseUI()
        registerImMsgEvent()
    }

    fun reInitImSdk() {
        if (!EMClient.getInstance().isSdkInited) {
            initImConfig()
        }
        if (!EMClient.getInstance().isLoggedIn) {
            LoginResultManager.loginIM(null)
        }
    }

    /**
     * 全局系统通知监听(透传消息)
     */
    private fun registerImMsgEvent() {
        val emMessageListener = object : EMMessageListener {
            override fun onMessageReceived(messages: MutableList<EMMessage>) {
                //收到聊天消息
                if (ConfigParamsManager.HAS_LOAD_CHAT) {
                    ThreadUtils.getMainHandler().post {
                        getMsgUserInfo(messages)
                        LiveDataEventManager.sendLiveDataMessage(
                            EventBusKeyConfig.RECEIVE_CHAT_MSG,
                            messages
                        )
                    }
                }
            }

            override fun onCmdMessageReceived(messages: MutableList<EMMessage>) {
                //收到透传消息
                ThreadUtils.getMainHandler().post {
                    messages.forEach {
                        LiveDataEventManager.sendLiveDataMessage(
                            EventBusKeyConfig.RECEIVE_CMD_MSG,
                            it
                        )
                        dealCommonCmdMsg(it)
                    }
                }
            }

            override fun onMessageRecalled(messages: MutableList<EMMessage>) {
                super.onMessageRecalled(messages)
                ThreadUtils.getMainHandler().post {
                    LiveDataEventManager.sendLiveDataMessage(
                        EventBusKeyConfig.RECALLED_MSG,
                        messages
                    )
                }
            }
        }
        EMClient.getInstance().chatManager().addMessageListener(emMessageListener)

    }

    private val imMsgNotifyTaskManager = TaskQueueManagerImpl()
    private var addImMsgTime = 0L
    private fun getMsgUserInfo(messages: MutableList<EMMessage>) {
        val userArray = arrayOfNulls<String>(messages.size + 1)
        for (i in messages.indices) {
            userArray[i] = messages[i].from
        }
        EMClient.getInstance().userInfoManager()
            .fetchUserInfoByUserId(userArray, object : EMValueCallBack<Map<String?, EMUserInfo?>?> {
                override fun onSuccess(value: Map<String?, EMUserInfo?>?) {
                    if (value == null) {
                        return
                    }
                    for (message in messages) {
                        if (AppCacheManager.userId != message.to || message.chatType != EMMessage.ChatType.Chat) {
                            continue
                        }
                        if (AppUtils.isAppForeground()) {
                            val currentTimeMillis = System.currentTimeMillis()
                            val differTime = currentTimeMillis - addImMsgTime
                            val msgTime = message.msgTime
                            if (message.isOnlineState && differTime > 5 * 1000 && msgTime > currentTimeMillis - 5 * 1000) {
                                //5s弹一次 且消息时间是5s以内收到的(防止离线时收到的消息上线后逐一推送过来)
                                addImMsgTask(message, value)
                            }
                        } else {
                            val emUserInfo = value[message.from]
                            sendChannelNotify(message, emUserInfo)
                        }

                    }
                }

                override fun onError(error: Int, errorMsg: String) {
                }
            })
    }

    private var mNotificationManager: NotificationManager? = null

    private var notifyId = 0

    // 发送指定渠道的通知消息（包括消息标题和消息内容）
    private fun sendChannelNotify(message: EMMessage, emUserInfo: EMUserInfo?) {
        val topActivity = ActivityUtils.getTopActivity() ?: return
        if (mNotificationManager == null) {
            mNotificationManager =
                topActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        }
        val nickName: String = if (emUserInfo != null) {
            emUserInfo.nickname
        } else {
            "收到一条新消息"
        }
        // 创建一个跳转到活动页面的意图
        ApplicationProxy.instance.getLiveRoomActivity()
        val clickIntent = Intent()
        clickIntent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, message.conversationId())
        clickIntent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, 1)
        clickIntent.putExtra("title", nickName)
        clickIntent.setClassName(
            applicationContext.packageName,
            "cn.yanhu.imchat.ui.chat.ImChatActivity"
        )
        logcom("对方ID：" + message.conversationId())
        logcom("对方名称：$nickName")
        // 创建一个用于页面跳转的延迟意图
        val contentIntent = PendingIntent.getActivity(
            topActivity, message.conversationId().toInt(), clickIntent, PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val imMsgChannel = NotificationChannel(
                "highIMMsgId", "聊天消息通知", NotificationManager.IMPORTANCE_HIGH
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                imMsgChannel.setAllowBubbles(true)
                logcom("是否可浮动" + imMsgChannel.canBubble())
            }
            mNotificationManager!!.createNotificationChannel(imMsgChannel)
            val highIMMsgId: NotificationCompat.Builder =
                NotificationCompat.Builder(topActivity, "highIMMsgId")

            var avatarUrl: String? = ""
            if (emUserInfo != null) {
                avatarUrl = emUserInfo.avatarUrl
            }
            Glide.with(topActivity).asBitmap().load(
                if (TextUtils.isEmpty(avatarUrl)) ContextCompat.getDrawable(
                    topActivity, cn.yanhu.commonres.R.drawable.avatar_woman_normal
                ) else avatarUrl
            ).apply(RequestOptions.bitmapTransform(RoundedCorners(300)))
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap?>?
                    ) {
                        highIMMsgId.setContentIntent(contentIntent) // 设置内容的点击意图
                            .setAutoCancel(true) // 点击通知栏后是否自动清除该通知
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setSmallIcon(R.mipmap.ic_logo) // 设置应用名称左边的小图标
                            .setLargeIcon(resource).setContentTitle(nickName) // 设置通知栏里面的标题文本
                            .setContentText(
                                EaseCommonUtils.getMessageDigest(
                                    message, topActivity
                                )
                            ).priority = NotificationCompat.PRIORITY_MAX
                        mNotificationManager!!.notify(notifyId++, highIMMsgId.build())
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        } else {
            val notification: NotificationCompat.Builder =
                NotificationCompat.Builder(topActivity).setContentIntent(contentIntent) // 设置内容的点击意图
                    .setAutoCancel(true) // 点击通知栏后是否自动清除该通知
                    .setSmallIcon(R.mipmap.ic_logo) // 设置应用名称左边的小图标
                    .setContentTitle(nickName) // 设置通知栏里面的标题文本
                    .setContentText(
                        EaseCommonUtils.getMessageDigest(
                            message, topActivity
                        )
                    ) // 设置通知栏里面的内容文本
                    .setPriority(NotificationCompat.PRIORITY_MAX)
            mNotificationManager!!.notify(notifyId++, notification.build())
        }
    }

    private fun addImMsgTask(
        message: EMMessage,
        value: Map<String?, EMUserInfo?>
    ) {
        addImMsgTime = System.currentTimeMillis()
        val topActivity = ActivityUtils.getTopActivity()
        val appMsgNotifyInfo = AppMsgNotifyInfo()
        appMsgNotifyInfo.msgType = AppMsgNotifyInfo.MSG_TYPE_RECEIVE_IM
        appMsgNotifyInfo.content = EaseCommonUtils.getMessageDigest(
            message, topActivity
        )
        appMsgNotifyInfo.userId = message.conversationId()


        val emUserInfo = value[message.from]
        val nickName: String = if (emUserInfo != null && !TextUtils.isEmpty(emUserInfo.nickname)) {
            emUserInfo.nickname
        } else {
            "收到一条新消息"
        }
        appMsgNotifyInfo.nickName = nickName
        appMsgNotifyInfo.portrait = emUserInfo?.avatarUrl.toString()
        val stringAttribute = message.getStringAttribute(ChatConstant.CUSTOM_SEND_USER_INFO, "")
        logcom("sendUserInfo=$stringAttribute")
        if (!TextUtils.isEmpty(stringAttribute)) {
            val sendUserInfo: UserDetailInfo? =
                GsonUtils.fromJson(stringAttribute, UserDetailInfo::class.java)
            sendUserInfo?.apply {
                if (!TextUtils.isEmpty(nickName)) {
                    appMsgNotifyInfo.nickName = this.nickName
                }
                if (!TextUtils.isEmpty(portrait)) {
                    appMsgNotifyInfo.portrait = this.portrait
                }
            }
        }
        appMsgNotifyInfo.showTimeMills = System.currentTimeMillis()
        imMsgNotifyTaskManager.addTask(ImChatMsgNotifyTask(appMsgNotifyInfo))
    }

    private fun dealCommonCmdMsg(message: EMMessage) {
        val source = message.getIntAttribute("source", -1)
        logcom("收到透传消息source=$source")
        if (source == CmdMsgTypeConfig.ADD_FRIEND) {
            val userInfo = ChatUserInfoManager.getUserInfo(message.conversationId())
            userInfo?.apply {
                this.isFriend = true
                ChatUserInfoManager.saveUserInfo(this)
            }

        } else if (source == ChatConstant.ACTION_PHONE_CALL_VIDEO) {
            //收到一对一视频
            val callInfo = message.getStringAttribute("callInfo")
            var appStatus = ""
            if (VersionUtils.isTargetQ(ActivityUtils.getTopActivity())) {
                if (CommonUtils.isScreenOff()) {
                    appStatus = "熄屏"
                    showReceiveNewCallInBg(callInfo)
                } else {
                    if (AppUtils.isAppForeground()) {
                        appStatus = "app前台显示"
                        showNewCallWhenForeground(callInfo)
                    } else {
                        appStatus = "app后台显示"
                        showReceiveNewCallInBg(callInfo)
                    }
                }
            } else {
                if (CommonUtils.isScreenOff()) {
                    appStatus = "熄屏"
                    RouteIntent.toToWaitPhoneActivity(callInfo)
                } else {
                    if (AppUtils.isAppForeground()) {
                        appStatus = "app前台显示"
                        showNewCallWhenForeground(callInfo)
                    } else {
                        appStatus = "app后台显示"
                        RouteIntent.toToWaitPhoneActivity(callInfo)
                    }
                }
            }
            logcom(appStatus)

        } else if (source == ChatConstant.ACTION_MSG_APPLY_SET_UP_SUCCESS) {
            if (!AgoraManager.isLiveRoom) {
                //申请上麦成功
                val roomId = message.getStringAttribute("roomId")
                val seatId = message.getStringAttribute("seatId")
                val ownerNickname = message.getStringAttribute("ownerNickname")
                runOnUiThread { showApplySuccessDialog(roomId, seatId, ownerNickname) }
            }
        } else if (source == ChatConstant.ACTION_PHONE_CALL_REFUSE) {
            val stringAttribute =
                message.getStringAttribute(ChatConstant.CUSTOM_DATA, "")
            if (!TextUtils.isEmpty(stringAttribute)) {
                if (CommonUtils.isPopShow(receiveImCallPop)) {
                    receiveImCallPop?.dismiss()
                    return
                }
                if (CommonUtils.isPopShow(receiveImCallBgPop)) {
                    receiveImCallBgPop?.dismiss()
                    return
                }
                val topActivity = ActivityUtils.getTopActivity()
                if ((topActivity is VideoPhoneActivity && stringAttribute.equals(
                        topActivity.chatUserId.toString()
                    ))
                ) {
                    topActivity.finish()
                }
            }
        }else if (source == ChatConstant.GLOBAL_GIFT_ALERT){
            if (CommonUtils.isScreenOff() || !AppUtils.isAppForeground()) {
                return
            }
            val data = message.getJSONObjectAttribute("data")
            addPopTask(ChatConstant.GLOBAL_GIFT_ALERT,
                data.toString())
        }
    }

    private fun showNewCallWhenForeground(callInfo: String) {
        val calling = ApplicationProxy.instance.isCalling()
        if (calling) {
            showWaitPhonePop(callInfo)
        } else {
            RouteIntent.toToWaitPhoneActivity(callInfo)
        }
    }

    private var receiveImCallBgPop: ReceiveImCallInBgPop? = null

    /**
     *
     * app后台显示时 收到来电
     * 实现方案1 采用悬浮窗
     */
    private fun showReceiveNewCallInBg(callInfo: String) {
        if (CommonUtils.isPopShow(receiveImCallBgPop)) {
            return
        }
        receiveImCallBgPop =
            ReceiveImCallInBgPop.showDialog(ActivityUtils.getTopActivity(), callInfo)
    }

    private var receiveImCallPop: ReceiveImCallPop? = null
    private fun showWaitPhonePop(callInfo: String) {
        if (CommonUtils.isPopShow(receiveImCallPop)) {
            return
        }
        val topActivity = ActivityUtils.getTopActivity()
        if (topActivity is FragmentActivity) {
            receiveImCallPop = ReceiveImCallPop.showDialog(topActivity, callInfo)
        }
    }

    private fun showApplySuccessDialog(roomId: String, seatId: String, ownerNickname: String) {

        DialogUtils.showConfirmDialog("上麦申请成功", {
            userSetSeat(roomId, seatId)
        }, {
        }, "$ownerNickname 同意了你的上麦请求", confirm = "进入房间")

    }

    private fun userSetSeat(roomId: String, seatId: String) {
        request({ agoraRxApi.userSetSeat(roomId, "2", seatId, AppCacheManager.userId) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    LiveRoomManager.toLiveRoomPage(
                        ActivityUtils.getTopActivity() as FragmentActivity,
                        roomId
                    )
                }
            })
    }


    private fun initToastStyle() {
        ToastUtils.getDefaultMaker().setGravity(Gravity.CENTER, 0, 0)
        ToastUtils.getDefaultMaker().setBgColor(Color.parseColor("#29282D"))
        ToastUtils.getDefaultMaker().setTextColor(Color.parseColor("#ffffff"))
        com.hjq.toast.ToastUtils.init(this, BlackToastStyle())
    }

    /**
     * 初始化网络请求
     */
    private fun initRetrofit() {
        //retrofit初始化
        val instance = RetrofitUtil.getInstance()
        val interceptorList: List<Interceptor> =
            listOf(HttpCommonInterceptor(), HttpHeadInterceptor())
        instance.timeout(30L).debugMode(BuildConfig.DEBUG).proxy(BuildConfig.DEBUG)
            .interceptors(interceptorList).interceptor(HttpCacheInterceptor())
            .converterFactory(CustomizeGsonConverterFactory.create())
        instance.init(BuildConfig.BASE_SERVER_ADDRESS, {
            if (BuildConfig.DEBUG) {
                it.sslSocketFactory(
                    CertificateManageHelper.getSSLSocketFactory(),
                    CertificateManageHelper.getTrustManager()
                )
                it.hostnameVerifier(CertificateManageHelper.getHostnameVerifier())
            }
        })
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_EXPORTED)
        } else {
            super.registerReceiver(receiver, filter)
        }
    }

    override fun registerReceiver(
        receiver: BroadcastReceiver?,
        filter: IntentFilter?,
        flags: Int
    ): Intent? {
        var intent: Intent? = null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val flagExported = flags and RECEIVER_EXPORTED != 0
                val flagNotExported = flags and RECEIVER_NOT_EXPORTED != 0
                if (!flagExported && !flagNotExported) {
                    intent = super.registerReceiver(receiver, filter, flags or RECEIVER_EXPORTED)
                    return intent
                }
                intent = super.registerReceiver(receiver, filter, flags)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            intent
        } else {
            super.registerReceiver(receiver, filter, flags)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        ARouterWrapper.destory()
    }

    companion object {
        private val appPopTaskQueueManagerImpl = TaskQueueManagerImpl()

        init {
            RefreshManager.init(SmartRefreshProcessor())
        }

        fun addPopTask(type: Int, content: String) {
            appPopTaskQueueManagerImpl.addTask(AppPopTask(type, content))
        }
    }
}