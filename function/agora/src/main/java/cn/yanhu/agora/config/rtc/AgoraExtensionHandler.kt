package cn.yanhu.agora.config.rtc

import io.agora.rtc2.IMediaExtensionObserver

/**
 * @author: zhengjun
 * created: 2023/5/31
 * desc:
 */
class AgoraExtensionHandler: IMediaExtensionObserver {
    private var mHandler: RtcExtensionHandler?=null

    companion object {
        const val TAG = "rtcHandler"
    }

    fun registerEventHandler(handler: RtcExtensionHandler) {
        mHandler = handler
    }
    fun removeEventHandler() {
        mHandler = null
    }

    override fun onEvent(provider: String?, extension: String?, key: String?, value: String?) {
        mHandler?.onEvent(provider,extension,key,value)
    }

    override fun onStarted(provider: String?, extension: String?) {
        mHandler?.onStarted(provider,extension)
    }

    override fun onStopped(provider: String?, extension: String?) {
        mHandler?.onStopped(provider,extension)
    }
    override fun onError(provider: String?, extension: String?, error: Int, message: String?) {
        mHandler?.onError(provider,extension,error,message)
    }
}