package cn.yanhu.agora.config

import android.app.Application
import cn.yanhu.agora.config.rtc.RtcEventHandler
import cn.yanhu.agora.config.rtc.RtcExtensionHandler
import cn.zj.netrequest.application.ApplicationProxy
import io.agora.rtc2.RtcEngine

/**
 * @author: witness
 * created: 2022/11/21
 * desc:
 */
class AgoraManager {
    private var mAgoraEngine: AgoraEngine? = null

    object InstanceHelper {
        val sSingle = AgoraManager()
    }

    private fun initAgoraEngine(application: Application) {
        mAgoraEngine = AgoraEngine(application)
    }

    fun rtcEngine(): RtcEngine {
        if (mAgoraEngine == null) {
            initAgoraEngine(ApplicationProxy.instance.getApplication())
        }
        return mAgoraEngine!!.rtcEngine()
    }

    fun registerRtcHandler(handler: RtcEventHandler?) {
        if (handler == null) {
            return
        }
        if (mAgoraEngine == null) {
            initAgoraEngine(ApplicationProxy.instance.getApplication())
        }
        mAgoraEngine!!.registerRtcHandler(handler)
    }

    fun registerRtcExtensionHandler(handler: RtcExtensionHandler?) {
        if (handler == null) {
            return
        }
        if (mAgoraEngine == null) {
            initAgoraEngine(ApplicationProxy.instance.getApplication())
        }
        mAgoraEngine!!.registerRtcExtensionHandler(handler)
    }

    private fun removeRtcExtensionHandler() {
        mAgoraEngine?.removeRtcExtensionHandler()
    }


    fun clearRtcHandler() {
        mAgoraEngine?.clearRtcHandler()
        removeRtcExtensionHandler()
        release()
    }


    private fun release() {
        mAgoraEngine?.release()
        mAgoraEngine = null
    }

    companion object {
        fun getInstance() = InstanceHelper.sSingle
    }
}