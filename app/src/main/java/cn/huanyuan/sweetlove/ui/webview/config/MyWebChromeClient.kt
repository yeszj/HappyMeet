package cn.huanyuan.sweetlove.ui.webview.config

import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.sweetlove.net.HttpHeadConfig
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.manager.ImageSelectManager
import com.google.gson.Gson
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import java.io.File

/**
 * Created by Ace on 2018/5/11.
 */
class MyWebChromeClient(var mContext: FragmentActivity, titleView: TitleBar) : WebChromeClient() {
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

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<out Uri?>?>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        //这是一个全局变量
        if (fileChooserParams != null
            && fileChooserParams.acceptTypes != null
        ) {
            showSelectPhotoPop(filePathCallback)
        }
        return true
    }

    private fun showSelectPhotoPop(filePathCallback: ValueCallback<Array<out Uri?>?>?) {
        ImageSelectManager.selectPic(
            mContext,
            false,
            CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_336),
            CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_180),
            1,
            object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia?>?) {
                    result ?: return
                    val resultArray = arrayOfNulls<Uri>(result.size)
                    for (i in result.indices) {
                        resultArray[i] = Uri.fromFile(File(result[i]!!.getAvailablePath()))
                    }
                    filePathCallback?.onReceiveValue(resultArray)
                }

                override fun onCancel() {
                    filePathCallback?.onReceiveValue(arrayOf<Uri?>())
                }
            })
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