package cn.huanyuan.happymeet.ui.userinfo.auth

import android.text.TextUtils
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.ActivityAuthCenterBinding
import cn.huanyuan.happymeet.ui.userinfo.UserViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.zj.netrequest.ext.parseState
import com.pcl.sdklib.sdk.wechat.WxAuthUtils

/**
 * @author: zhengjun
 * created: 2024/3/11
 * desc:
 */
class AuthCenterActivity : BaseActivity<ActivityAuthCenterBinding, UserViewModel>(
    R.layout.activity_auth_center,
    UserViewModel::class.java
) {
    override fun initData() {
        setFullScreenStatusBar()
        setStatusBarStyle(false)
    }

    override fun initListener() {
        super.initListener()
        mBinding.bgRealName.setOnSingleClickListener { RealNameActivity.lunch(mContext) }
        mBinding.bgAliBind.setOnSingleClickListener {
            val realName = mBinding.authInfo?.realName
            if (TextUtils.isEmpty(realName)){
                showToast("请先完成实名认证")
            }else{
                AliPayAccountBindActivity.lunch(mContext,realName!!)
            }
        }
        mBinding.bgWxBind.setOnSingleClickListener {
            val realName = mBinding.authInfo?.realName
            if (TextUtils.isEmpty(realName)){
                showToast("请先完成实名认证")
            }else{
                WxAuthUtils.weChatAuth(mContext)
            }
        }
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getAuthCenterInfo()
    }

    override fun onResume() {
        super.onResume()
        requestData()
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.authCenterInfoObservable.observe(this){ it ->
            parseState(it,{
                mBinding.authInfo = it
            })
        }
    }
}