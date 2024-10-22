package cn.zj.netrequest.application

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.fragment.app.FragmentActivity

interface IApplication {
    fun loginInvalid()
    fun getApplication(): Application
    fun isLogin(): Boolean
    fun getHead(): MutableMap<String, String?>
    fun getServeAddress():String

    fun askCustomer()

    /**
     * 显示充值弹框
     */
    fun showRechargePop(mContext: FragmentActivity, isDismissWhenPaySuccess: Boolean)
    fun showRechargePop(mContext: FragmentActivity,hasShadow:Boolean, isDismissWhenPaySuccess: Boolean)
    fun getLiveRoomActivity():Activity?

    fun isMiniLiveRoomShow():Boolean
    fun isCalling():Boolean
    fun hasLoadAgoraSdk():Boolean

    fun jumpToPage(className:String,intent: Intent)
}