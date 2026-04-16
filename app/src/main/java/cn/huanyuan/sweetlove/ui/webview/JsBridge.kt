package cn.huanyuan.sweetlove.ui.webview

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.sweetlove.net.HttpHeadConfig
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.router.PageIntentUtil
import cn.zj.netrequest.application.ApplicationProxy
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.pcl.sdklib.manager.SdkParamsManager
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessView
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import org.json.JSONException
import org.json.JSONObject

/**
 * @author: zhengjun
 * created: 2023/2/14
 * desc:
 */
@Keep
class JsBridge(private var mWebView: WebView, private var context: FragmentActivity) {
    @JavascriptInterface
    fun getDeviceInfo(): String {
        return if (!AppCacheManager.isFirstOpenApp){
            val header = HttpHeadConfig.getHeader()
            val toString = JSONObject((header as Map<*, *>?)!!).toString()
            val script = "javascript:saveDeviceInfo('$toString')"
            mWebView.post { mWebView.loadUrl(script) }
            toString
        }else{
            ""
        }
    }

    //跳原生页面
    @JavascriptInterface
    @Throws(JSONException::class)
    fun toClientPath(pageUrl: String) {
        logcom(pageUrl)
        PageIntentUtil.url2Page(context, pageUrl)
    }

    @JavascriptInterface
    fun logout(){
        ApplicationProxy.instance.loginInvalid()
    }

    @JavascriptInterface
    fun requestMerchantTransfer(query: String?) {
        val api = WXAPIFactory.createWXAPI(context, SdkParamsManager.WX_APP_ID)
        val wxSdkVersion: Int = api.wxAppSupportAPI
        if (wxSdkVersion >= Build.OPEN_BUSINESS_VIEW_SDK_INT) {
            val req = WXOpenBusinessView.Req()
            req.businessType = "requestMerchantTransfer"
            req.query = query
            api.sendReq(req)
        } else {
            /*需提示用户升级微信版本*/
            showToast("请更新微信版本")
        }
    }

    @JavascriptInterface
    fun goBackPage() {
        runOnUiThread(object : Runnable {
            override fun run() {
                if (mWebView.canGoBack()) {
                    mWebView.goBack()
                } else {
                    context.finish()
                }
            }
        })
    }
}