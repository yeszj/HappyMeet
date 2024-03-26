package cn.huanyuan.happymeet.func.manager

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.happymeet.ui.invite.BindInviteCodeActivity
import cn.huanyuan.happymeet.ui.login.LoginActivity
import cn.huanyuan.happymeet.ui.main.MainActivity
import cn.yanhu.commonres.bean.LoginSuccessInfo
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.imchat.config.ImKeyUtils
import com.blankj.utilcode.util.ActivityUtils
import com.netease.nimlib.sdk.auth.LoginInfo
import com.netease.yunxin.kit.corekit.im.IMKitClient
import com.netease.yunxin.kit.corekit.im.login.LoginCallback

/**
 * @author: zhengjun
 * created: 2024/2/29
 * desc:
 */
object LoginResultManager {
    fun loginSuccess(mContext: FragmentActivity, loginSuccessInfo:LoginSuccessInfo){
        AppCacheManager.userId = loginSuccessInfo.userId
        AppCacheManager.mToken = loginSuccessInfo.token
        AppCacheManager.imToken = loginSuccessInfo.imToken
        loginIM(mContext)
        if (loginSuccessInfo.isRegister){
            BindInviteCodeActivity.lunch(mContext,loginSuccessInfo)
        }else{
            ActivityUtils.finishActivity(LoginActivity::class.java)
            MainActivity.lunch(mContext)
        }
        mContext.finish()
    }

     fun loginIM(mContext: Activity) {
        val loginInfo = LoginInfo.LoginInfoBuilder.loginInfoDefault(AppCacheManager.userId, AppCacheManager.imToken)
            .withAppKey(ImKeyUtils.readAppKey(mContext)).build()
        IMKitClient.loginIM(loginInfo, object : LoginCallback<LoginInfo> {
            override fun onError(errorCode: Int, errorMsg: String) {

            }
            override fun onSuccess(data: LoginInfo?) {
            }
        })
    }
}