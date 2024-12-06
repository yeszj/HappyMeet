package cn.huanyuan.sweetlove.func.dialog

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Context
import cn.huanyuan.sweetlove.bean.GlobalGiftBean
import cn.huanyuan.sweetlove.databinding.PopGlobalGiftBinding
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.PositionPopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.func.task.AppPopTask
import cn.yanhu.baselib.utils.CommonUtils

/**
 * @author: zhengjun
 * created: 2024/7/17
 * desc:
 */
@SuppressLint("ViewConstructor")
class GlobalGiftPop(context: Context, private val globalGiftBean: GlobalGiftBean) :
    PositionPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_global_gift
    }

    private lateinit var mBinding: PopGlobalGiftBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopGlobalGiftBinding.bind(popupImplView)
    }


    override fun doAfterShow() {
        super.doAfterShow()
        mBinding.globalGift.setModel(globalGiftBean)
        val animatorSet: AnimatorSet = mBinding.globalGift.startAnimation()
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                dismiss()
            }
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        AppPopTask.currentPopTask?.doNextTask()
    }


    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            globalGiftBean: GlobalGiftBean,
            calBack: SimpleCallback? = null
        ): GlobalGiftPop {
            val matchPop = GlobalGiftPop(mContext, globalGiftBean)
            val builder = XPopup.Builder(mContext)
            builder.dismissOnTouchOutside(false)
                .dismissOnBackPressed(false)
                .setPopupCallback(calBack)
                .offsetY(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_60))
                .enableDrag(true)
                .hasShadowBg(false)
                .isRequestFocus(false)
                .isViewMode(true)
                .isClickThrough(true)
                .isTouchThrough(true)
                .enableShowWhenAppBackground(true)
                .isDestroyOnDismiss(true).asCustom(matchPop).show()
            return matchPop
        }
    }

}