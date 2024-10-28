package cn.yanhu.commonres.pop

import android.content.Context
import com.lxj.xpopup.core.CenterPopupView
import cn.yanhu.commonres.R
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.databinding.PopTeenTipBinding
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
        mBiding.ivClose.setOnSingleClickListener { dismiss() }
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