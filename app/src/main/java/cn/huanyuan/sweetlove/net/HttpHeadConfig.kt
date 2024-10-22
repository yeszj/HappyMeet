package cn.huanyuan.sweetlove.net

import android.text.TextUtils
import cn.yanhu.baselib.utils.SystemUtils
import cn.huanyuan.sweetlove.BuildConfig
import cn.zj.netrequest.application.ApplicationProxy
import cn.yanhu.baselib.utils.IpAddressUtils
import cn.yanhu.commonres.manager.AppCacheManager
import com.github.gzuliyujiang.oaid.DeviceID
import com.github.gzuliyujiang.oaid.DeviceIdentifier


/**
 * @author: witness
 * created: 2022/4/12
 * desc:
 */
object HttpHeadConfig {
    fun getHeader(): MutableMap<String, String?> {
        val map = mutableMapOf<String, String?>()
        if (!AppCacheManager.isFirstOpenApp){
            map["token"] = AppCacheManager.mToken
            val systemVersion = SystemUtils.getSystemVersion()
            if (!TextUtils.isEmpty(systemVersion)) {
                map["systemversion"] = systemVersion!!
            }
            map["ip"] = IpAddressUtils.ip
            map["user_id"] = AppCacheManager.userId
            val systemModel = SystemUtils.getDeviceName(ApplicationProxy.instance.getApplication())
            if (!TextUtils.isEmpty(systemModel)) {
                map["phonemodel"] = SystemUtils.encodeHeadInfo(systemModel!!)
            }

            var clientId = AppCacheManager.oaid
            if (TextUtils.isEmpty(clientId)){
                try {
                    clientId = DeviceIdentifier.getClientId()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (TextUtils.isEmpty(clientId)){
                    clientId = DeviceID.getPseudoID()
                }
            }

            map["oaid"] = clientId
        }
        map["os"] = "1"
        map["channel"] = BuildConfig.FLAVOR
        map["packagename"] = BuildConfig.APPLICATION_ID
        map["appversion"] = BuildConfig.VERSION_NAME

        return map
    }
}