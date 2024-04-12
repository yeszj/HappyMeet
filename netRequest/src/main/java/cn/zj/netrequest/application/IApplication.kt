package cn.zj.netrequest.application

import android.app.Application
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
    fun showRechargePop(mContext: FragmentActivity,  isDismissWhenPaySuccess: Boolean)
}