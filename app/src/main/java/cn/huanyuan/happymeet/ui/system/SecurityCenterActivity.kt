package cn.huanyuan.happymeet.ui.system

import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.bean.SecurityInfo
import cn.huanyuan.happymeet.databinding.ActivitySecurityCenterBinding
import cn.huanyuan.happymeet.ui.system.adapter.SecurityMenuAdapter
import cn.huanyuan.happymeet.ui.system.adapter.SecurityProhibitBannerAdapter
import cn.yanhu.commonres.router.PageIntentUtil
import cn.zj.netrequest.ext.parseState
import com.bumptech.glide.Glide

/**
 * @author: zhengjun
 * created: 2023/12/20
 * desc:
 */
class SecurityCenterActivity :
    cn.yanhu.baselib.base.BaseActivity<ActivitySecurityCenterBinding, SystemViewModel>(
        R.layout.activity_security_center,
        SystemViewModel::class.java
    ) {
    private val securityMenuAdapter by lazy { SecurityMenuAdapter() }
    private var securityInfo: SecurityInfo? = null
    override fun initData() {
        setFullScreenStatusBar()
        mBinding.apply {
            rvMenu.adapter = securityMenuAdapter
            securityMenuAdapter.setOnItemClickListener { _, _, position ->
                val item = securityMenuAdapter.getItem(position) ?: return@setOnItemClickListener
                PageIntentUtil.url2Page(mContext, item.url)
            }
        }
        getSecurityInfo()
    }

    override fun initListener() {
        mBinding.ivBanner.setOnClickListener {
            if (securityInfo != null) {
                PageIntentUtil.url2Page(mContext, securityInfo!!.safetyReminderUrl)
            }
        }
    }

    private fun getSecurityInfo() {
        mViewModel.getSecurityInfo()
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.securityInfoObservable.observe(this) { it ->
            parseState(it, {
                securityInfo = it
                securityMenuAdapter.submitList(it.serviceConfigs)
                Glide.with(mContext).load(it.safetyReminderImg).into(mBinding.ivBanner)
                val violationUserIds = it.violationUserIds
                if (violationUserIds != null && violationUserIds.size > 0) {
                    val chunked = violationUserIds.chunked(6)
                    bindProhibitInfo(chunked)
                }
            })
        }
    }

    private fun bindProhibitInfo(data: List<List<String>>) {
        val adapter = SecurityProhibitBannerAdapter(mContext, data)
        mBinding.bannerProhibit.addBannerLifecycleObserver(mContext)
            .setAdapter(adapter)
            .isAutoLoop(true)
            .start()
    }
}