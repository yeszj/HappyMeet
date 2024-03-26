package cn.huanyuan.happymeet.ui.main.tab_samecity

import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.FrgSameCityUserListBinding
import cn.huanyuan.happymeet.ui.main.MainViewModel
import cn.huanyuan.happymeet.ui.main.tab_samecity.adapter.SameCityUserListAdapter
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.ext.parseState
import com.jeremyliao.liveeventbus.LiveEventBus
import org.json.JSONObject

/**
 * @author: zhengjun
 * created: 2024/2/1
 * desc:
 */
class UserListFrg : BaseFragment<FrgSameCityUserListBinding, MainViewModel>(
    R.layout.frg_same_city_user_list,
    MainViewModel::class.java
) {
    private val adapter by lazy { SameCityUserListAdapter() }
    private var page = 1
    private var filterCity:String = ""
    private var filterAge:String = ""
    override fun initData() {
        adapter.stateView = getEmptyView()
        mBinding.recyclerView.itemAnimator?.changeDuration = 0
        adapter.setHasStableIds(true)
        mBinding.recyclerView.adapter = adapter
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getSameCityUserList(filterAge,filterCity,page)
    }

    override fun initListener() {
        super.initListener()
        adapter.setOnItemClickListener { adapter, _, position ->
            val item = adapter.getItem(position)
            RouteIntent.lunchPersonHomePage(item?.userId)
        }
    }

    override fun initRefresh() {
        super.initRefresh()
        RefreshManager.getInstance().initRefresh(mContext,true,mBinding.refreshLayout,object : IRefreshCallBack{
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
        onGetUserListResult()
        LiveEventBus.get<JSONObject>(LiveDataEventManager.START_FILTER_USER).observe(this) {
            filterCity = it.optString(FILTER_CITY)
            filterAge = it.optString(FILTER_AGE)
            requestData()
        }
    }

    private fun onGetUserListResult() {
        mViewModel.sameCityUserObservable.observe(this) { it ->
            parseState(it, {
                adapter.isStateViewEnable = true
                val tcListRes = it.tcListRes
                if (page == 1) {
                    adapter.submitList(tcListRes)
                } else {
                    adapter.addAll(tcListRes)
                }
                setDataLoadFinish(page, tcListRes.size, mBinding.refreshLayout)
            }, {
                endLoad(page, mBinding.refreshLayout)
            })
        }
    }

    companion object{
        const val FILTER_CITY = "filterCity"
        const val FILTER_AGE = "filterAge"
    }
}