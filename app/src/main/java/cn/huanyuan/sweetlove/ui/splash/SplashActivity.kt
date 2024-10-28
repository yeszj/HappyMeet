package cn.huanyuan.sweetlove.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.sweetlove.BaseApplication
import cn.huanyuan.sweetlove.bean.AppStartResponse
import cn.huanyuan.sweetlove.func.dialog.PrivacyPop
import cn.huanyuan.sweetlove.func.manager.LoginResultManager
import cn.huanyuan.sweetlove.net.rxApi
import cn.huanyuan.sweetlove.ui.login.profile.CompleteProfileActivity
import cn.huanyuan.sweetlove.ui.main.MainActivity
import cn.yanhu.baselib.utils.StatusBarUtil
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.imchat.config.ChatUiConfig
import cn.yanhu.imchat.config.ConversationKitConfig
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.hyphenate.EMCallBack
import com.pcl.sdklib.sdk.jverrify.JiGuangSDKUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import cn.huanyuan.sweetlove.R

/**
 * @author: witness
 * created: 2022/4/29
 * desc:
 */

@SuppressLint("CustomSplashScreen")
class SplashActivity : FragmentActivity(
) {
    private var installSplashScreen: SplashScreen? = null
    private val keepOnScreenCondition = AtomicBoolean(true)

    /*最终的方法是否调用*/
    private val dialogShowInvoke = AtomicBoolean(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= 31) {
            installSplashScreen = installSplashScreen()
            installSplashScreen?.apply {
                setKeepOnScreenCondition {
                    keepOnScreenCondition.get()
                }
                setOnExitAnimationListener {
                    showPrivacyPop()
                }
            }
        }

        //隐藏标题栏
        actionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
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
                if (!dialogShowInvoke.get()) {
                    //这里去掉调用一下最终要执行的方法，兜个底  解决部分厂商手机setOnExitAnimationListener不执行
                    showPrivacyPop()
                }
            }
        } else {
            redirectTo()
        }
    }

    private fun showPrivacyPop() {
        if (dialogShowInvoke.get()) {
            return
        }
        dialogShowInvoke.compareAndSet(false, true)
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
        JiGuangSDKUtils.getInstance().setJiGuangSDKListener(object :
            JiGuangSDKUtils.JiGuangSdkCallback() {
            override fun fail(msg: String?) {
                toLogin()
            }
            override fun onPreLoginSuccess() {
                toLogin()
            }
        })
        JiGuangSDKUtils.getInstance().initJVerificationSDk()
    }


    private fun setFullScreenStatusBar() {
        StatusBarUtil.setStatusBarFullTransparent(this)
    }

    private fun redirectTo() {
        ChatUiConfig.initConfig()
        ConversationKitConfig.initConfig()
        if (ApplicationProxy.instance.isLogin()) {
            if (!TextUtils.isEmpty(AppCacheManager.userId)) {
                LoginResultManager.loginIM(object : EMCallBack {
                    override fun onSuccess() {
                        showMainPage()
                    }

                    override fun onError(p0: Int, p1: String?) {
                    }
                })
            }

        } else {
            initJverify()
        }
    }

    private fun showMainPage() {
        request({ rxApi.appStart() }, object : OnRequestResultListener<AppStartResponse> {
            override fun onSuccess(data: BaseBean<AppStartResponse>) {
                val appStartResponse = data.data
                if (appStartResponse == null || appStartResponse.baseInfoFinish) {
                    MainActivity.lunch(this@SplashActivity, intent.extras)
                } else {
                    CompleteProfileActivity.lunch(this@SplashActivity)
                }
                finish()
            }

        }, false, activity = this@SplashActivity)

    }


    private fun toLogin() {
        RouteIntent.lunchLoginPage()
        finish()
    }
}