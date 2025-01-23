package cn.yanhu.agora.manager

import android.content.Context
import cn.yanhu.agora.manager.AgoraSdkDownloadManager.getSoPath
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.manager.AppCacheManager
import io.agora.rtc2.IMediaExtensionObserver
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx

/**
 * @author: zhengjun
 * created: 2024/11/21
 * desc:
 */
object RtcEngineInit {
     var mRtcEngine: RtcEngineEx? = null
    fun initRtcEngine(context: Context):RtcEngineEx? {
        try {
            val config = RtcEngineConfig()
            config.mContext = context
            config.mAppId = AppCacheManager.agoraAppId
            config.mEventHandler = object : IRtcEngineEventHandler() {
            }
            config.mExtensionObserver = object : IMediaExtensionObserver{
                override fun onEvent(
                    provider: String?,
                    extension: String?,
                    key: String?,
                    value: String?
                ) {
                    logcom(
                        "agora-onEvent",
                        "$provider——————$extension————————$key————————$value————————"
                    )
                }

                override fun onStarted(provider: String?, extension: String?) {
                    logcom("agora-onStarted", "$provider——————$extension")
                }

                override fun onStopped(provider: String?, extension: String?) {
                    logcom("agora-onStopped", "$provider——————$extension")
                }

                override fun onError(
                    provider: String?,
                    extension: String?,
                    error: Int,
                    message: String?
                ) {
                    logcom(
                        "agora-onError",
                        "$provider——————$extension————————$error————————$message————————"
                    )
                }

            }
            // 添加美颜插件
            config.addExtension("AgoraFaceUnityExtension")
            config.mNativeLibPath = getSoPath()
            mRtcEngine = RtcEngineEx.create(config) as RtcEngineEx
            // 启用插件
            mRtcEngine!!.enableExtension("FaceUnity", "Effect", true)
            return mRtcEngine
        } catch (e: Exception) {
            // TraceUtils.getInstance().onEventObject("app_agora_fail", e.getMessage());
            showToast("直播间初始化异常,请重新进入直播间尝试")
            logcom("声网：" + e.message)
            return null
        }
    }

}