package com.pcl.sdklib.sdk.wechat

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.pcl.sdklib.api.sdkRxApi
import com.pcl.sdklib.bean.PayRequest
import com.pcl.sdklib.bean.WechatPayData
import com.pcl.sdklib.manager.PayManager
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 * @author: zhengjun
 * created: 2023/11/13
 * desc:
 */
object WxPayUtils {
    @SuppressLint("CheckResult")
    @JvmStatic
    fun wxPay(rechargeId: Int, activity: FragmentActivity,rechargeType:Int = PayManager.RECHARGE_TYPE_ROSE) {
        DialogUtils.showLoading()
        val payRequest = PayRequest(rechargeId,rechargeType)
        request({ sdkRxApi.wxpay(payRequest)},object : OnRequestResultListener<WechatPayData>{
            override fun onSuccess(data: BaseBean<WechatPayData>) {
                DialogUtils.dismissLoading()
                val payInfo = data.data ?: return
                val api = WXAPIFactory.createWXAPI(activity, payInfo.appid)
                if (api.isWXAppInstalled){
                    val request = PayReq()
                    request.appId = payInfo.appid
                    request.partnerId = payInfo.partnerid
                    request.prepayId = payInfo.prepayid
                    request.packageValue = payInfo.packageX
                    request.nonceStr = payInfo.noncestr
                    request.timeStamp = payInfo.timestamp
                    request.sign = payInfo.sign
                    api.sendReq(request)
                }else{
                    showToast("请先安装微信")
                }
            }

            override fun onFail(code: Int?, msg: String?) {
                super.onFail(code, msg)
                DialogUtils.dismissLoading()
            }
        })
    }
}