package cn.yanhu.agora.config.rtc


/**
 * @author: zhengjun
 * created: 2023/5/31
 * desc:
 */
interface RtcExtensionHandler {
    fun onEvent(provider: String?, extension: String?, key: String?, value: String?)

    fun onStarted(provider: String?, extension: String?){}

    fun onStopped(provider: String?, extension: String?){}

    fun onError(provider: String?, extension: String?, error: Int, message: String?){}

}