package cn.huanyuan.sweetlove.ui.system

import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivitySecurityCenterBinding
import cn.huanyuan.sweetlove.ui.system.adapter.SecurityMenuAdapter
import cn.yanhu.commonres.bean.MineMenuBean
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.router.PageIntentUtil

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
    override fun initData() {
        setFullScreenStatusBar()
        setStatusBarStyle(false)
        mBinding.apply {
            rvMenu.adapter = securityMenuAdapter
            securityMenuAdapter.setOnItemClickListener { _, _, position ->
                val item = securityMenuAdapter.getItem(position) ?: return@setOnItemClickListener
                PageIntentUtil.url2Page(mContext, item.url)
            }
        }
        val list = mutableListOf<MineMenuBean>()
        list.add(MineMenuBean(1,"https://happymeet-new.oss-cn-hangzhou.aliyuncs.com/image/securityCenter/icon_log_off.png","注销账号",WebUrlManager.LOG_OFF,"1"))

        securityMenuAdapter.submitList(list)
    }

}