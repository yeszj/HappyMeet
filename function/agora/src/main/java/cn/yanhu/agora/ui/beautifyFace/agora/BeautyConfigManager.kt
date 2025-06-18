package cn.yanhu.agora.ui.beautifyFace.agora

import android.annotation.SuppressLint
import cn.yanhu.commonres.api.commonRxApi
import cn.yanhu.commonres.manager.AppCacheManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean

/**
 * @author: zhengjun
 * created: 2025/6/18
 * desc:
 */
object BeautyConfigManager {
    interface  OnLoadDefaultBeautyListener{
        fun onLoadResult(isSuccess: Boolean)
    }

    @JvmStatic
    @SuppressLint("CheckResult")
    fun getNetBeautyConfig(onLayoutChangeListener: OnLoadDefaultBeautyListener?=null) {
        var configKey = "female_beauty_config"
        if (AppCacheManager.isMan()) {
            configKey = "male_beauty_config"
        }
        request({ commonRxApi.getConfigInfo(configKey) },object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {
                AppCacheManager.beautyDefaultConfig = data.data?:return
                onLayoutChangeListener?.onLoadResult(true)
            }
            override fun onFail(code: Int?, msg: String?) {
                onLayoutChangeListener?.onLoadResult(false)
            }
        })
    }
}