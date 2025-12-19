package com.pcl.sdklib.sdk.wechat
import android.text.Html
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.manager.AppCacheManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ActivityUtils
import com.pcl.sdklib.api.sdkRxApi
import com.pcl.sdklib.bean.ServiceInfo
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
        request({ sdkRxApi.getServiceInfo() },object : OnRequestResultListener<ServiceInfo>{
            override fun onSuccess(data: BaseBean<ServiceInfo>) {
                val serviceInfo = data.data?:return
                val content = Html.fromHtml(serviceInfo.notice, Html.FROM_HTML_MODE_LEGACY)
                DialogUtils.showConfirmDialog("温馨提示",{
                    val topActivity = ActivityUtils.getTopActivity()
                    val appId = SdkParamsManager.WX_APP_ID // 填移动应用(App)的 AppId
                    val api = WXAPIFactory.createWXAPI(topActivity, appId)
                    if (api.isWXAppInstalled){
                        // 判断当前版本是否支持拉起客服会话
                        if (api.wxAppSupportAPI >= Build.SUPPORT_OPEN_CUSTOMER_SERVICE_CHAT) {
                            val req = WXOpenCustomerServiceChat.Req()
                            req.corpId = SdkParamsManager.WX_COMPANY_ID // 企业ID
                            req.url = serviceInfo.url // 客服URL
                            api.sendReq(req)
                        }else{
                            showToast("请更新微信版本")
                        }
                    }else{
                        showToast("请先安装微信")
                    }
                },{

                },content, confirm = "确认授权", cancel = "取消授权")

            }

        })


    }

}