package cn.huanyuan.sweetlove.ui.recommend

import android.view.View
import cn.huanyuan.sweetlove.databinding.ActivityRecommendRoomBinding
import cn.yanhu.baselib.base.BaseActivity
import cn.huanyuan.sweetlove.R
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2025/7/17
 * desc:
 */
class RecommendRoomActivity : BaseActivity<ActivityRecommendRoomBinding, RecommendRoomViewModel>(
    R.layout.activity_recommend_room, RecommendRoomViewModel::class.java
) {
    private val roomAdapter by lazy { RecommendRoomAdapter() }
    override fun initData() {
        setStatusBarStyle(false)
        mBinding.rvRoom.adapter = roomAdapter
        roomAdapter.setOnItemClickListener(object :
            BaseQuickAdapter.OnItemClickListener<UserDetailInfo> {
            override fun onClick(
                adapter: BaseQuickAdapter<UserDetailInfo, *>, view: View, position: Int
            ) {
                val item = roomAdapter.getItem(position) ?: return
                LiveRoomManager.toLiveRoomPage(mContext, item.roomId.toString())
            }

        })
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getRecommendRoom()
    }

    override fun initRefresh() {
        super.initRefresh()
        RefreshManager.getInstance().initRefresh(mContext,false, mBinding.refreshLayout, object : IRefreshCallBack{
            override fun onRefresh() {
                requestData()
            }
        })
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.recommendRoomObservable.observe(this) {
            parseState(it, {
                val list = mutableListOf<UserDetailInfo>()
                val recommends = it.recommends
                val friends = it.friends
                val roomTitle = UserDetailInfo()
                roomTitle.roomId = -1

                val emptyItem = UserDetailInfo()
                emptyItem.roomId = -2
                list.add(roomTitle)
                if (friends.isNotEmpty()) {
                    list.addAll(friends)
                } else {
                    list.add(emptyItem)
                }
                list.add(roomTitle)
                if (recommends.isNotEmpty()) {
                    list.addAll(recommends)
                } else {
                    list.add(emptyItem)
                }
                roomAdapter.submitList(list)
                endRefreshing(mBinding.refreshLayout)
            })
        }
    }
}