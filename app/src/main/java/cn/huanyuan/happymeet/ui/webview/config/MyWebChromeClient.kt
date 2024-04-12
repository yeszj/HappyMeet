package cn.huanyuan.happymeet.ui.webview.config

import android.text.TextUtils
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ProgressBar
import cn.huanyuan.happymeet.net.HttpHeadConfig
import cn.yanhu.baselib.view.TitleBar
import com.google.gson.Gson

/**
 * Created by Ace on 2018/5/11.
 */
class MyWebChromeClient(titleView: TitleBar) : WebChromeClient() {
    private var progressBar: ProgressBar? = null
    private var titleView: TitleBar? = titleView
    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        if (progressBar != null) {
            progressBar!!.progress = newProgress
            if (newProgress < 100) {
                progressBar!!.visibility = View.VISIBLE
            } else {
                val s = Gson().toJson(HttpHeadConfig.getHeader())
                view.loadUrl("javascript:saveDeviceInfo($s)")
                progressBar!!.visibility = View.GONE
            }
        }
    }

    override fun onReceivedTitle(view: WebView, title: String) {
        super.onReceivedTitle(view, title)
        if (titleView != null && !TextUtils.isEmpty(title) && !view.url!!.contains(title)) {
            titleView!!.setTitleName(title)
        }
    }

    fun setProgressBar(progressBar: ProgressBar?): MyWebChromeClient {
        this.progressBar = progressBar
        return this
    }
}