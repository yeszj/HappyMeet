package cn.huanyuan.sweetlove.ui.wallet.detail

import android.content.Context
import android.content.Intent
import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.WalletRecordInfo
import cn.huanyuan.sweetlove.databinding.ActivityWalletDetailBinding
import cn.yanhu.commonres.pop.CommonTypeFilterPop
import cn.huanyuan.sweetlove.ui.wallet.WalletViewModel
import cn.huanyuan.sweetlove.ui.wallet.adapter.WalletDetailAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.func.sticky.StickyHeadContainer
import cn.yanhu.baselib.func.sticky.StickyItemDecoration
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.bean.FilterInfo
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.zj.netrequest.ext.parseState

/**
 * @author: zhengjun
 * created: 2024/3/18
 * desc:
 */
class WalletDetailActivity : BaseActivity<ActivityWalletDetailBinding, WalletViewModel>(
    R.layout.activity_wallet_detail, WalletViewModel::class.java
) {
    private val detailAdapter by lazy { WalletDetailAdapter() }
    private var filterId: String = ""
    private var type: Int = TYPE_ROSE_DETAIL
    private var page = 1
    override fun initData() {
        setStatusBarStyle(false)
        type = intent.getIntExtra(IntentKeyConfig.TYPE, TYPE_ROSE_DETAIL)
        if (type == TYPE_COIN_DETAIL) {
            mBinding.titleBar.setLeftTitleName("金币明细")
        }
        val emptyView = getEmptyView()
        emptyView.setFootText("暂无明细")
        detailAdapter.stateView = emptyView
        mBinding.recyclerView.adapter = detailAdapter
        mBinding.recyclerView.addItemDecoration(
            StickyItemDecoration(
                mBinding.stickContainer,
                WalletDetailAdapter.TYPE_STICKY_HEAD
            )
        )
        mBinding.stickContainer.setDataCallback(object : StickyHeadContainer.DataCallback {
            override fun onDataChange(pos: Int) {
                val item = detailAdapter.getItem(pos)
                mBinding.stickHead.recordInfo = item
            }
        })
        requestData()
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
            filterPop = CommonTypeFilterPop.showPop(
                mContext,
                filterList,
                object : CommonTypeFilterPop.OnFilterListener {
                    override fun onSelectFilter(filterInfo: FilterInfo?) {
                        filterId = filterInfo?.id.toString()
                        mBinding.titleBar.setTitleRightText(filterInfo?.name)
                        page = 1
                        requestData()
                    }
                })
        }
    }

    override fun initRefresh() {
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
        mViewModel.getWalletRecord(filterId, type, page)
    }

    var filterList = mutableListOf<FilterInfo>()
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.walletRecordLivedata.observe(this) { it ->
            parseState(it, { it ->
                filterList = it.filterList
                val list = mutableListOf<WalletRecordInfo>()
                val recordList = it.list
                recordList.forEach {
                    list.add(it)
                    list.addAll(it.list)
                }
                if (page == 1) {
                    detailAdapter.isStateViewEnable = list.size <= 0
                    detailAdapter.submitList(list)
                } else {
                    detailAdapter.addAll(list)
                }
                setDataLoadFinish(page, list.size, mBinding.refreshLayout)
            }, {
                endLoad(page, mBinding.refreshLayout)
            })
        }
    }

    companion object {
        const val TYPE_ROSE_DETAIL = 1
        const val TYPE_COIN_DETAIL = 2
        fun lunch(context: Context, type: Int) {
            val intent = Intent(context, WalletDetailActivity::class.java)
            intent.putExtra(IntentKeyConfig.TYPE, type)
            context.startActivity(intent)
        }
    }
}