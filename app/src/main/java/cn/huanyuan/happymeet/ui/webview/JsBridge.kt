package cn.huanyuan.happymeet.ui.webview

import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.happymeet.net.HttpHeadConfig
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.router.PageIntentUtil
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
}