package cn.huanyuan.sweetlove.ui.teenage

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityTeenAgeModeBinding
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ActivityUtils

/**
 * @author: zhengjun
 * created: 2024/12/11
 * desc:
 */
class TeenAgeModeActivity : BaseActivity<ActivityTeenAgeModeBinding, TeenAgeViewModel>(
    R.layout.activity_teen_age_mode,
    TeenAgeViewModel::class.java
) {
    private var isOpen = false
    override fun initData() {
        setFullScreenStatusBar()
        setStatusBarStyle(false)
        if (intent.hasExtra(IntentKeyConfig.DATA)){
            isOpen = intent.getBooleanExtra(IntentKeyConfig.DATA, false)
            setModeStatus()
        }else{
            mViewModel.checkIsOpenJuvenileMode(object : OnRequestResultListener<Boolean>{
                override fun onSuccess(data: BaseBean<Boolean>) {
                    isOpen = data.data == true
                    setModeStatus()
                }
            })
        }

    }

    override fun initListener() {
        super.initListener()
        mBinding.tvOperate.setOnSingleClickListener {
            val intent = Intent(mContext,TeenAgePwdSetActivity::class.java)
            if (isOpen){
                intent.putExtra(IntentKeyConfig.TYPE,TeenAgePwdSetActivity.TYPE_INPUT)
            }else{
                intent.putExtra(IntentKeyConfig.TYPE,TeenAgePwdSetActivity.TYPE_SET)
            }
            pwdResult.launch(intent)
        }
        mBinding.titleBar.setTitleButtonOnClickListener(object :
            TitleBar.TitleButtonOnClickListener {
            override fun leftButtonOnClick(v: View?) {
                if (isOpen) {
                    ActivityUtils.finishAllActivities()
                } else {
                    finish()
                }
            }

            override fun rightButtonOnClick(v: View?) {
            }
        })
    }

    private val pwdResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            isOpen = it.data?.getBooleanExtra(IntentKeyConfig.DATA, false) == true
            setModeStatus()
            if (!isOpen) {
                finish()
            }
        }
    }

    private fun setModeStatus() {
        mBinding.isOpen = isOpen
        mBinding.titleBar.setShowBack(if (isOpen) View.INVISIBLE else View.VISIBLE)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isOpen) {
                ActivityUtils.finishAllActivities()
                true
            } else {
                super.onKeyDown(keyCode, event)
            }
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    companion object {
        fun lunch(context: Context, isOpen: Boolean) {
            val intent = Intent(context, TeenAgeModeActivity::class.java)
            intent.putExtra(IntentKeyConfig.DATA, isOpen)
            context.startActivity(intent)
        }
    }
}