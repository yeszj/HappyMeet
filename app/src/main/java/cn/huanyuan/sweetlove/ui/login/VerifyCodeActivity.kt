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
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.parseState
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.RegexUtils
import com.pcl.sdklib.sdk.wechat.WxAuthUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel


/**
 * @author: zhengjun
 * created: 2024/2/28
 * desc:
 */
class VerifyCodeActivity : BaseActivity<ActivityVerifyCodeBinding, LoginViewModel>(
    R.layout.activity_verify_code,
    LoginViewModel::class.java
) {
    var loginMap  = hashMapOf<String, String>()
    private var isWxLoginBindPhone = false
    override fun initData() {
        setFullScreenStatusBar(true)
        setStatusBarStyle(false)
        checkInputListener()
        mBinding.viewModel = mViewModel
        val phone = intent.getStringExtra(IntentKeyConfig.DATA)
        if (intent.hasExtra(IntentKeyConfig.MAP_INFO)){
            mBinding.tvPhone.text  = "绑定手机号"
            isWxLoginBindPhone = true
            loginMap = intent
                .getSerializableExtra(IntentKeyConfig.MAP_INFO) as HashMap<String, String>
        }
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
            startLogin()
        }
        mBinding.connectedService.setOnSingleClickListener {
            ApplicationProxy.instance.askCustomer()
        }
    }

    private fun sendVerifyCode(){
        mViewModel.sendVerifyCode()
    }

    @SuppressLint("SetTextI18n")
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.codeLivedata.observe(this){ it ->
            parseState(it,{
                if (it=="白名单用户"){
                    mBinding.etCode.setText("2024")
                    startLogin()
                }else{
                    showToast(it)
                    KeyboardUtils.showSoftInput(mBinding.etCode)
                }
                startCodeTimeDown()
            })
        }
        mViewModel.loginLivedata.observe(this){ it ->
            parseState(it,{
                LoginResultManager.loginSuccess(mContext,it!!)
            })
        }
    }

    override fun exactDestroy() {
        super.exactDestroy()
        timeDownScope?.cancel()
    }

    private fun startLogin(){
        if (isWxLoginBindPhone){
            mViewModel.wxLogin(loginMap)
        }else{
            mViewModel.login(LoginResultManager.SOURCE_EMS)
        }
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
        fun lunch(context: FragmentActivity, phone: String = "",map: HashMap<String,String>?=null) {
            val intent = Intent(context, VerifyCodeActivity::class.java)
            intent.putExtra(IntentKeyConfig.DATA, phone)
            if (map!=null){
                intent.putExtra(IntentKeyConfig.MAP_INFO,map)
            }
            context.startActivity(intent)
        }
    }
}