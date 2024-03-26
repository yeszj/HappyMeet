package cn.yanhu.baselib.widget

import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.webkit.*


/**
 * Created by Ace on 2018/5/11.
 */
open class MyWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        //调用拨号、短信等程序
        if (url.startsWith("mailto:") || url.startsWith("geo:")
            || url.startsWith("tel:") || url.startsWith("sms:")
            || url.startsWith("taobao:") || url.startsWith("alipayqr:")
            || url.contains(".apk")
        ) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
            view.context.startActivity(intent)
            return true
        } else  if (!url.startsWith("http") && !url.startsWith("https")) {
            try {
                // 以下固定写法
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
                intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                view.context.startActivity(intent)
            } catch (e: Exception) {
                // 防止没有安装的情况
                e.printStackTrace()
            }
            return true
        }else{
            return super.shouldOverrideUrlLoading(view, request)
        }
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        super.onReceivedSslError(view, handler, error)
        //handler.proceed()
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
    }
}