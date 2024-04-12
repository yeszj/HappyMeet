package cn.huanyuan.happymeet.ui.main.tab_blinddate

import android.os.Bundle
import android.text.TextUtils
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.FrgBlindUserListItemBinding
import cn.huanyuan.happymeet.ui.main.MainViewModel
import cn.huanyuan.happymeet.ui.main.tab_blinddate.adapter.RoomListAdapter
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.loading.RoomLoadingCallBack
import cn.yanhu.commonres.router.RouteIntent
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.util.setOnDebouncedItemClick

/**
 * @author: zhengjun
 * created: 2024/2/18
 * desc:相亲房间和用户列表
 */
class BlindUserOrRoomItemFrg : BaseFragment<FrgBlindUserListItemBinding, MainViewModel>(
    R.layout.frg_blind_user_list_item,
    MainViewModel::class.java
) {
    private var type = TYPE_RECOMMEND
    private var page = 1
    private val adapter by lazy { RoomListAdapter(mContext) }
    override fun initData() {
        type = requireArguments().getInt(IntentKeyConfig.TYPE)
        mBinding.recyclerView.adapter = adapter
        adapter.setOnDebouncedItemClick { adapter, _, position ->
            val item = adapter.getItem(position)
            item?.apply {
                if (TextUtils.isEmpty(item.roomId)) {
                    RouteIntent.lunchPersonHomePage(this.ownerInfo?.userId)
                } else {
                    RouteIntent.lunchToLiveRoom(adapter.items.toMutableList(),page, item.roomId.toString())
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
        RefreshManager.getInstance().initRefresh(
            mContext,
            true,
            mBinding.refreshLayout,
            object : IRefreshCallBack {
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
        mViewModel.roomListObservable.observe(this) { it ->
            parseState(it, {
                val roomList = it.roomList
                if (page == 1) {
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