package cn.yanhu.agora.manager

import android.content.Context
import cn.yanhu.agora.manager.AgoraSdkDownloadManager.getSoPath
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.manager.AppCacheManager
import io.agora.rtc2.Constants
import io.agora.rtc2.ExtensionContext
import io.agora.rtc2.IMediaExtensionObserver
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtc2.video.VideoCanvas

/**
 * @author: zhengjun
 * created: 2024/11/21
 * desc:
 */
object RtcEngineInit {
    var mRtcEngine: RtcEngineEx? = null
    fun initRtcEngine(context: Context): RtcEngineEx? {
        try {
            val config = RtcEngineConfig()
            config.mContext = context
            config.mAppId = AppCacheManager.agoraAppId
            config.mEventHandler = object : IRtcEngineEventHandler() {
            }
            config.mExtensionObserver = object : IMediaExtensionObserver {
                override fun onEventWithContext(
                    extContext: ExtensionContext?,
                    key: String?,
                    value: String?
                ) {
                    logcom(
                        "onEventWithContext",
                        "${extContext?.providerName}——————${extContext?.extensionName}————————$key————————$value————————"
                    )
                }

                override fun onStartedWithContext(extContext: ExtensionContext?) {
                    logcom(
                        "onStartedWithContext",
                        "${extContext?.providerName}——————${extContext?.extensionName}"
                    )
                }

                override fun onStoppedWithContext(extContext: ExtensionContext?) {
                    logcom(
                        "onStartedWithContext",
                        "${extContext?.providerName}——————${extContext?.extensionName}"
                    )
                }

                override fun onErrorWithContext(
                    extContext: ExtensionContext?,
                    error: Int,
                    message: String?
                ) {
                    logcom(
                        "onStartedWithContext",
                        "${extContext?.providerName}——————${extContext?.extensionName}————————$error————————$message————————"
                    )
                }

            }
            // 添加美颜插件
            //  config.addExtension("AgoraFaceUnityExtension")
            config.mNativeLibPath = getSoPath()
            mRtcEngine = RtcEngineEx.create(config) as RtcEngineEx
            // 启用插件
            //  mRtcEngine!!.enableExtension("FaceUnity", "Effect", true)
            mRtcEngine!!.setAudioScenario(Constants.AudioScenario.getValue(Constants.AudioScenario.GAME_STREAMING))
            return mRtcEngine
        } catch (e: Exception) {
            // TraceUtils.getInstance().onEventObject("app_agora_fail", e.getMessage());
            showToast("直播间初始化异常,请重新进入直播间尝试")
            logcom("声网：" + e.message)
            return null
        }
    }

}