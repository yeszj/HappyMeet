package cn.huanyuan.sweetlove.func.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Typeface
import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.PopPrivacyBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.widget.spans.CustomClickSpan
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.router.RouteIntent
import com.blankj.utilcode.util.ScreenUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView

/**
 * @author: zhengjun
 * created: 2024/2/28
 * desc:
 */
@SuppressLint("ViewConstructor")
class PrivacyPop(
    val mContext: Activity,
    private val onPrivacyClickListener: OnPrivacyClickListener
) : CenterPopupView(mContext) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_privacy
    }

    private var mBinding: PopPrivacyBinding? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = PopPrivacyBinding.bind(popupImplView)
        mBinding?.apply {
            val build = Spans.builder()
                .text("欢迎使用${CommonUtils.getString(R.string.app_name)}!\n")
                .text("请您务必谨慎阅读充分理解")
                .text("《用户隐私政策》")
                .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.customer_service_blue))
                .style(Typeface.BOLD)
                .click(
                    tvContent,
                    CustomClickSpan(
                        mContext,
                        CommonUtils.getColor(cn.yanhu.baselib.R.color.customer_service_blue),
                        object : CustomClickSpan.OnAllSpanClickListener {
                            override fun onClick(widget: View?) {
                                toAgreementPage(WebUrlManager.PRIVACY_AGREEMENT)
                            }

                        })
                )
                .text("和")
                .text("《用户服务协议》")
                .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.customer_service_blue))
                .style(Typeface.BOLD)
                .click(
                    tvContent,
                    CustomClickSpan(
                        mContext,
                        CommonUtils.getColor(cn.yanhu.baselib.R.color.customer_service_blue),
                        object : CustomClickSpan.OnAllSpanClickListener {
                            override fun onClick(widget: View?) {
                                toAgreementPage(WebUrlManager.USER_AGREEMENT)
                            }

                        })
                )
                .text("的各条款。包括但不限于:1、我们会根据你使用服务的具体功能需要，收集必要的用户信息(包括手机号、设备信息、地理位置等)或取得必要的授权(文件读取权限、文件写入权限等)。2、未经您的同意，我们不会从第三方获取，共享或对外提供您的信息。3、我们会收集您的Mac地址、OAID等设备信息进行SDK的初始化工作，并保障APP正常数据统计和安全风控。您可通过阅读完整版的")

                .text("《用户隐私政策》")
                .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.customer_service_blue))
                .style(Typeface.BOLD)
                .click(
                    tvContent,
                    CustomClickSpan(
                        mContext,
                        CommonUtils.getColor(cn.yanhu.baselib.R.color.customer_service_blue),
                        object : CustomClickSpan.OnAllSpanClickListener {
                            override fun onClick(widget: View?) {
                                toAgreementPage(WebUrlManager.PRIVACY_AGREEMENT)
                            }

                        })
                )
                .text("和")
                .text("《用户服务协议》")
                .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.customer_service_blue))
                .style(Typeface.BOLD)
                .click(
                    tvContent,
                    CustomClickSpan(
                        mContext,
                        CommonUtils.getColor(cn.yanhu.baselib.R.color.customer_service_blue),
                        object : CustomClickSpan.OnAllSpanClickListener {
                            override fun onClick(widget: View?) {
                                toAgreementPage(WebUrlManager.USER_AGREEMENT)
                            }

                        })
                )
                .build()
            tvContent.text = build
            tvAgree.setOnSingleClickListener {
                dismiss()
                onPrivacyClickListener.onAgree()
            }
            tvDisAgree.setOnSingleClickListener {
                dismiss()
                onPrivacyClickListener.onDisAgree()
            }
        }
    }

    interface OnPrivacyClickListener {
        fun onAgree()
        fun onDisAgree()
    }

    private fun toAgreementPage(url: String) {
        RouteIntent.lunchToWebView(url)
    }

    companion object {
        fun showPop(
            mContext: Activity, onCommitAccostListener: OnPrivacyClickListener
        ): PrivacyPop {
            val matchPop = PrivacyPop(mContext, onCommitAccostListener)
            val builder = XPopup.Builder(mContext)
            builder
                .isDestroyOnDismiss(true)
                .maxHeight((ScreenUtils.getScreenHeight()*0.8).toInt())
                .asCustom(matchPop).show()
            return matchPop
        }
    }
}