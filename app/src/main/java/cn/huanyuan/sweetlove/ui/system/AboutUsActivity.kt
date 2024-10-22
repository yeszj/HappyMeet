package cn.huanyuan.sweetlove.ui.system

import android.annotation.SuppressLint
import cn.huanyuan.sweetlove.databinding.ActivityAboutUsBinding
import cn.yanhu.baselib.base.BaseActivity
import cn.huanyuan.sweetlove.R
import cn.yanhu.baselib.utils.CommonUtils
import com.blankj.utilcode.util.AppUtils
import cn.huanyuan.sweetlove.BuildConfig
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.router.PageIntentUtil

/**
 * @author: zhengjun
 * created: 2024/10/11
 * desc:
 */
class AboutUsActivity : BaseActivity<ActivityAboutUsBinding, SystemViewModel>(
    R.layout.activity_about_us,
    SystemViewModel::class.java
) {
    @SuppressLint("SetTextI18n")
    override fun initData() {
        setStatusBarStyle(false)
        mBinding.tvVersion.text =
            "${CommonUtils.getString(R.string.app_name)}v${AppUtils.getAppVersionName()}-${AppUtils.getAppVersionCode()}-${BuildConfig.FLAVOR}"
        mBinding.tvAgreement.setOnSingleClickListener {
            PageIntentUtil.url2Page(mContext, WebUrlManager.PRIVACY_AGREEMENT)
        }
    }
}