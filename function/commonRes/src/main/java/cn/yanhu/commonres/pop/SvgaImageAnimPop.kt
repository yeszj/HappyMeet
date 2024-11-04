package cn.yanhu.commonres.pop

import android.annotation.SuppressLint
import android.content.Context
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.databinding.PopSvgaImageAnimBinding
import cn.yanhu.commonres.utils.SVGAUtils
import com.blankj.utilcode.util.ScreenUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.animator.PopupAnimator
import com.lxj.xpopup.animator.TranslateAnimator
import com.lxj.xpopup.enums.PopupAnimation
import com.lxj.xpopup.impl.FullScreenPopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity

/**
 * @author: zhengjun
 * created: 2024/8/13
 * desc:
 */
@SuppressLint("ViewConstructor")
class SvgaImageAnimPop(context: Context, val url: String) : FullScreenPopupView(context) {
    override fun getImplLayoutId(): Int {
        return cn.yanhu.commonres.R.layout.pop_svga_image_anim
    }

    private lateinit var mBinding: PopSvgaImageAnimBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopSvgaImageAnimBinding.bind(popupImplView)
        SVGAUtils.loadSVGAAnim(mBinding.chatSvga, url,object : SVGAParser.ParseCompletion{
            override fun onComplete(videoItem: SVGAVideoEntity) {
            }
            override fun onError() {
                dismiss()
            }
        })
        mBinding.chatSvga.callback = object : SVGACallback{
            override fun onFinished() {
                dismiss()
            }
            override fun onPause() {
            }
            override fun onRepeat() {
            }
            override fun onStep(frame: Int, percentage: Double) {
            }
        }
        mBinding.chatSvga.setOnSingleClickListener { dismiss() }
    }

    override fun getPopupAnimator(): PopupAnimator {
        return TranslateAnimator(
            this.popupContentView, this.animationDuration, PopupAnimation.NoAnimation
        )
    }

    companion object {
        @JvmStatic
        fun showDialog(
            context: Context, url: String, simpleCallback: SimpleCallback
        ): SvgaImageAnimPop {
            val matchPop = SvgaImageAnimPop(context, url)
            val builder = XPopup.Builder(context)
            builder.maxHeight(ScreenUtils.getAppScreenHeight()).setPopupCallback(simpleCallback)
                .dismissOnTouchOutside(true).dismissOnBackPressed(true)
                .hasShadowBg(false).isDestroyOnDismiss(true).asCustom(matchPop)
                .show()
            return matchPop
        }
    }
}