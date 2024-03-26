package cn.huanyuan.happymeet.ui.splash

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cn.huanyuan.happymeet.BaseApplication
import cn.huanyuan.happymeet.func.dialog.PrivacyPop
import cn.huanyuan.happymeet.func.manager.LoginResultManager
import cn.huanyuan.happymeet.ui.main.MainActivity
import cn.jiguang.verifysdk.api.JVerificationInterface
import cn.yanhu.baselib.utils.StatusBarUtil
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.imchat.config.ChatUiConfig
import cn.yanhu.imchat.config.ConversationKitConfig
import cn.zj.netrequest.application.ApplicationProxy
import com.netease.yunxin.kit.corekit.im.IMKitClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean


/**
 * @author: witness
 * created: 2022/4/29
 * desc:
 */

@SuppressLint("CustomSplashScreen")
class SplashActivity : Activity(
) {
    private var installSplashScreen: SplashScreen? = null
    private val keepOnScreenCondition = AtomicBoolean(true)
    /*最终的方法是否调用*/
    private val dialogShowInvoke = AtomicBoolean(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen = installSplashScreen()
        installSplashScreen?.apply {
            setKeepOnScreenCondition {
                keepOnScreenCondition.get()
            }
            setOnExitAnimationListener {
                showPrivacyPop()
            }
        }

        //隐藏标题栏
        actionBar?.hide()
        super.onCreate(savedInstanceState)
        if ((intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0)) {
            finish()
            return
        }
        setFullScreenStatusBar()
        if (AppCacheManager.isFirstOpenApp) {
            MainScope().launch {
                Log.i("SplashActivity", "可以做一些初始化逻辑")
                /*解除blockui，此时正常情况下会调用setOnExitAnimationListener 已知Android12拉起App时不会调用*/
                keepOnScreenCondition.compareAndSet(true, false)
                delay(200)//这里最好延迟一下，保证setOnExitAnimationListener有机会得到执行
                if(!dialogShowInvoke.get()){
                    //这里去掉调用一下最终要执行的方法，兜个底  解决部分厂商手机setOnExitAnimationListener不执行
                    showPrivacyPop()
                }
            }
        } else {
            redirectTo()
        }
    }

    private fun showPrivacyPop() {
        if (dialogShowInvoke.get()){
            return
        }
        dialogShowInvoke.compareAndSet(false,true)
        PrivacyPop.showPop(this@SplashActivity, object : PrivacyPop.OnPrivacyClickListener {
            override fun onAgree() {
                (application as BaseApplication).initSdk()
                AppCacheManager.isFirstOpenApp = false
                keepOnScreenCondition.compareAndSet(false, true)
                initJverify()
            }

            override fun onDisAgree() {
                finish()
            }
        })
    }

    private fun initJverify() {
        JVerificationInterface.init(this, 5000
        ) { code, msg ->
            Log.d("tag", "code =" + code + "msg =" + msg)
            if (code == 8000) {
                preLogin()
            } else {
                toLogin()
            }
        }
    }

    private fun preLogin() {
        if (JVerificationInterface.checkVerifyEnable(this)) {
            JVerificationInterface.preLogin(this, 5000
            ) { _, _, _ -> //回调响应结果
                toLogin()
            }
        } else {
            toLogin()
        }
    }

    private fun setFullScreenStatusBar() {
        StatusBarUtil.setStatusBarFullTransparent(this)
    }

    private fun redirectTo() {
        ChatUiConfig.initConfig()
        ConversationKitConfig.initConfig()
        if (ApplicationProxy.instance.isLogin()) {
            if (TextUtils.isEmpty(IMKitClient.account())) {
                LoginResultManager.loginIM(this@SplashActivity)
            }
            showMainPage()
        } else {
            initJverify()
        }
    }

    private fun showMainPage() {
        MainActivity.lunch(this, intent.extras)
        finish()
    }


    private fun toLogin() {
        RouteIntent.lunchLoginPage()
        finish()
    }
}