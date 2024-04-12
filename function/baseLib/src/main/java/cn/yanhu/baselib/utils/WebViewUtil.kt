@file:Suppress("DEPRECATION")

package cn.yanhu.baselib.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import com.blankj.utilcode.util.ActivityUtils


/**
 * @author: witness
 * created: 2022/3/11
 * desc:
 */
object WebViewUtil {

    @SuppressLint("SetJavaScriptEnabled", "SdCardPath")
    fun initWebViews(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true
            javaScriptCanOpenWindowsAutomatically = true
            loadsImagesAutomatically = true
            domStorageEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            loadWithOverviewMode = true
            allowFileAccess = true
            allowContentAccess = true
            defaultTextEncodingName = "utf-8"
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            cacheMode = WebSettings.LOAD_DEFAULT
            databaseEnabled = true
            databasePath = "/data/data/"+ ActivityUtils.getTopActivity().packageName+"/databases/"
        }

        webView.isDrawingCacheEnabled = true
        webView.isVerticalScrollBarEnabled = false
        WebView.setWebContentsDebuggingEnabled(true)
       // webView.addJavascriptInterface(JsBridge(webView), "AppJs")
        dealJavascriptLeak(webView)
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun dealJavascriptLeak(webView: WebView) {
        webView.removeJavascriptInterface("searchBoxJavaBridge_")
        webView.removeJavascriptInterface("accessibility")
        webView.removeJavascriptInterface("accessibilityTraversal")
    }

    fun clearWebView(webView: WebView) {
        webView.apply {
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            settings.javaScriptEnabled = false
            clearCache(true)
            clearHistory()
            (webView.parent as ViewGroup).removeView(webView)
            removeAllViews()
            destroy()
        }
    }
}