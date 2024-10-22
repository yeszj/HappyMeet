package cn.huanyuan.sweetlove.ui.history

import android.content.Context
import android.content.Intent
import android.view.View
import cn.huanyuan.sweetlove.databinding.ActivitySeenMeHistoryBinding
import cn.yanhu.baselib.base.BaseActivity
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.ui.history.adapter.SeenMeHistoryAdapter
import cn.huanyuan.sweetlove.ui.userinfo.level.UserLevelActivity
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.commonres.router.RouterPath
import cn.zj.netrequest.ext.parseState
import com.alibaba.android.arouter.facade.annotation.Route

/**
 * @author: zhengjun
 * created: 2024/3/4
 * desc:谁看过我
 */

@Route(path = RouterPath.ROUTER_SEEN_ME)
class SeenMeHistoryActivity : BaseActivity<ActivitySeenMeHistoryBinding, HistoryViewModel>(
    R.layout.activity_seen_me_history,
    HistoryViewModel::class.java
) {
    private val historyAdapter by lazy { SeenMeHistoryAdapter() }
    private var page = 1
    override fun initData() {
        setStatusBarStyle(false)
        historyAdapter.isStateViewEnable = true
        historyAdapter.stateView = getEmptyView()
        mBinding.rvHistory.adapter = historyAdapter
        requestData()
    }

    override fun initListener() {
        super.initListener()
        mBinding.ivOpen.setOnClickListener {
            UserLevelActivity.lunch(mContext)
        }
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getHistoryViewPageInfo(page)
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
        mViewModel.seenMeHistoryObservable.observe(this) { it ->
            parseState(it, {
                val hasPermission = it.isHasPermission
                historyAdapter.setIsLock(hasPermission)
                mBinding.refreshLayout.setEnableLoadMore(hasPermission)
                mBinding.rvHistory.isNestedScrollingEnabled = hasPermission
                if (hasPermission){
                    mBinding.ivOpen.visibility = View.GONE
                }else{
                    mBinding.ivOpen.visibility = View.VISIBLE
                }
                val historyViewPages = it.historyViewPages
                if (page == 1) {
                    historyAdapter.submitList(historyViewPages)
                } else {
                    historyAdapter.addAll(historyViewPages)
                }
                setDataLoadFinish(page,historyViewPages.size,mBinding.refreshLayout)
            },{
                endLoad(page,mBinding.refreshLayout)
            })
        }
    }
    companion object{
        fun lunch(context: Context){
            context.startActivity(Intent(context,SeenMeHistoryActivity::class.java))
        }
    }
}