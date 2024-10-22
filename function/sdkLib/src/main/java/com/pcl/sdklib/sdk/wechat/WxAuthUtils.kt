package com.pcl.sdklib.sdk.wechat

import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ThreadUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.pcl.sdklib.api.sdkRxApi
import com.pcl.sdklib.listener.OnAuthResultListener
import com.pcl.sdklib.manager.SdkParamsManager
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 * @author: zhengjun
 * created: 2024/3/8
 * desc:
 */
object WxAuthUtils {
    const val TYPE_LOGIN = 1//登录
    const val TYPE_CASH_AUTH = 2//提现授权
    fun weChatAuth(context: FragmentActivity) {
        ThreadUtils.runOnUiThread {
            val api = WXAPIFactory.createWXAPI(context, SdkParamsManager.WX_APP_ID)
            if (api.isWXAppInstalled) {
                val req = SendAuth.Req()
                req.scope = "snsapi_userinfo" // 只能填 snsapi_userinfo
                api.sendReq(req)
            } else {
                showToast("微信未安装，请先安装微信")
            }
        }
    }

    fun registerAuthResultListener(
        context: FragmentActivity,
        onAuthResultListener: OnAuthResultListener
    ) {
        LiveEventBus.get<String>(LiveDataEventManager.WX_AUTH_SUCCESS).observe(context) {
            request(
                { sdkRxApi.wxAuth(it) },
                object : OnRequestResultListener<Boolean> {
                    override fun onSuccess(data: BaseBean<Boolean>) {
                        onAuthResultListener.onAuthSuccess()
                    }
                }, activity = context
            )
        }
    }
}