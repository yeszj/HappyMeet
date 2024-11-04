package cn.huanyuan.sweetlove.ui.main.tab_blinddate

import android.os.Bundle
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.FrgBlindUserListItemBinding
import cn.huanyuan.sweetlove.ui.main.MainViewModel
import cn.huanyuan.sweetlove.ui.main.tab_blinddate.adapter.RoomListAdapter
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.loading.RoomLoadingCallBack
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.hyphenate.chat.EMMessage
import com.jeremyliao.liveeventbus.LiveEventBus

/**
 * @author: zhengjun
 * created: 2024/2/18
 * desc:交友房间和用户列表
 */
class BlindUserOrRoomItemFrg : BaseFragment<FrgBlindUserListItemBinding, MainViewModel>(
    R.layout.frg_blind_user_list_item, MainViewModel::class.java
) {
    private var type = TYPE_RECOMMEND
    private var page = 1
    private val adapter by lazy { RoomListAdapter(mContext) }
    override fun initData() {
        type = requireArguments().getInt(IntentKeyConfig.TYPE)
        mBinding.recyclerView.adapter = adapter
        val emptyView = getEmptyView()
        emptyView.setFootText("暂无房间")
        adapter.stateView = emptyView
        adapter.setOnDebouncedItemClick { adapter, _, position ->
            val item = adapter.getItem(position)
            item?.apply {
                if (item.id <= 0) {
                    RouteIntent.lunchPersonHomePage(this.ownerInfo?.userId)
                } else {
                    LiveRoomManager.toLiveRoomPage(mContext, this.id.toString())
                }
            }
        }
    }

    override fun initLoadService() {
        val loadSir = initCustomLoadingLoad(RoomLoadingCallBack())
        loadService = loadSir.register(mBinding.root) {
            onReload()
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

    override fun initListener() {
        super.initListener()
        LiveEventBus.get<String>(EventBusKeyConfig.CLOSELIVEROOM).observe(this) {
            page = 1
            requestData()
        }
        LiveEventBus.get<EMMessage>(EventBusKeyConfig.RECEIVE_CMD_MSG).observe(this){
            val source = it.getIntAttribute("source", -1)
            if (source == ChatConstant.ACTION_MSG_SWITCH_TYPE_CONFIRM || source == ChatConstant.ACTION_MSG_SWITCH_TYPE_PLAZA){
                //有房主将房间切换位专属或者大厅 刷新页面
                page = 1
                requestData()
            }
        }
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.roomListObservable.observe(this) { it ->
            parseState(it, {
                val roomList = it.roomList
                if (page == 1) {
                    adapter.isStateViewEnable = roomList.size <= 0
                    adapter.submitList(roomList)
                } else {
                    adapter.addAll(roomList)
                }
                setDataLoadFinish(page, roomList.size, mBinding.refreshLayout)
            }, {
                endLoad(page, mBinding.refreshLayout)
            })
        }
    }

    override fun requestData() {
        mViewModel.getRoomList(type, page)
    }

    companion object {
        const val TYPE_RECOMMEND = 1
        const val TYPE_EXCLUSIVE = 2
        const val TYPE_FRIENDS = 3
        fun newsInstance(type: Int): BlindUserOrRoomItemFrg {
            val fragment = BlindUserOrRoomItemFrg()
            val bundle = Bundle()
            bundle.putInt(IntentKeyConfig.TYPE, type)
            fragment.arguments = bundle
            return fragment
        }
    }
}