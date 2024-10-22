package cn.yanhu.commonres.pop

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.View
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.widget.spans.CustomClickSpan
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.databinding.PopAgreementTipsBinding
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.router.PageIntentUtil
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * @author: zhengjun
 * created: 2024/8/6
 * desc:
 */
@SuppressLint("ViewConstructor")
class AgreementTipsPop(
    context: Context,
    private val operatorProtocalUrl: String?,
    private val protocalName: String?,
    private val onClickAgreeListener: OnClickAgreeListener
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return cn.yanhu.commonres.R.layout.pop_agreement_tips
    }

    private lateinit var mBinding: PopAgreementTipsBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopAgreementTipsBinding.bind(popupImplView)
        val builder = Spans.builder()
        if (!TextUtils.isEmpty(operatorProtocalUrl)) {
            builder.text(protocalName).click(
                mBinding.tvAgreement,
                CustomClickSpan(
                    context,
                        CommonUtils.getColor(cn.yanhu.baselib.R.color.colorMain),
                    object : CustomClickSpan.OnAllSpanClickListener {
                        override fun onClick(widget: View?) {
                            toAgreementDetail(operatorProtocalUrl!!)
                        }
                    })
            )
        }
        builder.text("《用户服务协议》${if (!TextUtils.isEmpty(operatorProtocalUrl)) "\n" else ""}")
            .click(
                mBinding.tvAgreement,
                CustomClickSpan(
                    context,
                    CommonUtils.getColor(cn.yanhu.baselib.R.color.colorMain),
                    object : CustomClickSpan.OnAllSpanClickListener {
                        override fun onClick(widget: View?) {
                            toAgreementDetail(WebUrlManager.USER_AGREEMENT)
                        }
                    })
            )
        builder.text("《用户隐私政策》${if (TextUtils.isEmpty(operatorProtocalUrl)) "\n" else ""}")
            .click(
                mBinding.tvAgreement,
                CustomClickSpan(
                    context,
                    CommonUtils.getColor(cn.yanhu.baselib.R.color.colorMain),
                    object : CustomClickSpan.OnAllSpanClickListener {
                        override fun onClick(widget: View?) {
                            toAgreementDetail(WebUrlManager.PRIVACY_AGREEMENT)
                        }
                    })
            )
        builder.text("《号码认证协议》").click(
            mBinding.tvAgreement,
            CustomClickSpan(
                context,
                CommonUtils.getColor(cn.yanhu.baselib.R.color.colorMain),
                object : CustomClickSpan.OnAllSpanClickListener {
                    override fun onClick(widget: View?) {
                        toAgreementDetail(WebUrlManager.PHONE_AGREEMENT)
                    }
                })
        )
        val build = builder.build()
        mBinding.tvAgreement.text = build
        mBinding.tvAgree.setOnSingleClickListener {
            onClickAgreeListener.onAgree()
            dismiss()
        }
    }

    private fun toAgreementDetail(url: String) {
        PageIntentUtil.url2Page(context, url)
    }

    interface OnClickAgreeListener {
        fun onAgree()
    }

    companion object {
        @JvmStatic
        fun showDialog(
            context: Context,
            operatorProtocalUrl: String?,
            protocalName: String?,
            onClickAgreeListener: OnClickAgreeListener
        ): AgreementTipsPop {
            val matchPop =
                AgreementTipsPop(context, operatorProtocalUrl, protocalName, onClickAgreeListener)
            val builder = XPopup.Builder(context)
            builder
                .isDestroyOnDismiss(true).asCustom(matchPop).show()
            return matchPop
        }
    }
}