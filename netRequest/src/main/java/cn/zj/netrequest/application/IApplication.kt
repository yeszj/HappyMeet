package cn.zj.netrequest.application

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import cn.zj.netrequest.OnRoomLeaveListener
import java.math.BigDecimal

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
    fun showRechargePop(mContext: FragmentActivity, isDismissWhenPaySuccess: Boolean,balanceRose: BigDecimal?=null)
    fun showRechargePop(mContext: FragmentActivity,hasShadow:Boolean, isDismissWhenPaySuccess: Boolean)
    fun getLiveRoomActivity():Activity?
    fun isCalling():Boolean

    fun isMiniLiveRoomShow():Boolean
    fun isShowFloatCalling():Boolean
    fun hasLoadAgoraSdk():Boolean
    fun hasLoadBeautySdk():Boolean

    fun jumpToPage(className:String,intent: Intent)
    fun finishLiveRoomActivity(onRoomLeaveListener: OnRoomLeaveListener)

    fun reLoginImSdk(onImLoginListener: OnImLoginListener)
}