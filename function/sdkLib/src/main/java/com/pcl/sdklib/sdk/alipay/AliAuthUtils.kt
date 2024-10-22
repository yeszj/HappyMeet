package com.pcl.sdklib.sdk.alipay

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.alipay.sdk.app.AuthTask
import com.pcl.sdklib.api.sdkRxApi
import com.pcl.sdklib.bean.AuthResult
import com.pcl.sdklib.listener.OnAuthResultListener
import java.lang.ref.WeakReference

/**
 * @author: zhengjun
 * created: 2024/10/10
 * desc:
 */
object AliAuthUtils {
    const val FLAG_ALI_AUTH = 3
    private var onAuthResultListener: OnAuthResultListener?=null
    @JvmStatic
    fun aliAuth(activity: FragmentActivity,onAuthResultListener: OnAuthResultListener) {
        this.onAuthResultListener = onAuthResultListener
        DialogUtils.showLoading()
        request({ sdkRxApi.alipayAuth() }, object : OnRequestResultListener<String> {
            override fun onSuccess(data: BaseBean<String>) {
                DialogUtils.dismissLoading()
                val authInfo: String = data.data.toString()
                Thread(
                    AuthRunnable(
                        activity,
                        AuthHandler(activity),
                        authInfo
                    )
                ).start()
            }
            override fun onFail(code: Int?, msg: String?) {
                super.onFail(code, msg)
                DialogUtils.dismissLoading()
            }
        })
    }

    class AuthRunnable(
        activity: FragmentActivity,
        private val payHandler: AuthHandler,
        private val authInfo: String,
    ) : Runnable {
        private var mActivityRef: WeakReference<FragmentActivity>? = null

        init {
            mActivityRef = WeakReference(activity)
        }

        override fun run() {
            mActivityRef?.apply {
                val activity = get()
                activity?.apply {
                    // 构造AuthTask 对象
                    val authTask = AuthTask(mActivityRef!!.get())
                    // 调用授权接口，获取授权结果
                    val result = authTask.authV2(authInfo, true)
                    val msg = Message()
                    msg.what = FLAG_ALI_AUTH
                    msg.obj = result
                    payHandler.sendMessage(msg)
                }

            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class AuthHandler(activity: FragmentActivity) : Handler(Looper.getMainLooper()) {
        private var mActivityRef: WeakReference<FragmentActivity>? = null

        init {
            mActivityRef = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            if (msg.what == FLAG_ALI_AUTH) {
                val authResult = AuthResult(msg.obj as Map<String?, String?>, true)
                val resultStatus: String? = authResult.resultStatus
                // 判断resultStatus 为“9000”且result_code
                // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
                if (TextUtils.equals(
                        resultStatus,
                        "9000"
                    ) && TextUtils.equals(authResult.resultCode, "200")
                ) {
                    // 获取alipay_open_id，调支付时作为参数extern_token 的value
                    // 传入，则支付账户为该授权账户
                    aliAuthSuccess(authResult.result)
                } else {
                    // 其他状态值则为授权失败
                    showToast("授权失败")
                }
            }
        }
    }

    private fun aliAuthSuccess(result: String?) {
        request({ sdkRxApi.aliAuthSuccess(result) }, object : OnRequestResultListener<String> {
            override fun onSuccess(data: BaseBean<String>) {
                onAuthResultListener?.onAuthSuccess()
                showToast("授权成功")
            }
        })
    }

}