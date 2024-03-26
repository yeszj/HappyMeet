package com.pcl.sdklib.manager

import androidx.fragment.app.FragmentActivity
import cn.yanhu.commonres.bean.PayWayInfo
import cn.yanhu.commonres.manager.LiveDataEventManager
import com.jeremyliao.liveeventbus.LiveEventBus
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
        if (selectType == PayWayInfo.TYPE_ALIPAY) {
            AliPayUtils.aliPay(rechargeId, activity, rechargeType)
        } else {
            WxPayUtils.wxPay(rechargeId, activity, rechargeType)
        }
    }
}