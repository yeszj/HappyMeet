package cn.huanyuan.happymeet.ui.main.tab_msg

import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.FrgFriendsBinding
import cn.huanyuan.happymeet.ui.main.MainViewModel
import cn.huanyuan.happymeet.ui.main.tab_msg.adapter.FriendsAdapter
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.zj.netrequest.ext.parseState

/**
 * @author: zhengjun
 * created: 2024/2/4
 * desc:
 */
class FriendsFrg : BaseFragment<FrgFriendsBinding, MainViewModel>(
    R.layout.frg_friends,
    MainViewModel::class.java
) {
    private val adapter by lazy { FriendsAdapter() }
    private var page = 1
    override fun initData() {
        adapter.isStateViewEnable = true
        adapter.stateView = getEmptyView()
        mBinding.recyclerView.adapter = adapter
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getFriendList(page)
    }

    override fun lazyLoad() {
    }

    override fun initRefresh() {
        super.initRefresh()
        RefreshManager.getInstance().initRefresh(mContext,true,mBinding.refreshLayout,object :
            IRefreshCallBack {
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
        mViewModel.friendObservable.observe(this){ it ->
            parseState(it,{
                val tcListRes = it.list
                if (page==1){
                    adapter.submitList(tcListRes)
                }else{
                    adapter.addAll(tcListRes)
                }
                setDataLoadFinish(page,tcListRes.size,mBinding.refreshLayout)
            },{
                endLoad(page,mBinding.refreshLayout)
            })
        }
    }
}