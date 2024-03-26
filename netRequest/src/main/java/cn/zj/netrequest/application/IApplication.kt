package cn.zj.netrequest.application

import android.app.Application

interface IApplication {
    fun loginInvalid()
    fun getApplication(): Application
    fun isLogin(): Boolean
    fun getHead(): MutableMap<String, String?>
    fun getServeAddress():String

    fun askCustomer()
}