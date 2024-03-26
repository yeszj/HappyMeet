package cn.huanyuan.happymeet.func

import android.app.Application
import android.text.TextUtils
import cn.huanyuan.happymeet.BuildConfig
import cn.huanyuan.happymeet.net.HttpHeadConfig
import cn.yanhu.baselib.cache.UserPref
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.application.IApplication
import com.blankj.utilcode.util.Utils
import com.pcl.sdklib.sdk.union.UnionServiceUtils

/**
 * @author: zhengjun
 * created: 2024/1/30
 * desc:
 */
class ApplicationRouterImpl:IApplication {

    override fun loginInvalid() {
        UserPref.clear()
        RouteIntent.lunchLoginPage()
    }

    override fun getApplication(): Application {
        return Utils.getApp()
    }

    override fun isLogin(): Boolean {
        return !TextUtils.isEmpty(AppCacheManager.mToken)
    }

    override fun getHead(): MutableMap<String, String?> {
        return HttpHeadConfig.getHeader()
    }

    override fun getServeAddress(): String {
        return BuildConfig.BASE_SERVER_ADDRES
    }

    override fun askCustomer() {
        UnionServiceUtils.askCustomer()
    }


    companion object {
        fun getInstance() = InstanceHelper.sSingle
    }

    object InstanceHelper {
        val sSingle = ApplicationRouterImpl()
    }
}