package cn.huanyuan.sweetlove.ui.userinfo.auth

import android.content.Context
import android.content.Intent
import cn.huanyuan.sweetlove.databinding.ActivityAlipayAccountBindBinding
import cn.huanyuan.sweetlove.ui.userinfo.UserViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.huanyuan.sweetlove.R
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.config.IntentKeyConfig

/**
 * @author: zhengjun
 * created: 2024/3/11
 * desc:
 */
class AliPayAccountBindActivity : BaseActivity<ActivityAlipayAccountBindBinding, UserViewModel>(
    R.layout.activity_alipay_account_bind,
    UserViewModel::class.java
) {
    override fun initData() {
        setStatusBarStyle(false)
        val name = intent.getStringExtra(IntentKeyConfig.DATA)
        mBinding.etRealName.text = name
    }

    override fun initListener() {
        super.initListener()
        mBinding.btnAuth.setOnSingleClickListener {

        }
    }

    companion object {
        fun lunch(context: Context, name: String) {
            val intent = Intent(
                context, AliPayAccountBindActivity::class.java
            )
            intent.putExtra(IntentKeyConfig.DATA,name)
            context.startActivity(intent)

        }
    }
}