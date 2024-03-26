package cn.huanyuan.happymeet.ui.login

import android.graphics.Typeface.BOLD
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.ActivityLoginBinding
import cn.yanhu.baselib.anim.AnimUtils
import cn.yanhu.baselib.anim.ShakeAnimator
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.spans.CustomClickSpan
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.router.RouterPath
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.RegexUtils
import kotlin.system.exitProcess

/**
 * @author: zhengjun
 * created: 2024/2/27
 * desc:
 */
@Route(path = RouterPath.ROUTER_LOGIN)
class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>(
    R.layout.activity_login,
    LoginViewModel::class.java
) {

    private var isPrivacyCheck = false
    override fun initData() {
        setFullScreenStatusBar(true)
        setStatusBarStyle(false)
        setAgreementInfo()
    }

    override fun initListener() {
        super.initListener()
        mBinding.ivCheck.setOnClickListener {
            if (isPrivacyCheck) {
                mBinding.ivCheck.setImageResource(cn.yanhu.commonres.R.drawable.svg_unselected_r20)
            } else {
                mBinding.ivCheck.setImageResource(cn.yanhu.commonres.R.drawable.svg_selected_r20)
            }
            isPrivacyCheck = !isPrivacyCheck
            mBinding.isSelect = isPrivacyCheck
        }
        mBinding.bgCode.setOnSingleClickListener {
            if (checkCondition()) {
                VerifyCodeActivity.lunch(mContext,mBinding.etPhone.text.toString())
            }
        }
        mBinding.bgWxLogin.setOnSingleClickListener {
            startWxLogin()
        }
    }

    private fun startWxLogin(){

    }

    private fun checkCondition(): Boolean {
        val phone = mBinding.etPhone.text.toString()
        if (!isPrivacyCheck) {
            showToast("请同意并勾选我们的协议")
            AnimUtils.with(ShakeAnimator()).playOn(mBinding.ivTips)
            return false
        } else if (TextUtils.isEmpty(phone)) {
            showToast("请输入手机号码")
            return false
        }else if (!RegexUtils.isMobileSimple(phone)){
            showToast("请输入正确的手机号码")
            return false
        }
        return true
    }

    private fun setAgreementInfo() {
        val build = Spans.builder()
            .text("同意并接受").color(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontGrayColor))
            .text("用户服务协议")
            .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontTextColor)).style(BOLD)
            .click(
                mBinding.tvAgreement,
                CustomClickSpan(
                    mContext,
                    CommonUtils.getColor(cn.yanhu.baselib.R.color.fontTextColor),
                    object : CustomClickSpan.OnAllSpanClickListener {
                        override fun onClick(widget: View?) {
                            toAgreementPage(WebUrlManager.USER_AGREEMENT)
                        }

                    })
            )
            .text("、").color(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontGrayColor))
            .text("隐私政策").color(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontTextColor))
            .click(
                mBinding.tvAgreement,
                CustomClickSpan(
                    mContext,
                    CommonUtils.getColor(cn.yanhu.baselib.R.color.fontTextColor),
                    object : CustomClickSpan.OnAllSpanClickListener {
                        override fun onClick(widget: View?) {
                            toAgreementPage(WebUrlManager.PRIVACY_AGREEMENT)
                        }

                    })
            )
            .style(BOLD)
            .text("、").color(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontGrayColor))
            .text("号码认证协议")
            .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontTextColor)).style(BOLD).click(
                mBinding.tvAgreement,
                CustomClickSpan(
                    mContext,
                    CommonUtils.getColor(cn.yanhu.baselib.R.color.fontTextColor),
                    object : CustomClickSpan.OnAllSpanClickListener {
                        override fun onClick(widget: View?) {
                            toAgreementPage(WebUrlManager.PHONE_AGREEMENT)
                        }

                    })
            )
            .build()
        mBinding.tvAgreement.text = build
    }

    private fun toAgreementPage(url: String) {
        RouteIntent.lunchToWebView(url)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            val isExit = CommonUtils.exitApp(keyCode)
            if (!isExit) {
                ActivityUtils.finishAllActivities()
                exitProcess(0)
            } else {
                true
            }
        } else {
            super.onKeyDown(keyCode, event)
        }
    }
}