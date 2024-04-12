package cn.huanyuan.happymeet

import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.net.http.HttpResponseCache
import android.os.Looper
import android.view.Gravity
import cn.huanyuan.happymeet.func.ApplicationRouterImpl
import cn.huanyuan.happymeet.net.HttpHeadInterceptor
import cn.huanyuan.happymeet.ui.main.MainActivity
import cn.yanhu.baselib.crash.CrashUtils
import cn.yanhu.baselib.crash.ExceptionHandler
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.refresh.SmartRefreshProcessor
import cn.yanhu.baselib.utils.ext.logComToFile
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.widget.router.ARouterWrapper
import cn.yanhu.commonres.db.UserInfoDbManager
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.imchat.config.ImKeyUtils
import cn.yanhu.imchat.config.NimSDKOptionConfig
import cn.yanhu.imchat.push.PushMessageHandler
import cn.yanhu.imchat.ui.chat.ImChatActivity
import cn.yanhu.imchat.ui.groupChat.ImTeamChatActivity
import cn.zj.netrequest.RetrofitUtil
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.factory.CustomizeGsonConverterFactory
import cn.zj.netrequest.https.CertificateManageHelper
import cn.zj.netrequest.intercept.HttpCacheInterceptor
import cn.zj.netrequest.intercept.HttpCommonInterceptor
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ProcessUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.github.gzuliyujiang.oaid.DeviceIdentifier
import com.hjq.toast.style.BlackToastStyle
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.SDKOptions
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.yunxin.kit.corekit.im.IMKitClient
import com.netease.yunxin.kit.corekit.im.repo.SettingRepo.isPushNotify
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_CHAT_P2P_PAGE
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_CHAT_TEAM_PAGE
import com.netease.yunxin.kit.corekit.route.XKitRouter
import com.opensource.svgaplayer.SVGAParser
import com.pcl.sdklib.sdk.union.UnicornGlideImageLoader
import com.pcl.sdklib.sdk.union.UnionRequestPermissionEvent
import com.qiyukf.nimlib.sdk.StatusBarNotificationConfig
import com.qiyukf.unicorn.api.Unicorn
import com.qiyukf.unicorn.api.YSFOptions
import com.qiyukf.unicorn.api.event.EventProcessFactory
import com.qiyukf.unicorn.api.event.SDKEvents
import okhttp3.Interceptor
import xyz.doikki.videoplayer.ijk.IjkPlayerFactory
import xyz.doikki.videoplayer.player.VideoViewConfig
import xyz.doikki.videoplayer.player.VideoViewManager
import java.io.File


class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (ProcessUtils.isMainProcess()) {
            Utils.init(this)
            ApplicationProxy.instance = ApplicationRouterImpl.getInstance()
            init()
            //checkException()
            registerAppStatusListener()
            registerActivityLifecycleCallBack()
            initImKit()
            if (!AppCacheManager.isFirstOpenApp) {
                initSdk()
            }
        }
    }

    private fun initImKit() {
        val options: SDKOptions =
            NimSDKOptionConfig.getSDKOptions<MainActivity>(this, ImKeyUtils.readAppKey(this))
        IMKitClient.config(this, null, options)
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
        Unicorn.config(this, "3f9a2096a3b5dc05c7525429aab70eda", options(), UnicornGlideImageLoader(this))
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
                .setPlayerFactory(create)
                .build()
        )
    }


    fun initSdk() {
        UserInfoDbManager.initDb(this)
        DeviceIdentifier.register(this)
        initImConfig()
        initQiyu()
        SVGAParser.shareParser().init(this)
    }


    private fun initQiyu() {
        Unicorn.initSdk()
    }

    private fun initImConfig() {
        IMKitClient.initSDK()
        registerImMsgEvent()
        XKitRouter.registerRouter(PATH_CHAT_TEAM_PAGE, ImTeamChatActivity::class.java)
        XKitRouter.registerRouter(PATH_CHAT_P2P_PAGE, ImChatActivity::class.java)
        IMKitClient.toggleNotification(isPushNotify())
        IMKitClient.registerMixPushMessageHandler(PushMessageHandler())
    }

    /**
     * 全局系统通知监听(透传消息)
     */
    private  fun registerImMsgEvent(){
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeCustomNotification({
                // 处理自定义系统通知。

            }, true)
    }


    private fun options(): YSFOptions {
        val options = YSFOptions()
        val statusBarNotificationConfig = StatusBarNotificationConfig()
        options.statusBarNotificationConfig = statusBarNotificationConfig
        options.sdkEvents = SDKEvents()
        options.sdkEvents.eventProcessFactory = EventProcessFactory { eventType: Int ->
            if (eventType == 5) {
                //sdk申请权限事件
                return@EventProcessFactory UnionRequestPermissionEvent()
            }
            null
        }
        return options
    }
    private fun initToastStyle() {
        ToastUtils.getDefaultMaker()
            .setGravity(Gravity.CENTER, 0, 0)
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
            .interceptors(interceptorList)
            .interceptor(HttpCacheInterceptor())
            .converterFactory(CustomizeGsonConverterFactory.create())
        instance.init(BuildConfig.BASE_SERVER_ADDRES, {
            if (BuildConfig.DEBUG) {
                it.sslSocketFactory(
                    CertificateManageHelper.getSSLSocketFactory(),
                    CertificateManageHelper.getTrustManager()
                )
                it.hostnameVerifier(CertificateManageHelper.getHostnameVerifier())
            }
        })
    }

    override fun onTerminate() {
        super.onTerminate()
        ARouterWrapper.destory()
    }

    companion object {
        init {
            RefreshManager.init(SmartRefreshProcessor())
        }
    }
}