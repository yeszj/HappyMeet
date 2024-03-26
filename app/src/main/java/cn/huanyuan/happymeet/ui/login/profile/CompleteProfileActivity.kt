package cn.huanyuan.happymeet.ui.login.profile

import android.content.Intent
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.FragmentActivity
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.ActivityCompleteProfileBinding
import cn.huanyuan.happymeet.ui.invite.BindInviteCodeActivity
import cn.huanyuan.happymeet.ui.login.LoginActivity
import cn.huanyuan.happymeet.ui.login.LoginViewModel
import cn.huanyuan.happymeet.ui.main.MainActivity
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.LoginSuccessInfo
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.zj.netrequest.ext.parseState
import com.blankj.utilcode.util.ActivityUtils

/**
 * @author: zhengjun
 * created: 2024/2/29
 * desc:
 */
class CompleteProfileActivity : BaseActivity<ActivityCompleteProfileBinding, LoginViewModel>(
    R.layout.activity_complete_profile,
    LoginViewModel::class.java
) {
    override fun initData() {
        setFullScreenStatusBar(true)
        setStatusBarStyle(false)
        showSexAndAgeFrg()
        mViewModel.loginSuccessInfo.value = intent.getSerializableExtra(IntentKeyConfig.DATA) as LoginSuccessInfo
        mViewModel.personInfo.value = BaseUserInfo()
    }

    override fun initListener() {
        super.initListener()
        mBinding.titleBar.setTitleButtonOnClickListener(object : TitleBar.TitleButtonOnClickListener{
            override fun leftButtonOnClick(v: View?) {
                clickBack()
            }
            override fun rightButtonOnClick(v: View?) {
            }

        })
        mBinding.btnNext.setOnSingleClickListener { toNextStep() }
    }

    private var sexAndAgeFrg:EditSexAndAgeFrg?=null
    private var avatarAndNameFrg:EditAvatarAndNameFrg?=null
    private fun showSexAndAgeFrg(){
        mBinding.step = 1
        var isBack = true
        if (sexAndAgeFrg == null) {
            isBack = false
            sexAndAgeFrg = EditSexAndAgeFrg.newsInstance()
        }
        replaceFragment(avatarAndNameFrg, sexAndAgeFrg!!, isBack)
    }

    private fun showAvatarAndNameFrg() {
        if (avatarAndNameFrg == null) {
            avatarAndNameFrg = EditAvatarAndNameFrg.newsInstance()
        }
        if (mBinding.step == 1) {
            replaceFragment(sexAndAgeFrg, avatarAndNameFrg!!, false)
        } else {
            replaceFragment(editLocationFrg, avatarAndNameFrg!!, true)
        }
        mBinding.step = 2

    }

    private fun clickBack() {
        when (mBinding.step) {
            1 -> {
                if (mViewModel.personInfo.value?.gender == 0) {
                    showToast("请选择性别～")
                }else{
                    commitProfileInfo()
                }
            }

            2 -> {
                showSexAndAgeFrg()
            }

            else -> {
                showAvatarAndNameFrg()
            }
        }
    }
    private fun toNextStep() {
        when (mBinding.step) {
            1 -> {
                if (mViewModel.personInfo.value?.gender == 0) {
                    showToast("请选择性别～")
                } else {
                    showAvatarAndNameFrg()
                }
            }

            2 -> {
                if (TextUtils.isEmpty(mViewModel.personInfo.value?.portrait)) {
                    showToast("请上传头像")
                } else {
                    showLocationFrg()
                }
            }

            else -> {
                commitProfileInfo()
            }
        }
    }

    private var editLocationFrg:EditLocationFrg?=null
    private fun showLocationFrg() {
        if (editLocationFrg == null) {
            editLocationFrg = EditLocationFrg.newsInstance()
        }
        replaceFragment(avatarAndNameFrg, editLocationFrg!!, false)
        mBinding.step = 3
    }

    private fun commitProfileInfo() {
        mViewModel.insertBasicInfo()
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.saveBasicInfoLivedata.observe(this){
            parseState(it,{
                MainActivity.lunch(this, intent.extras)
                ActivityUtils.finishActivity(BindInviteCodeActivity::class.java)
                ActivityUtils.finishActivity(LoginActivity::class.java)
                finish()
            })
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            clickBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        fun lunch(context: FragmentActivity,loginSuccessInfo: LoginSuccessInfo) {
            val intent = Intent(context, CompleteProfileActivity::class.java)
            intent.putExtra(IntentKeyConfig.DATA,loginSuccessInfo)
            context.startActivity(intent)
        }
    }
}