package cn.huanyuan.sweetlove.ui.main.tab_msg

import android.annotation.SuppressLint
import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.FrgFriendsBinding
import cn.huanyuan.sweetlove.ui.main.MainViewModel
import cn.huanyuan.sweetlove.ui.main.tab_msg.adapter.FriendsAdapter
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.commonres.bean.SameCityUserInfo
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.BaseQuickAdapter

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
        adapter.setOnItemClickListener(object : BaseQuickAdapter.OnItemClickListener<SameCityUserInfo>{
            override fun onClick(
                adapter: BaseQuickAdapter<SameCityUserInfo, *>,
                view: View,
                position: Int
            ) {
                val item = adapter.getItem(position)?:return
                if (item.roomId>0){
                    LiveRoomManager.toLiveRoomPage(mContext,item.roomId.toString())
                }else{
                    RouteIntent.lunchPersonHomePage(item)
                }
            }
        })
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

    @SuppressLint("SetTextI18n")
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.friendObservable.observe(this){ it ->
            parseState(it,{
                val tcListRes = it.list
                mBinding.tvTitle.text = "我的好友(${it.totalCount})"
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