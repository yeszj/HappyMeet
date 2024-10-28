package com.pcl.sdklib.sdk.wechat
import cn.yanhu.baselib.utils.ext.showToast
import com.blankj.utilcode.util.ActivityUtils
import com.pcl.sdklib.manager.SdkParamsManager
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.modelbiz.WXOpenCustomerServiceChat
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 * @author: zhengjun
 * created: 2023/2/16
 * desc:
 */
object WxCustomerServiceUtils  {

    fun askCustomer() {
        val topActivity = ActivityUtils.getTopActivity() ?: return
        val appId = SdkParamsManager.WX_APP_ID // 填移动应用(App)的 AppId
        val api = WXAPIFactory.createWXAPI(topActivity, appId)
        if (api.isWXAppInstalled){
            // 判断当前版本是否支持拉起客服会话
            if (api.wxAppSupportAPI >= Build.SUPPORT_OPEN_CUSTOMER_SERVICE_CHAT) {
                val req = WXOpenCustomerServiceChat.Req()
                req.corpId = SdkParamsManager.WX_COMPANY_ID // 企业ID
                req.url = SdkParamsManager.WX_CUSTOMER_URL // 客服URL
                api.sendReq(req)
            }else{
                showToast("请更新微信版本")
            }
        }else{
            showToast("请先安装微信")
        }
    }

}