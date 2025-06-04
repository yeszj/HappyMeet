package cn.huanyuan.sweetlove.func.manager

import android.text.TextUtils
import cn.huanyuan.sweetlove.BuildConfig
import cn.zj.netrequest.application.ApplicationProxy
import com.meituan.android.walle.WalleChannelReader

/**
 * @author: zhengjun
 * created: 2025/5/30
 * desc:
 */
object ChannelUtils {
    fun getChannel():String{
        val channelInfo = WalleChannelReader.getChannelInfo(ApplicationProxy.instance.getApplication())
        var channel = channelInfo?.channel
        if (TextUtils.isEmpty(channel)){
            channel = BuildConfig.FLAVOR
        }
        return channel!!
    }
}