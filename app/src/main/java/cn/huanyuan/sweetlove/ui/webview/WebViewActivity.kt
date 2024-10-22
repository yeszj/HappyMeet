package cn.huanyuan.sweetlove.ui.webview

import android.content.Context
import android.content.Intent
import android.view.View
import android.webkit.WebView
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityWebviewBinding
import cn.huanyuan.sweetlove.net.HttpHeadConfig
import cn.huanyuan.sweetlove.ui.webview.config.MyWebChromeClient
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.WebViewUtil
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.baselib.widget.MyWebViewClient
import cn.yanhu.commonres.router.RouterPath
import com.alibaba.android.arouter.facade.annotation.Route

/**
 * @author: witness
 * created: 2022/3/11
 * desc:
 */
@Route(path = RouterPath.ROUTER_WEBVIEW)
class WebViewActivity : BaseActivity<ActivityWebviewBinding, WebViewModel>(
    R.layout.activity_webview,
    WebViewModel::class.java
) {
    override fun initBeforeContentView() {
        // 开启 WebView 长截图，在 setContentView() 方法前面设置才有效
        WebView.enableSlowWholeDocumentDraw()
    }

    override fun initData() {
        val url = intent.getStringExtra(WEB_URL)
        WebViewUtil.initWebViews(mBinding.webView)
        mBinding.webView.webChromeClient =
            MyWebChromeClient(mBinding.title).setProgressBar(mBinding.progressBar)
        mBinding.webView.webViewClient = MyWebViewClient()
        mBinding.webView.addJavascriptInterface(JsBridge(mBinding.webView,mContext), "AppJs")
        mBinding.webView.loadUrl(url.toString(), HttpHeadConfig.getHeader())
        mBinding.title.setTitleButtonOnClickListener(object : TitleBar.TitleButtonOnClickListener {
            override fun leftButtonOnClick(v: View?) {
                back()
            }

            override fun rightButtonOnClick(v: View?) {
            }
        })
    }

    override fun initStatusBar() {
        setStatusBarToWhite()
    }

    override fun exactDestroy() {
        super.exactDestroy()
        WebViewUtil.clearWebView(mBinding.webView)
    }

    companion object {
        const val WEB_URL: String = "url"
        fun lunch(context: Context, url: String) {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(WEB_URL, url)
            context.startActivity(intent)
        }
    }
}