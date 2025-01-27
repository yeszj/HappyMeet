package cn.huanyuan.sweetlove.ui.login

import android.graphics.Typeface.BOLD
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityLoginBinding
import cn.huanyuan.sweetlove.func.manager.LoginResultManager
import cn.jiguang.verifysdk.api.JVerificationInterface
import cn.yanhu.baselib.anim.AnimUtils
import cn.yanhu.baselib.anim.ShakeAnimator
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.SoftHideKeyBoardUtil
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.spans.CustomClickSpan
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.router.RouterPath
import cn.zj.netrequest.ext.parseState
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.RegexUtils
import com.pcl.sdklib.sdk.jverrify.JiGuangSDKUtils
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
        SoftHideKeyBoardUtil.assistActivity(mContext)
        setFullScreenStatusBar(true)
        setAgreementInfo()
        initJiGuangLogin()
    }

    private fun initJiGuangLogin() {
        JiGuangSDKUtils.getInstance().setJiGuangSDKListener(object :
            JiGuangSDKUtils.JiGuangSdkCallback() {
            override fun fail(msg: String?) {
                super.fail(msg)
            }

            override fun successLoginToken(loginToken: String, operator: String?) {
                super.successLoginToken(loginToken, operator)
                getPhoneByJiguangToken(loginToken)
            }

            override fun jumpPhoneLogin() {
                super.jumpPhoneLogin()
                JVerificationInterface.clearPreLoginCache() //清除预取号缓存
                JVerificationInterface.dismissLoginAuthActivity() //关闭授权页
            }
        })
        JiGuangSDKUtils.getInstance().startLogin()
    }

    private fun getPhoneByJiguangToken(loginToken: String) {
        mViewModel.getPhone(loginToken)
        mViewModel.getPhoneLivedata.observe(this@LoginActivity) { it ->
            parseState(it, {
                startLogin(it)
            })
        }
    }

    private fun startLogin(it: String) {
        mViewModel.jiGuangLogin(it)
        mViewModel.loginLivedata.observe(this@LoginActivity) { it ->
            parseState(it, {
                LoginResultManager.loginSuccess(mContext, it)
            })
        }
    }

    override fun initListener() {
        super.initListener()
        mBinding.etPhone.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                if (RegexUtils.isMobileExact(s.toString())) {
                    mBinding.bgCode.alpha = 1.0f
                    mBinding.bgCode.isEnabled = true
                } else if (mBinding.bgCode.isEnabled) {
                    mBinding.bgCode.alpha = 0.5f
                    mBinding.bgCode.isEnabled = false
                }
            }

        })
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
                KeyboardUtils.hideSoftInput(mBinding.etPhone)
                VerifyCodeActivity.lunch(mContext, mBinding.etPhone.text.toString())
            }
        }
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
        } else if (!RegexUtils.isMobileSimple(phone)) {
            showToast("请输入正确的手机号码")
            return false
        }
        return true
    }

    private fun setAgreementInfo() {
        val build = Spans.builder()
            .text("同意并接受").color(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontGrayColor))
            .text("用户服务协议")
            .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.white)).style(BOLD)
            .click(
                mBinding.tvAgreement,
                CustomClickSpan(
                    mContext,
                    CommonUtils.getColor(cn.yanhu.baselib.R.color.white),
                    object : CustomClickSpan.OnAllSpanClickListener {
                        override fun onClick(widget: View?) {
                            toAgreementPage(WebUrlManager.USER_AGREEMENT)
                        }

                    })
            )
            .text("、").color(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontGrayColor))
            .text("隐私政策").color(CommonUtils.getColor(cn.yanhu.baselib.R.color.white))
            .click(
                mBinding.tvAgreement,
                CustomClickSpan(
                    mContext,
                    CommonUtils.getColor(cn.yanhu.baselib.R.color.white),
                    object : CustomClickSpan.OnAllSpanClickListener {
                        override fun onClick(widget: View?) {
                            toAgreementPage(WebUrlManager.PRIVACY_AGREEMENT)
                        }

                    })
            )
            .style(BOLD)
            .text("、").color(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontGrayColor))
            .text("号码认证协议")
            .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.white)).style(BOLD).click(
                mBinding.tvAgreement,
                CustomClickSpan(
                    mContext,
                    CommonUtils.getColor(cn.yanhu.baselib.R.color.white),
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