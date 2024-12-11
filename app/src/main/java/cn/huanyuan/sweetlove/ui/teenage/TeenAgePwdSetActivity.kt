package cn.huanyuan.sweetlove.ui.teenage

import android.content.Intent
import android.view.KeyEvent
import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityTeenAgePwdSetBinding
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.zj.netrequest.ext.parseState

/**
 * @author: zhengjun
 * created: 2024/12/11
 * desc:
 */
class TeenAgePwdSetActivity : BaseActivity<ActivityTeenAgePwdSetBinding, TeenAgeViewModel>(
    R.layout.activity_teen_age_pwd_set,
    TeenAgeViewModel::class.java
) {

    private var type: Int = TYPE_SET
    private var isFirstInput = true
    private var firstPassword = ""
    private var secondPassword = ""
    private var closeInputPassword = ""
    override fun initData() {
        setStatusBarStyle(false)
        type = intent.getIntExtra(IntentKeyConfig.TYPE, TYPE_SET)
        bindTips()
    }

    override fun initListener() {
        super.initListener()
        mBinding.titleBar.setTitleButtonOnClickListener(object :
            TitleBar.TitleButtonOnClickListener {
            override fun leftButtonOnClick(v: View?) {
                clickBack()
            }

            override fun rightButtonOnClick(v: View?) {
            }

        })
        mBinding.pwdEdit.setOnInputListener {
            mBinding.tvSure.alpha = 1f
            mBinding.tvSure.isEnabled = true
            if (type == TYPE_SET) {
                if (isFirstInput) {
                    firstPassword = it
                    if (isAutoNextStep){
                        toSecondInput()
                    }else{
                        isAutoNextStep = true
                    }
                } else {
                    secondPassword = it
                }
            } else {
                closeInputPassword = it
            }

        }
        mBinding.tvSure.setOnSingleClickListener {
            if (type == TYPE_SET) {
                if (isFirstInput) {
                    toSecondInput()
                } else {
                    if (firstPassword==secondPassword){
                        openTeenAgeMode()
                    }else{
                        showToast("两次密码输入不一致，请重新输入")
                    }
                }
            } else {
                closeTeenAgeMode()
            }
        }
    }

    private fun toSecondInput() {
        showToast("请再次输入密码")
        isFirstInput = false
        mBinding.pwdEdit.removeAll()
        mBinding.tvSure.alpha = 0.5f
        mBinding.tvSure.isEnabled = false
        bindTips()
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.openTeenAgeModeResult.observe(this){
            parseState(it,{
                setSuccess(true)
                showToast("已开启青少年模式")
            })
        }
        mViewModel.closeTeenAgeModeResult.observe(this){
            parseState(it,{
                setSuccess(false)
                showToast("已关闭青少年模式")
            })
        }
    }

    private fun openTeenAgeMode(){
        mViewModel.openTeenAgeMode(firstPassword)
    }

    private fun closeTeenAgeMode(){
        mViewModel.closeTeenAgeMode(closeInputPassword)
    }

    private var isAutoNextStep = true
    private fun clickBack() {
        if (type == TYPE_SET) {
            if (isFirstInput) {
                finish()
            } else {
                isAutoNextStep = false
                isFirstInput = true
                bindTips()
                mBinding.pwdEdit.showAllCode(firstPassword)
                mBinding.tvSure.alpha = 1f
                mBinding.tvSure.isEnabled = true
            }
        } else {
            finish()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            clickBack()
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    private fun setSuccess(isOpen: Boolean) {
        val intent = Intent()
        intent.putExtra(IntentKeyConfig.DATA, isOpen)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun bindTips() {
        mBinding.apply {
            if (type == TYPE_SET) {
                if (isFirstInput) {
                    tvTitle.text = "请设置密码"
                } else {
                    tvTitle.text = "请确认密码"
                }
                tvTips.text = "请监护人设置密码"
            } else {
                tvTitle.text = "请输入密码"
                tvTips.text = "输入密码以关闭青少年模式"
            }
        }
    }

    companion object {
        const val TYPE_SET = 1
        const val TYPE_INPUT = 2
    }


}