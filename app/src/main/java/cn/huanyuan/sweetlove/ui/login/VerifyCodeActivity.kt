package cn.huanyuan.sweetlove.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityVerifyCodeBinding
import cn.huanyuan.sweetlove.func.manager.LoginResultManager
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.ext.countDown
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.SimpleTextWatcher
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.zj.netrequest.ext.parseState
import com.blankj.utilcode.util.RegexUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * @author: zhengjun
 * created: 2024/2/28
 * desc:
 */
class VerifyCodeActivity : BaseActivity<ActivityVerifyCodeBinding, LoginViewModel>(
    R.layout.activity_verify_code,
    LoginViewModel::class.java
) {
    override fun initData() {
        setFullScreenStatusBar(true)
        setStatusBarStyle(false)
        checkInputListener()
        mBinding.viewModel = mViewModel
        val phone = intent.getStringExtra(IntentKeyConfig.DATA)
        if (!TextUtils.isEmpty(phone)){
            mViewModel.phoneExt.set(phone)
            sendVerifyCode()
        }
    }

    override fun initListener() {
        super.initListener()
        mBinding.btnCode.setOnSingleClickListener {
            if (checkInputValue()) {
                sendVerifyCode()
            }
        }
        mBinding.btnNext.setOnSingleClickListener {
            startPhoneLogin()
        }
    }

    private fun sendVerifyCode(){
        mViewModel.sendVerifyCode()
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.codeLivedata.observe(this){ it ->
            parseState(it,{
                showToast(it)
                startCodeTimeDown()
            })
        }
        mViewModel.loginLivedata.observe(this){ it ->
            parseState(it,{
                LoginResultManager.loginSuccess(mContext,it)
            })
        }
    }


    private fun startPhoneLogin(){
        mViewModel.login(0)
    }

    private fun checkInputListener() {
        mBinding.etPhone.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                val code = mBinding.etCode.text.toString()
                setBtnEnable(s, code )
            }
        })

        mBinding.etCode.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                val phone = mBinding.etPhone.text.toString()
                setBtnEnable(phone, s )
            }
        })
    }

    private fun setBtnEnable(phone: CharSequence?, code: CharSequence?){
        if (!TextUtils.isEmpty(phone) && RegexUtils.isMobileSimple(phone) && !TextUtils.isEmpty(
                code)
        ) {
            mBinding.btnNext.isEnabled = true
            mBinding.btnNext.alpha = 1.0f
        } else {
            mBinding.btnNext.isEnabled = false
            mBinding.btnNext.alpha = 0.3f
        }
    }

    private fun checkInputValue(): Boolean {
        val phone = mBinding.etPhone.text.toString()
        if (TextUtils.isEmpty(phone)) {
            showToast("请输入手机号码")
            return false
        } else if (!RegexUtils.isMobileSimple(phone)) {
            showToast("请输入正确的手机号码")
            return false
        }
        return true
    }

    private var timeDownScope: CoroutineScope? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("SetTextI18n")
    private fun startCodeTimeDown() {
        mContext.countDown(start = {
            timeDownScope = it
            mBinding.btnCode.alpha = 0.3f
            mBinding.btnCode.isEnabled = false
            mBinding.btnCode.text = "60s"
        }, end = {
            mBinding.btnCode.alpha = 1f
            mBinding.btnCode.isEnabled = true
            mBinding.btnCode.text = "获取验证码"
        }, next = {
            mBinding.btnCode.text = "${it}s"
        }, cancel = {})
    }


    companion object {
        fun lunch(context: FragmentActivity, phone: String = "") {
            val intent = Intent(context, VerifyCodeActivity::class.java)
            intent.putExtra(IntentKeyConfig.DATA, phone)
            context.startActivity(intent)
        }
    }
}