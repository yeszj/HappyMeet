package cn.huanyuan.sweetlove.func

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.sweetlove.BaseApplication
import cn.huanyuan.sweetlove.BuildConfig
import cn.huanyuan.sweetlove.func.dialog.RoseRechargePop
import cn.huanyuan.sweetlove.net.HttpHeadConfig
import cn.huanyuan.sweetlove.ui.login.LoginActivity
import cn.huanyuan.sweetlove.ui.main.MainActivity
import cn.yanhu.agora.manager.dbCache.AgoraSdkCacheManager
import cn.yanhu.agora.miniwindow.LiveRoomVideoMiniManager
import cn.yanhu.agora.miniwindow.EaseCallFloatWindow
import cn.yanhu.agora.miniwindow.MiniWindowManager
import cn.yanhu.agora.ui.imphone.FromWaitPhoneActivity
import cn.yanhu.agora.ui.imphone.ToWaitPhoneActivity
import cn.yanhu.agora.ui.imphone.VideoPhoneActivity
import cn.yanhu.agora.ui.liveRoom.live.LiveRoomActivity
import cn.yanhu.baselib.cache.UserPref
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.OnRoomLeaveListener
import cn.zj.netrequest.application.IApplication
import cn.zj.netrequest.application.OnImLoginListener
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.Utils
import com.hyphenate.EMCallBack
import com.hyphenate.chat.EMClient
import com.pcl.sdklib.sdk.jverrify.JiGuangSDKUtils
import com.pcl.sdklib.sdk.location.LocationCacheManager
import com.pcl.sdklib.sdk.wechat.WxCustomerServiceUtils

/**
 * @author: zhengjun
 * created: 2024/1/30
 * desc:
 */
class ApplicationRouterImpl : IApplication {

    override fun loginInvalid() {
        val topActivity = ActivityUtils.getTopActivity()
        if (topActivity!=null && topActivity is LoginActivity){
            return
        }
        JiGuangSDKUtils.getInstance().initJVerificationSDk()
        EMClient.getInstance().logout(true, object : EMCallBack {
            override fun onSuccess() {
                logoutSuccess()
            }

            override fun onError(code: Int, error: String?) {
                logoutSuccess()
            }
        })
    }

    private fun logoutSuccess() {
        UserPref.clear()
        LocationCacheManager.mapLocation = null
        RouteIntent.lunchLoginPage()
        for (activity in ActivityUtils.getActivityList()) {
            if (activity !is LoginActivity) {
                activity.finish()
            }
        }
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
        return BuildConfig.BASE_SERVER_ADDRESS
    }

    override fun askCustomer() {
        WxCustomerServiceUtils.askCustomer()
    }

    override fun showRechargePop(mContext: FragmentActivity, isDismissWhenPaySuccess: Boolean) {
        RoseRechargePop.showDialog(mContext, isDismissWhenPaySuccess)
    }

    override fun showRechargePop(
        mContext: FragmentActivity,
        hasShadowBg: Boolean,
        isDismissWhenPaySuccess: Boolean
    ) {
        RoseRechargePop.showDialog(mContext, hasShadowBg, isDismissWhenPaySuccess)
    }

    override fun getLiveRoomActivity(): Activity? {
        val activityList = ActivityUtils.getActivityList()
        activityList.forEach {
            if (it is LiveRoomActivity) {
                return it
            }
        }
        return null
    }


    override fun isCalling(): Boolean {
        if (isMiniLiveRoomShow()) {
            return true
        }
        if (isShowFloatCalling()) {
            return true
        }
        val it = ActivityUtils.getTopActivity()
        if (it is VideoPhoneActivity || it is FromWaitPhoneActivity || it is ToWaitPhoneActivity || it is LiveRoomActivity) {
            return true
        }
        return false
    }

    override fun isMiniLiveRoomShow(): Boolean {
        return LiveRoomVideoMiniManager.getInstance().isShowing
    }

    override fun isShowFloatCalling(): Boolean {
        return EaseCallFloatWindow.getInstance().isShowing
    }

    override fun hasLoadAgoraSdk(): Boolean {
        return AgoraSdkCacheManager.hasLoadAgoraSdk()
    }

    override fun jumpToPage(className: String, intent: Intent) {
        val topActivity = ActivityUtils.getTopActivity()
        if (className.contains(".MainActivity") && topActivity !is MainActivity) {
            MiniWindowManager.switchPageToFront(topActivity, intent)
        } else {
            topActivity.startActivity(intent)
        }
    }

    override fun finishLiveRoomActivity(onRoomLeaveListener: OnRoomLeaveListener) {
        val liveRoomActivity = getLiveRoomActivity() as LiveRoomActivity?
        liveRoomActivity?.roomLeave(onRoomLeaveListener)
    }

    override fun reLoginImSdk(onImLoginListener: OnImLoginListener) {
        (getApplication() as BaseApplication).reInitImSdk(onImLoginListener)
    }


    companion object {
        fun getInstance() = InstanceHelper.sSingle
    }

    object InstanceHelper {
        val sSingle = ApplicationRouterImpl()
    }
}