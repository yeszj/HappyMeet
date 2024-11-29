package cn.huanyuan.sweetlove.func.manager

import android.annotation.SuppressLint
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.sweetlove.net.rxApi
import cn.huanyuan.sweetlove.ui.invite.BindInviteCodeActivity
import cn.huanyuan.sweetlove.ui.login.LoginActivity
import cn.huanyuan.sweetlove.ui.login.profile.CompleteProfileActivity
import cn.huanyuan.sweetlove.ui.main.MainActivity
import cn.jiguang.verifysdk.api.JVerificationInterface
import cn.yanhu.commonres.bean.LoginSuccessInfo
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.imchat.custom.message.push.huawei.HMSPushHelper
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ActivityUtils
import com.hyphenate.EMCallBack
import com.hyphenate.chat.EMClient

/**
 * @author: zhengjun
 * created: 2024/2/29
 * desc:
 */
object LoginResultManager {
    fun loginSuccess(mContext: FragmentActivity, loginSuccessInfo: LoginSuccessInfo) {
        if (!TextUtils.isEmpty(loginSuccessInfo.phoneEndNum)) {
            AppCacheManager.phoneEndNum = loginSuccessInfo.phoneEndNum
        }
        AppCacheManager.userId = loginSuccessInfo.userId
        AppCacheManager.mToken = loginSuccessInfo.token
        AppCacheManager.imToken = loginSuccessInfo.imToken
        AppCacheManager.province = loginSuccessInfo.province
        loginIM(object : EMCallBack {
            override fun onSuccess() {
                if (loginSuccessInfo.isRegister) {
                    BindInviteCodeActivity.lunch(mContext, loginSuccessInfo)
                } else {
                    if (loginSuccessInfo.baseInfoFinish) {
                        ActivityUtils.finishActivity(LoginActivity::class.java)
                        MainActivity.lunch(mContext)
                    } else {
                        CompleteProfileActivity.lunch(mContext)
                    }
                }
                HMSPushHelper.getInstance().getHMSToken(mContext)
                HMSPushHelper.getInstance().getHonorToken(mContext)
                mContext.finish()
                JVerificationInterface.clearPreLoginCache() //清除预取号缓存
                JVerificationInterface.dismissLoginAuthActivity() //关闭授权页
            }

            override fun onError(p0: Int, p1: String?) {

            }

        })
    }

    private var isReset: Boolean = false
    fun loginIM(callBack: EMCallBack?) {
        if (TextUtils.isEmpty(AppCacheManager.imToken)) {
            getImToken(callBack)
        } else {
            EMClient.getInstance()
                .loginWithToken(
                    AppCacheManager.userId,
                    AppCacheManager.imToken,
                    object : EMCallBack {
                        override fun onSuccess() {
                            EMClient.getInstance().chatManager().loadAllConversations()
                            EMClient.getInstance().groupManager().loadAllGroups()
                            callBack?.onSuccess()
                            isReset = false
                        }

                        override fun onError(code: Int, p1: String?) {
                            if (code == 200) {
                                callBack?.onSuccess()
                                isReset = false
                            }
                            if (!isReset && code != 200) {
                                isReset = true
                                imLogout(callBack)
                            } else {
                                callBack?.onError(code, p1)
                            }
                        }

                    })
        }

    }

    @SuppressLint("CheckResult")
    private fun getImToken(callBack: EMCallBack?) {
        request({ rxApi.getImToken() }, object : OnRequestResultListener<String> {
            override fun onSuccess(data: BaseBean<String>) {
                AppCacheManager.imToken = data.data.toString()
                loginIM(callBack)
            }

            override fun onFail(code: Int?, msg: String?) {
                super.onFail(code, msg)
                ApplicationProxy.instance.loginInvalid()
            }
        })
    }

    //环信登出
    private fun imLogout(callBack: EMCallBack?) {
        EMClient.getInstance().logout(true, object : EMCallBack {
            override fun onSuccess() {
                loginIM(callBack)
            }

            override fun onError(code: Int, message: String) {
            }
        })
    }
}