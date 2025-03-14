package cn.yanhu.agora.ui.statics

import android.view.View
import cn.yanhu.agora.databinding.ActivityLiveStatisticsTotalBinding
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.statistic.LiveStatisticsTotalAdapter
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.bean.FilterInfo
import cn.yanhu.commonres.pop.CommonTypeFilterPop
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
    private var filterId: String = "5"
    override fun initData() {
        setFullScreenStatusBar()
        setStatusBarStyle(false)
        mBinding.rvLive.adapter = liveTotalAdapter
        mBinding.rvInviteTotal.adapter = inviteTotalAdapter
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getLiveStatisticInfo(filterId)
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
        mBinding.titleBar.setTitleButtonOnClickListener(object :
            TitleBar.TitleButtonOnClickListener {
            override fun leftButtonOnClick(v: View?) {
                finish()
            }

            override fun rightButtonOnClick(v: View?) {
                showFilterPop()
            }

        })
    }


    private var filterPop: CommonTypeFilterPop? = null
    private fun showFilterPop() {
        if (filterPop != null) {
            filterPop?.show()
        } else {
            filterPop = CommonTypeFilterPop.showPop(
                mContext,
                filterList,
                object : CommonTypeFilterPop.OnFilterListener {
                    override fun onSelectFilter(filterInfo: FilterInfo?) {
                        filterId = filterInfo?.id.toString()
                        mBinding.titleBar.setTitleRightText(filterInfo?.name)
                        requestData()
                    }
                })
        }
    }

    private var filterList = mutableListOf<FilterInfo>()
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.liveStatisticLivedata.observe(this){ it ->
            parseState(it,{
                mBinding.userinfo = it.userInfo
                filterList = it.filterList
                mBinding.executePendingBindings()
                liveTotalAdapter.submitList(it.myData)
                inviteTotalAdapter.submitList(it.apprenticeData)
                setDataLoadFinish(1,0,mBinding.refresh)
            })
        }
    }
}