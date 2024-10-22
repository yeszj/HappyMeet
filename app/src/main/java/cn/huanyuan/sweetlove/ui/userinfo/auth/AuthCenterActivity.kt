package cn.huanyuan.sweetlove.ui.userinfo.auth

import android.text.TextUtils
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityAuthCenterBinding
import cn.huanyuan.sweetlove.ui.userinfo.UserViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.zj.netrequest.ext.parseState
import com.pcl.sdklib.listener.OnAuthResultListener
import com.pcl.sdklib.sdk.alipay.AliAuthUtils
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
        mBinding.bgRealName.setOnSingleClickListener {
            val realName = mBinding.authInfo?.realName
            if (TextUtils.isEmpty(realName)) {
                RealNameActivity.lunch(mContext)
            } else {
                showToast("已认证")
            }
        }
        mBinding.bgAliBind.setOnSingleClickListener {
            val realName = mBinding.authInfo?.realName
            if (TextUtils.isEmpty(realName)) {
                showToast("请先完成实名认证")
            } else {
                AliAuthUtils.aliAuth(mContext, object : OnAuthResultListener {
                    override fun onAuthSuccess() {
                        requestData()
                    }
                })
            }
        }
        mBinding.bgWxBind.setOnSingleClickListener {
            val realName = mBinding.authInfo?.realName
            if (TextUtils.isEmpty(realName)) {
                showToast("请先完成实名认证")
            } else {
                WxAuthUtils.weChatAuth(mContext)
            }
        }
        WxAuthUtils.registerAuthResultListener(mContext, object : OnAuthResultListener {
            override fun onAuthSuccess() {
                requestData()
            }
        })
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
        mViewModel.authCenterInfoObservable.observe(this) { it ->
            parseState(it, {
                mBinding.authInfo = it
            })
        }
    }
}