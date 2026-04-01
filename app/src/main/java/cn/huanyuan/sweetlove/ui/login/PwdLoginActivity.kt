package cn.huanyuan.sweetlove.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityPwdLoginBinding
import cn.huanyuan.sweetlove.databinding.ActivityVerifyCodeBinding
import cn.huanyuan.sweetlove.func.manager.LoginResultManager
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.TextViewDrawableUtils
import cn.yanhu.baselib.utils.ext.countDown
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.view.CustomFontEditText
import cn.yanhu.baselib.view.CustomFontEditText.OnClickRightDrawableListener
import cn.yanhu.baselib.widget.SimpleTextWatcher
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.status.ErrorCode
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.RegexUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * @author: zhengjun
 * created: 2024/2/28
 * desc:
 */
class PwdLoginActivity : BaseActivity<ActivityPwdLoginBinding, LoginViewModel>(
    R.layout.activity_pwd_login,
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
        }
    }

    override fun initListener() {
        super.initListener()
        mBinding.btnNext.setOnSingleClickListener {
            startPhoneLogin()
        }
        mBinding.connectedService.setOnSingleClickListener {
            ApplicationProxy.instance.askCustomer()
        }
        mBinding.etPwd.clickRightDrawableListener = object : OnClickRightDrawableListener{
            override fun clickRightDrawable() {
                switchInputType(mBinding.etPwd)
            }
        }
    }

    private fun switchInputType(editText: CustomFontEditText) {
        val inputType: Int = editText.inputType
        if (inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            // 切换到明文
            editText.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
            TextViewDrawableUtils.setDrawableRight(
                mContext,
                editText,
                cn.yanhu.commonres.R.drawable.svg_eye_open
            )
        } else {
            // 切换回密文
            editText.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
            TextViewDrawableUtils.setDrawableRight(
                mContext,
                editText,
                cn.yanhu.commonres.R.drawable.svg_eye_close
            )
        }
        editText.setSelection(editText.text.toString().length)
    }


    @SuppressLint("SetTextI18n")
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.loginLivedata.observe(this){ it ->
            parseState(it,{
                LoginResultManager.loginSuccess(mContext,it!!)
            },{
                if (it.code == ErrorCode.CODE_VERIFY_LOGIN){
                    VerifyCodeActivity.lunch(mContext,mBinding.etPhone.text.toString().trim())
                    finish()
                }
            })
        }
    }


    private fun startPhoneLogin(){
        mViewModel.login(LoginResultManager.SOURCE_PWD)
    }

    private fun checkInputListener() {
        mBinding.etPhone.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                val code = mBinding.etPwd.text.toString()
                setBtnEnable(s, code )
            }
        })

        mBinding.etPwd.addTextChangedListener(object : SimpleTextWatcher() {
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


    companion object {
        fun lunch(context: FragmentActivity, phone: String = "") {
            val intent = Intent(context, PwdLoginActivity::class.java)
            intent.putExtra(IntentKeyConfig.DATA, phone)
            context.startActivity(intent)
        }
    }
}