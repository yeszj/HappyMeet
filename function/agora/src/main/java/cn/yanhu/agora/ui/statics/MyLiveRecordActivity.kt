package cn.yanhu.agora.ui.statics

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import cn.yanhu.agora.databinding.ActivityMyLiveRecordBinding
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.statistic.LiveRecordAdapter
import cn.yanhu.agora.bean.LiveRecordResponse
import cn.yanhu.baselib.func.sticky.StickyHeadContainer
import cn.yanhu.baselib.func.sticky.StickyItemDecoration
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.bean.FilterInfo
import cn.yanhu.commonres.pop.CommonTypeFilterPop
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/12/23
 * desc:
 */
class MyLiveRecordActivity : BaseActivity<ActivityMyLiveRecordBinding, LiveStatisticsViewModel>(
    R.layout.activity_my_live_record, LiveStatisticsViewModel::class.java
) {
    private var filterList: MutableList<FilterInfo> = mutableListOf()
    var filterId: Int = 0
    var page: Int = 1
    private val recordAdapter by lazy { LiveRecordAdapter() }
    override fun initData() {
        setFullScreenStatusBar()
        setStatusBarStyle(false)
        val emptyView = getEmptyView()
        emptyView.setFootText("暂无开播记录")
        recordAdapter.stateView = emptyView
        mBinding.recyclerView.adapter = recordAdapter
        mBinding.recyclerView.addItemDecoration(
            StickyItemDecoration(
                mBinding.stickContainer, LiveRecordAdapter.TYPE_STICKY_HEAD
            )
        )
        mBinding.stickContainer.setDataCallback(object : StickyHeadContainer.DataCallback {
            override fun onDataChange(pos: Int) {
                val item = recordAdapter.getItem(pos)
                mBinding.stickHead.recordInfo = item
            }
        })
        recordAdapter.setOnItemClickListener(object : BaseQuickAdapter.OnItemClickListener<LiveRecordResponse.RecordInfo>{
            override fun onClick(
                adapter: BaseQuickAdapter<LiveRecordResponse.RecordInfo, *>,
                view: View,
                position: Int
            ) {
                val item = recordAdapter.getItem(position)?:return
                if (TextUtils.isEmpty(item.date)){
                    LiveIncomeDetailActivity.lunch(mContext,item.roomId)
                }
            }

        })
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getMyLiveRecord(filterId, page)
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

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.liveRecordLivedata.observe(this) { it ->
            parseState(it, { it ->
                filterList = it.filterList
                val list = mutableListOf<LiveRecordResponse.RecordInfo>()
                val recordList = it.dataList
                recordList.forEach {
                    list.add(it)
                    list.addAll(it.list)
                }
                if (page == 1) {
                    recordAdapter.isStateViewEnable = list.size <= 0
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


    override fun initListener() {
        super.initListener()
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
            filterPop = CommonTypeFilterPop.showPop(mContext,
                filterList,
                object : CommonTypeFilterPop.OnFilterListener {
                    override fun onSelectFilter(filterInfo: FilterInfo?) {
                        filterInfo?.apply {
                            filterId = filterInfo.id
                            mBinding.titleBar.setTitleRightText(filterInfo.name)
                            page = 1
                            requestData()
                        }

                    }
                })
        }
    }

    companion object{
        fun lunch(context: Context){
            context.startActivity(Intent(context,MyLiveRecordActivity::class.java))
        }
    }
}