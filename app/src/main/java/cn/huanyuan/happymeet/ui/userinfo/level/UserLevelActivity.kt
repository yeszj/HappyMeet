package cn.huanyuan.happymeet.ui.userinfo.level

import android.content.Context
import android.content.Intent
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.ActivityUserLevelBinding
import cn.huanyuan.happymeet.ui.userinfo.UserViewModel
import cn.huanyuan.happymeet.ui.userinfo.adapter.UserLevelHeadAdapter
import cn.huanyuan.happymeet.ui.userinfo.adapter.UserLevelPrivilegeAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.QuickAdapterHelper

/**
 * @author: zhengjun
 * created: 2024/3/18
 * desc:
 */
class UserLevelActivity : BaseActivity<ActivityUserLevelBinding, UserViewModel>(
    R.layout.activity_user_level,
    UserViewModel::class.java
) {
    private lateinit var helper: QuickAdapterHelper
    private val headAdapter by lazy { UserLevelHeadAdapter() }
    private val privilegeAdapter by lazy { UserLevelPrivilegeAdapter() }
    override fun initData() {
        setFullScreenStatusBar()
        helper = QuickAdapterHelper.Builder(privilegeAdapter)
            .build()
        mBinding.recyclerView.itemAnimator?.changeDuration = 0
        mBinding.recyclerView.adapter = helper.adapter
        helper.addBeforeAdapter(0, headAdapter.apply {
        })
        requestData()
    }

    override fun initRefresh() {
        super.initRefresh()
        RefreshManager.getInstance()
            .initRefresh(mContext, false, mBinding.refreshLayout, object : IRefreshCallBack {
                override fun onRefresh() {
                    requestData()
                    endRefreshing(mBinding.refreshLayout)
                }
            })
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getUserLevelInfo()
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.userLevelObservable.observe(this) { it ->
            parseState(it, {
                headAdapter.item = it
                privilegeAdapter.submitList(it.privilegeList)
            })
        }
    }

    companion object {
        fun lunch(mContext: Context) {
            mContext.startActivity(Intent(mContext,UserLevelActivity::class.java))
        }
    }
}