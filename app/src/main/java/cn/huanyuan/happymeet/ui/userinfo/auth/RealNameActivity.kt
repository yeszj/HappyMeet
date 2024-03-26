package cn.huanyuan.happymeet.ui.userinfo.auth

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.ActivityRealNameBinding
import cn.huanyuan.happymeet.ui.userinfo.UserViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.manager.RequestCodeManager
import com.blankj.utilcode.util.RegexUtils

/**
 * @author: zhengjun
 * created: 2024/3/11
 * desc:
 */
class RealNameActivity : BaseActivity<ActivityRealNameBinding, UserViewModel>(
    R.layout.activity_real_name,
    UserViewModel::class.java
) {
    override fun initData() {
        setStatusBarStyle(false)
    }

    override fun initListener() {
        super.initListener()
        mBinding.btnAuth.setOnSingleClickListener {
            if (checkValue()){
                showToast("认证成功")
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun checkValue():Boolean {
        val name = mBinding.etRealName.text.toString().trim()
        if (TextUtils.isEmpty(name)) {
            showToast("请填写真实姓名")
            return false
        }
        val idCard = mBinding.etIdCard.text.toString().trim()
        if (!RegexUtils.isIDCard18(idCard)) {
            showToast("请填写正确的身份证")
            return false
        }
        return true
    }

    companion object{

        fun lunch(context:Activity){

            context.startActivityForResult(Intent(context,RealNameActivity::class.java),
                RequestCodeManager.REQUEST_CODE_REAL_NAME)
        }
    }
}