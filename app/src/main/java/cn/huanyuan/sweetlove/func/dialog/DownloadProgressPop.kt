package cn.huanyuan.sweetlove.func.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import cn.huanyuan.sweetlove.databinding.PopDownloadProgressBinding
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.interfaces.SimpleCallback

/**
 * @author: zhengjun
 * created: 2023/10/10
 * desc:
 */
@SuppressLint("ViewConstructor")
class DownloadProgressPop(context: Context, private val downLoadProgress: Int) :
    CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return cn.huanyuan.sweetlove.R.layout.pop_download_progress
    }

    private var mBinding: PopDownloadProgressBinding? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        mBinding = PopDownloadProgressBinding.bind(popupImplView)
        mBinding?.apply {
            setProgress(downLoadProgress)
        }
    }

    @SuppressLint("SetTextI18n")
    fun setProgress(downLoadProgress: Int) {
        mBinding?.apply {
            progress.setProgressNum(downLoadProgress.toFloat(), 0)
            tvProgress.text = "$downLoadProgress%"
            if (downLoadProgress >= 100) {
                dismiss()
            }
        }
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Activity,
            progress: Int,
            xPopupCallback: SimpleCallback
        ): DownloadProgressPop {
            val sexPop = DownloadProgressPop(mContext, progress)
            val builder = XPopup.Builder(mContext)
            builder
                .isClickThrough(true)
                .setPopupCallback(xPopupCallback)
                .isDestroyOnDismiss(true).asCustom(sexPop).show()
            return sexPop
        }
    }
}