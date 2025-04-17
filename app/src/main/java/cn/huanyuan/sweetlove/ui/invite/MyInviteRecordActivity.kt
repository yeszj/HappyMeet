package cn.huanyuan.sweetlove.ui.invite

import android.content.Context
import android.content.Intent
import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.ActivityMyInviteRecordBinding
import cn.huanyuan.sweetlove.ui.invite.adapter.InviteRecordAdapter
import cn.huanyuan.sweetlove.ui.invite.adapter.InviteRecordHeadAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.NoMoreDataFootView
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.commonres.bean.FilterInfo
import cn.yanhu.commonres.pop.CommonTypeFilterPop
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.router.RouterPath
import cn.zj.netrequest.ext.parseState
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter4.QuickAdapterHelper

/**
 * @author: zhengjun
 * created: 2024/3/14
 * desc:我的邀请记录
 */
@Route(path = RouterPath.ROUTER_MY_INVITE_RECORD_PAGE)
class MyInviteRecordActivity : BaseActivity<ActivityMyInviteRecordBinding, InviteViewModel>(
    R.layout.activity_my_invite_record,
    InviteViewModel::class.java
) {
    private val recordHeadAdapter by lazy { InviteRecordHeadAdapter(mContext) }
    private val recordAdapter by lazy { InviteRecordAdapter() }
    private lateinit var helper: QuickAdapterHelper
    private var page = 1
    private var filterId: String = ""
    private var filterTimeId: String = ""

    override fun initData() {
        setFullScreenStatusBar()
        setStatusBarStyle(false)
        helper = QuickAdapterHelper.Builder(recordAdapter)
            .build()
        mBinding.rvRecord.adapter = helper.adapter
        helper.addBeforeAdapter(recordHeadAdapter)
        requestData()
    }

    override fun initListener() {
        super.initListener()
        recordAdapter.setOnItemClickListener { adapter, _, position ->
            val item = adapter.getItem(position)
            RouteIntent.lunchPersonHomePage(item?.userId)
        }
        recordHeadAdapter.addOnItemChildClickListener(R.id.tv_filter
        ) { _, _, _ -> showFilterPop() }
        recordHeadAdapter.addOnItemChildClickListener(R.id.tv_filterTime
        ) { _, _, _ -> showFilterTimePop() }
    }


    private var filterTimePop: CommonTypeFilterPop? = null
    private var filterTimeList = mutableListOf<FilterInfo>()
    private fun showFilterTimePop() {
        if (filterTimePop != null) {
            filterTimePop?.show()
        } else {
            filterTimePop = CommonTypeFilterPop.showPop(
                mContext,
                filterTimeList,
                object : CommonTypeFilterPop.OnFilterListener {
                    override fun onSelectFilter(filterInfo: FilterInfo?) {
                        filterTimeId = filterInfo?.id.toString()
                        val filterName = filterInfo?.name.toString()
                        recordHeadAdapter.filterTimeName = filterName
                        recordHeadAdapter.notifyItemChanged(0,true)
                        page = 1
                        requestData()
                    }
                })
        }
    }

    private var filterPop: CommonTypeFilterPop? = null
    private var filterList = mutableListOf<FilterInfo>()
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
                        val filterName = filterInfo?.name.toString()
                        recordHeadAdapter.filterName = filterName
                        recordAdapter.filterName = filterName
                        recordHeadAdapter.notifyItemChanged(0,true)
                        page = 1
                        requestData()
                    }
                })
        }
    }

    override fun initRefresh() {
        super.initRefresh()
        RefreshManager.getInstance()
            .initRefresh(mContext, true, mBinding.refreshLayout, object : IRefreshCallBack {
                override fun onRefresh() {
                    page = 1
                    requestData()
                }

                override fun onLoadMore() {
                    page++
                    requestData()
                }
            })
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getMyInviteUser(page,filterId,filterTimeId)
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.myInviteInfoObservable.observe(this) { it ->
            parseState(it, {
                val list = it.list
                filterList = it.filterList
                filterTimeList = it.inviteFilterList
                if (page == 1) {
                    if (list.size<=0){
                        mBinding.emptyView.visibility = View.VISIBLE
                        mBinding.emptyView.footViewState(NoMoreDataFootView.FOOT_NO_DATA)
                    }else{
                        mBinding.emptyView.visibility = View.GONE
                    }
                    recordHeadAdapter.item = it
                    recordAdapter.submitList(list)
                } else {
                    recordAdapter.addAll(list)
                }
                setDataLoadFinish(page, list.size, mBinding.refreshLayout)
            }, {
                endLoad(page, mBinding.refreshLayout)
            })
        }
    }

    companion object{
        fun lunch(context: Context){
            context.startActivity(Intent(context, MyInviteRecordActivity::class.java))
        }
    }
}