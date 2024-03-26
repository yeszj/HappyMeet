package com.pcl.sdklib.sdk.alipay

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.alipay.sdk.app.PayTask
import com.blankj.utilcode.util.ThreadUtils
import com.pcl.sdklib.api.sdkRxApi
import com.pcl.sdklib.bean.PayRequest
import com.pcl.sdklib.bean.PayResult
import com.pcl.sdklib.manager.PayManager
import java.lang.ref.WeakReference

/**
 * @author: zhengjun
 * created: 2023/11/13
 * desc:
 */
object AliPayUtils {
    const val FLAG_ALI_PAY = 3
    private const val ALIPAY_SUCCESS = "9000"
    private const val ALIPAY_CANCEL = "6001"
    private const val ALIPAY_NET_ERROR = "6002"

    @SuppressLint("CheckResult")
    @JvmStatic
    fun aliPay(rechargeId: Int, activity: FragmentActivity,rechargeType:Int = PayManager.RECHARGE_TYPE_ROSE) {
        DialogUtils.showLoading()
        val payRequest = PayRequest(rechargeId,rechargeType)

        request({ sdkRxApi.alipay(payRequest)},object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {
                DialogUtils.dismissLoading()
                val orderInfo: String = data.data.toString() // 订单信息
                Thread(PayRunnable(activity, PayHandler(activity),orderInfo)).start()
            }
            override fun onFail(code: Int?, msg: String?) {
                super.onFail(code, msg)
                DialogUtils.dismissLoading()
            }
        })
    }


    class PayRunnable(
        activity: FragmentActivity,
        private val payHandler: PayHandler,
        private val orderInfo: String,
    ) : Runnable {
        private var mActivityRef: WeakReference<FragmentActivity>? = null

        init {
            mActivityRef = WeakReference(activity)
        }

        override fun run() {
            mActivityRef?.apply {
                val activity = get()
                activity?.apply {
                    val alipay = PayTask(mActivityRef!!.get())
                    val result = alipay.payV2(orderInfo, true)
                    val msg = Message()
                    msg.what = FLAG_ALI_PAY
                    msg.obj = result
                    payHandler.sendMessage(msg)
                }

            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class PayHandler(activity: FragmentActivity) : Handler(Looper.getMainLooper()) {
        private var mActivityRef: WeakReference<FragmentActivity>? = null
        init {
            mActivityRef = WeakReference(activity)
        }
        override fun handleMessage(msg: Message) {
            if (msg.what == FLAG_ALI_PAY) {
                val payResult = PayResult(msg.obj as Map<String?, String?>)

                /**
                 * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                 */
                val resultInfo = payResult.result // 同步返回需要验证的信息

                val resultStatus = payResult.resultStatus
                ThreadUtils.getMainHandler().post {
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, ALIPAY_SUCCESS)) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        showToast("支付成功")
                        LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.PAY_RESULT,true)
                    } else if (TextUtils.equals(
                            resultStatus, ALIPAY_CANCEL
                        )
                    ) {
                        showToast("支付取消")
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        showToast("支付失败")
                        LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.PAY_RESULT,false)
                    }
                }

            }
        }
    }
}