package cn.yanhu.agora.config

import android.app.Application
import cn.yanhu.agora.config.rtc.AgoraExtensionHandler
import cn.yanhu.agora.config.rtc.AgoraRtcHandler
import cn.yanhu.agora.config.rtc.RtcEventHandler
import cn.yanhu.agora.config.rtc.RtcExtensionHandler
import cn.yanhu.agora.manager.BeautySetManager
import io.agora.rtc2.Constants
import io.agora.rtc2.Constants.AUDIO_PROFILE_DEFAULT
import io.agora.rtc2.Constants.AUDIO_SCENARIO_GAME_STREAMING
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig

/**
 * @author: witness
 * created: 2022/11/21
 * desc:
 */
class AgoraEngine(application: Application) {
    private var mRtcEngine: RtcEngine
    private var mRtcEventHandler = AgoraRtcHandler()
    private var mExtensionObserver = AgoraExtensionHandler()
    init {
        val appId = "301729ee939d4470b6b60a795e9ccc22"
        val config = RtcEngineConfig()
        config.mContext = application
        config.addExtension("AgoraFaceUnityExtension")
        config.mAppId = appId
        config.mEventHandler = mRtcEventHandler
        config.mExtensionObserver = mExtensionObserver
        //config.mNativeLibPath = AgoraManager.getInstance().getSoPath()
        mRtcEngine = RtcEngine.create(config)
        // 启用插件
        mRtcEngine.enableExtension("FaceUnity", "Effect", true)
        mRtcEngine.setDefaultAudioRoutetoSpeakerphone(true)
        mRtcEngine.enableAudio()
        BeautySetManager.getInstance().initExtension(mRtcEngine)
        mRtcEngine.enableAudioVolumeIndication(400,3,true)
        mRtcEngine.muteAllRemoteAudioStreams(true)
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        mRtcEngine.setAudioProfile(AUDIO_PROFILE_DEFAULT,AUDIO_SCENARIO_GAME_STREAMING )
    }

    fun rtcEngine(): RtcEngine {
        return mRtcEngine
    }

    fun registerRtcExtensionHandler(handler: RtcExtensionHandler) {
        mExtensionObserver.registerEventHandler(handler)
    }
    fun removeRtcExtensionHandler() {
        mExtensionObserver.removeEventHandler()
    }

    fun registerRtcHandler(handler: RtcEventHandler) {
        mRtcEventHandler.registerEventHandler(handler)
    }

    fun removeRtcHandler(handler: RtcEventHandler) {
        mRtcEventHandler.removeEventHandler(handler)
    }

    fun clearRtcHandler() {
        mRtcEventHandler.clearEventHandler()
    }

    fun release() {
        mRtcEngine.stopPreview()
        mRtcEngine.disableVideo()
        RtcEngine.destroy()
    }
}