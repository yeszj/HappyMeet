package com.pcl.sdklib.manager

import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.PayWayInfo
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.jeremyliao.liveeventbus.LiveEventBus
import com.pcl.sdklib.api.sdkRxApi
import com.pcl.sdklib.listener.OnPayResultListener
import com.pcl.sdklib.sdk.alipay.AliPayUtils
import com.pcl.sdklib.sdk.wechat.WxPayUtils

/**
 * @author: zhengjun
 * created: 2024/3/8
 * desc:
 */
object PayManager {
    const val RECHARGE_TYPE_ROSE = 1 //充值玫瑰
    const val RECHARGE_TYPE_LIVE_TIME = 2 //购买开播时长

    /**
     * 监听支付结果
     * 在支付页面调用此方法 可在支付成功后进行刷新页面等操作
     */
    fun registerPayResult(activity: FragmentActivity, onPayResultListener: OnPayResultListener) {
        LiveEventBus.get<Boolean>(LiveDataEventManager.PAY_RESULT).observe(activity) {
            if (it) {
                onPayResultListener.onPaySuccess()
            } else {
                onPayResultListener.onPayFail()
            }
        }
    }

    /**
     * @param selectType  PayWayInfo.TYPE_ALIPAY --1支付宝   PayWayInfo.TYPE_WXPAY --2微信
     * @param rechargeId 充值id
     * @param rechargeType 充值类型 RECHARGE_TYPE_ROSE--1玫瑰充值 RECHARGE_TYPE_LIVE_TIME--2购买开播时长
     */
    fun toPay(
        activity: FragmentActivity,
        selectType: Int,
        rechargeId: Int,
        rechargeType: Int = RECHARGE_TYPE_ROSE
    ) {
        request({ sdkRxApi.payCheck(rechargeId.toString())},object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {
                toPay(selectType, rechargeId, activity, rechargeType)
            }
            override fun onFail(code: Int?, msg: String?) {
                super.onFail(code, msg)
                when (code) {
                    324 -> {
                        DialogUtils.showConfirmDialog("温馨提示",{
                            toPay(selectType, rechargeId, activity, rechargeType)
                        },{
                        },msg!!,"取消支付","继续支付")
                    }
                    else -> {
                        showToast(msg)
                    }
                }
            }
        },false,activity = activity)


    }

    private fun toPay(
        selectType: Int,
        rechargeId: Int,
        activity: FragmentActivity,
        rechargeType: Int
    ) {
        if (selectType == PayWayInfo.TYPE_ALIPAY) {
            AliPayUtils.aliPay(rechargeId, activity, rechargeType)
        } else {
            WxPayUtils.wxPay(rechargeId, activity, rechargeType)
        }
    }
}