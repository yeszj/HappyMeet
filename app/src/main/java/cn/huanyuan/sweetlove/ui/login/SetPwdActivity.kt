package cn.huanyuan.sweetlove.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityPwdLoginBinding
import cn.huanyuan.sweetlove.databinding.ActivitySetPwdBinding
import cn.huanyuan.sweetlove.databinding.ActivityVerifyCodeBinding
import cn.huanyuan.sweetlove.func.manager.LoginResultManager
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.DialogUtils
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
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.RegexUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel

/**
 * @author: zhengjun
 * created: 2024/2/28
 * desc:
 */
class SetPwdActivity : BaseActivity<ActivitySetPwdBinding, LoginViewModel>(
    R.layout.activity_set_pwd, LoginViewModel::class.java
) {
    override fun initData() {
        setFullScreenStatusBar(true)
        setStatusBarStyle(false)
        checkInputListener()
        mBinding.viewModel = mViewModel
        val phone = intent.getStringExtra(IntentKeyConfig.DATA)
        if (!TextUtils.isEmpty(phone)) {
            mViewModel.phoneExt.set(phone)
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
            setLoginPwd()
        }
        mBinding.etPwd.clickRightDrawableListener = object : OnClickRightDrawableListener {
            override fun clickRightDrawable() {
                switchInputType(mBinding.etPwd)
            }
        }
    }

    private fun sendVerifyCode() {
        mViewModel.sendVerifyCode(2)
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

    private fun switchInputType(editText: CustomFontEditText) {
        val inputType: Int = editText.inputType
        if (inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            // 切换到明文
            editText.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
            TextViewDrawableUtils.setDrawableRight(
                mContext, editText, cn.yanhu.commonres.R.drawable.svg_eye_open
            )
        } else {
            // 切换回密文
            editText.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
            TextViewDrawableUtils.setDrawableRight(
                mContext, editText, cn.yanhu.commonres.R.drawable.svg_eye_close
            )
        }
        editText.setSelection(editText.text.toString().length)
    }


    @SuppressLint("SetTextI18n")
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.codeLivedata.observe(this) { it ->
            parseState(it, {
                showToast(it)
                startCodeTimeDown()
            })
        }
        mViewModel.setPwdLivedata.observe(this){
            DialogUtils.dismissLoading()
            parseState(it,{
                showToast("设置成功")
                finish()
            })
        }
    }

    private fun setLoginPwd() {
        DialogUtils.showLoading()
        mViewModel.setPassword()
    }

    private fun checkInputListener() {
        mBinding.etPhone.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                setBtnEnable()
            }
        })

        mBinding.etPwd.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                setBtnEnable()
            }
        })

        mBinding.etCode.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                setBtnEnable()
            }
        })
    }

    private fun setBtnEnable() {
        val phone = mBinding.etPhone.text.toString().trim()
        val code = mBinding.etCode.text.toString().trim()
        val pwd = mBinding.etPwd.text.toString().trim()
        if (!TextUtils.isEmpty(phone) && RegexUtils.isMobileSimple(phone) && !TextUtils.isEmpty(
                code
            ) && code.length >= 4 && !TextUtils.isEmpty(pwd) && pwd.length >= 6
        ) {
            mBinding.btnNext.isEnabled = true
            mBinding.btnNext.alpha = 1.0f
        } else {
            mBinding.btnNext.isEnabled = false
            mBinding.btnNext.alpha = 0.3f
        }
    }

    override fun exactDestroy() {
        super.exactDestroy()
        timeDownScope?.cancel()
    }


    companion object {
        fun lunch(context: FragmentActivity, phone: String = "") {
            val intent = Intent(context, SetPwdActivity::class.java)
            intent.putExtra(IntentKeyConfig.DATA, phone)
            context.startActivity(intent)
        }
    }
}