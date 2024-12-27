package cn.yanhu.agora.ui.statics

import cn.yanhu.agora.databinding.ActivityLiveStatisticsTotalBinding
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.statistic.LiveStatisticsTotalAdapter
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.ext.parseState

/**
 * @author: zhengjun
 * created: 2024/12/23
 * desc:
 */
class LiveStatisticsTotalActivity :
    BaseActivity<ActivityLiveStatisticsTotalBinding, LiveStatisticsViewModel>(
        R.layout.activity_live_statistics_total, LiveStatisticsViewModel::class.java
    ) {
    private val liveTotalAdapter by lazy { LiveStatisticsTotalAdapter() }
    private val inviteTotalAdapter by lazy { LiveStatisticsTotalAdapter() }

    override fun initData() {
        setFullScreenStatusBar()
        setStatusBarStyle(false)
        mBinding.rvLive.adapter = liveTotalAdapter
        mBinding.rvInviteTotal.adapter = inviteTotalAdapter
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getLiveStatisticInfo()
    }

    override fun initRefresh(){
        RefreshManager.getInstance()
            .initRefresh(mContext, false, mBinding.refresh, object : IRefreshCallBack {
                override fun onRefresh() {
                    requestData()
                }

                override fun onLoadMore() {
                }
            })
    }

    override fun initListener() {
        super.initListener()
        mBinding.bgLiveRoom.setOnSingleClickListener {
            MyLiveRecordActivity.lunch(mContext)
        }
        mBinding.bgMyInvite.setOnSingleClickListener {
            RouteIntent.lunchToMyInviteRecord()
        }
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.liveStatisticLivedata.observe(this){ it ->
            parseState(it,{
                mBinding.userinfo = it.userInfo
                mBinding.executePendingBindings()
                liveTotalAdapter.submitList(it.myData)
                inviteTotalAdapter.submitList(it.apprenticeData)
                setDataLoadFinish(1,0,mBinding.refresh)
            })
        }
    }
}