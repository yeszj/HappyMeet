package cn.huanyuan.sweetlove.ui.main.tab_samecity

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.FrgSameCityUserListBinding
import cn.huanyuan.sweetlove.ui.main.MainViewModel
import cn.huanyuan.sweetlove.ui.main.tab_samecity.adapter.SameCityUserListAdapter
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.commonres.bean.SameCityUserInfo
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.imchat.ui.chat.ImChatActivity
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.chad.library.adapter4.util.setOnDebouncedItemClick
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
        val linearLayoutManager = LinearLayoutManager(context)
        mBinding.recyclerView.layoutManager = linearLayoutManager
        linearLayoutManager.isItemPrefetchEnabled = true
        linearLayoutManager.initialPrefetchItemCount = 10
        adapter.setHasStableIds(true)
        mBinding.recyclerView.adapter = adapter
    }

    override fun requestData() {
        super.requestData()
        filterCity = AppCacheManager.province
        mViewModel.getSameCityUserList(filterAge,filterCity,page)
    }

    override fun initListener() {
        super.initListener()
        adapter.setOnDebouncedItemClick{ adapter, _, position ->
            val item = adapter.getItem(position)?:return@setOnDebouncedItemClick
            if (item.roomId>0){
                LiveRoomManager.toLiveRoomPage(mContext,item.roomId.toString())
            }else{
                RouteIntent.lunchPersonHomePage(item)
            }
        }
        adapter.addOnDebouncedChildClick(R.id.iv_accost,1000,object : BaseQuickAdapter.OnItemChildClickListener<SameCityUserInfo>{
            override fun onItemClick(
                adapter: BaseQuickAdapter<SameCityUserInfo, *>,
                view: View,
                position: Int
            ) {
                val item = adapter.getItem(position)?:return
                ImChatActivity.lunch(mContext,item.userId)
            }
        })
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