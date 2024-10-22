package cn.huanyuan.sweetlove.ui.invite

import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityBindInviteCodeBinding
import cn.huanyuan.sweetlove.ui.login.LoginViewModel
import cn.huanyuan.sweetlove.ui.login.profile.CompleteProfileActivity
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.LoginSuccessInfo
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.ext.parseState

/**
 * @author: zhengjun
 * created: 2024/2/28
 * desc:
 */
class BindInviteCodeActivity : BaseActivity<ActivityBindInviteCodeBinding, LoginViewModel>(
    R.layout.activity_bind_invite_code,
    LoginViewModel::class.java
) {
    private var loginSuccessInfo: LoginSuccessInfo? = null
    override fun initData() {
        //loginSuccessInfo不为空 是首次注册登录时绑定流程
        loginSuccessInfo = intent.getSerializableExtra(IntentKeyConfig.DATA) as LoginSuccessInfo?
        setFullScreenStatusBar(true)
        setStatusBarStyle(false)
        if (loginSuccessInfo == null) {
            mBinding.tvLogin.visibility = View.GONE
            mBinding.btnNext.text = "确认绑定"
        } else {
            mBinding.tvLogin.visibility = View.VISIBLE
            mBinding.btnNext.text = "下一步"
        }
    }

    override fun initListener() {
        mBinding.ivClose.setOnSingleClickListener { mBinding.etInviteCode.setText("") }
        mBinding.tvLogin.setOnSingleClickListener {
            RouteIntent.lunchLoginPage()
            finish()
        }
        mBinding.btnNext.setOnSingleClickListener { bindInviteCode() }
    }

    private fun bindInviteCode() {
        val inviteCode = mBinding.etInviteCode.text.toString()
        if (TextUtils.isEmpty(inviteCode)) {
            if (loginSuccessInfo == null) {
                showToast("请输入邀请码")
            } else {
                toCompleteInfoPage()
            }
        } else {
            mViewModel.bindInviteCode(inviteCode)
        }
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.inviteCodeLivedata.observe(this) {
            parseState(it, {
                if (loginSuccessInfo == null) {
                    showToast("绑定成功")
                    LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.BIND_CODE_SUCCESS)
                    finish()
                } else {
                    toCompleteInfoPage()
                }
            })
        }
    }

    private fun toCompleteInfoPage() {
        CompleteProfileActivity.lunch(mContext)
    }


    companion object {
        fun lunch(context: FragmentActivity, loginSuccessInfo: LoginSuccessInfo? = null) {
            val intent = Intent(context, BindInviteCodeActivity::class.java)
            intent.putExtra(IntentKeyConfig.DATA, loginSuccessInfo)
            context.startActivity(intent)
        }
    }
}