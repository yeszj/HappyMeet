package cn.huanyuan.sweetlove.func.dialog

import android.content.Context
import com.lxj.xpopup.core.CenterPopupView
import cn.huanyuan.sweetlove.R
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.huanyuan.sweetlove.databinding.PopTeenTipBinding
import cn.huanyuan.sweetlove.ui.teenage.TeenAgeModeActivity
import com.blankj.utilcode.util.ScreenUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.SimpleCallback

/**
 * @author: zhengjun
 * created: 2024/10/28
 * desc:
 */
class TeenTipPop(context: Context) : CenterPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.pop_teen_tip
    }

    private lateinit var mBiding : PopTeenTipBinding
    override fun onCreate() {
        super.onCreate()
        mBiding = PopTeenTipBinding.bind(popupImplView)
        mBiding.tvClose.setOnSingleClickListener { dismiss() }
        mBiding.tvOpen.setOnSingleClickListener {
            TeenAgeModeActivity.lunch(context,false)
            dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,simpleCallback: SimpleCallback
        ): TeenTipPop {
            val matchPop = TeenTipPop(mContext)
            val builder = XPopup.Builder(mContext)
            builder
                .setPopupCallback(simpleCallback)
                .maxWidth(ScreenUtils.getScreenWidth())
                .asCustom(matchPop).show()
            return matchPop
        }
    }
}