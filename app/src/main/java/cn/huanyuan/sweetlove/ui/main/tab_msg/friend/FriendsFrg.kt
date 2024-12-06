package cn.huanyuan.sweetlove.ui.main.tab_msg.friend

import android.annotation.SuppressLint
import android.view.View
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.FrgFriendsBinding
import cn.huanyuan.sweetlove.ui.main.MainViewModel
import cn.huanyuan.sweetlove.ui.main.tab_msg.adapter.FriendTitleAdapter
import cn.huanyuan.sweetlove.ui.main.tab_msg.adapter.FriendsAdapter
import cn.huanyuan.sweetlove.ui.main.tab_msg.adapter.FriendsRequestAdapter
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.SameCityUserInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.imchat.db.ChatUserInfoManager
import cn.yanhu.imchat.manager.EmMsgManager
import cn.yanhu.imchat.manager.ImUserManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.status.BaseBean
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.hyphenate.chat.EMMessage
import com.jeremyliao.liveeventbus.LiveEventBus

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
    private lateinit var helper: QuickAdapterHelper
    private val friendTitleAdapter by lazy { FriendTitleAdapter() }
    private var friendRequestAdapter: FriendsRequestAdapter? = null
    private var friendRequestTitleAdapter: FriendTitleAdapter? = null

    private var page = 1
    override fun initData() {
        adapter.isStateViewEnable = true
        val emptyView = getEmptyView()
        emptyView.setFootText("暂无好友")
        adapter.stateView = emptyView
        helper = QuickAdapterHelper.Builder(adapter)
            .build()
        mBinding.recyclerView.adapter = helper.adapter
        helper.addBeforeAdapter(friendTitleAdapter)
        adapter.setOnItemClickListener(object :
            BaseQuickAdapter.OnItemClickListener<SameCityUserInfo> {
            override fun onClick(
                adapter: BaseQuickAdapter<SameCityUserInfo, *>,
                view: View,
                position: Int
            ) {
                val item = adapter.getItem(position) ?: return
                if (item.roomId > 0) {
                    LiveRoomManager.toLiveRoomPage(mContext, item.roomId.toString())
                } else {
                    RouteIntent.lunchPersonHomePage(item)
                }
            }
        })
        requestData()
    }

    override fun initListener() {
        super.initListener()
        LiveEventBus.get<EMMessage>(EventBusKeyConfig.RECEIVE_CMD_MSG).observe(this) {
            val source = it.getIntAttribute("source", -1)
            if (source == ChatConstant.ACTION_NEW_FRIEND_REQUEST) {
                getRequestList()
            }
        }
        LiveEventBus.get<Boolean>(EventBusKeyConfig.ADD_FRIEND_STATE).observe(this){
            if (it){
                requestData()
            }else{
                getRequestList()
            }
        }
    }

    private fun addRequestAdapterListener(){
        val listener =
            BaseQuickAdapter.OnItemChildClickListener<SameCityUserInfo> { _, view, position ->
                if (view.id == R.id.tv_pass){
                    DialogUtils.showConfirmDialog("温馨提示", {
                        operateRequest(position,1)
                    }, {
                    }, "成为好友后文字聊天免费，确认通过吗？")
                }else if (view.id == R.id.tv_refuse){
                    operateRequest(position,-1)
                }
            }
        friendRequestAdapter?.addOnItemChildClickListener(R.id.tv_pass,listener)
        friendRequestAdapter?.addOnItemChildClickListener(R.id.tv_refuse,listener)
    }

    private fun operateRequest(position:Int,status:Int){
        val item = friendRequestAdapter?.getItem(position)?:return
        mViewModel.operateRequest(item.id.toString(),status,object :
            OnRequestResultListener<String> {
            override fun onSuccess(data: BaseBean<String>) {
                if (status==1){
                    EmMsgManager.sendApplyFriend(item.userId,
                        ImUserManager.getSelfUserInfo().nickName,item.nickName)
                    ChatUserInfoManager.updateIsFriend(item.userId,true)
                    getFriendList()
                }
                getRequestList()
            }
            override fun onFail(code: Int?, msg: String?) {
                super.onFail(code, msg)
                showToast(msg)
            }
        })
    }
    override fun requestData() {
        super.requestData()
        getFriendList()
        getRequestList()
    }

    private fun getFriendList() {
        mViewModel.getFriendList(page)
    }

    private fun getRequestList() {
        mViewModel.getRequestList(1)
    }

    override fun lazyLoad() {
    }

    override fun initRefresh() {
        super.initRefresh()
        RefreshManager.getInstance().initRefresh(mContext, true, mBinding.refreshLayout, object :
            IRefreshCallBack {
            override fun onRefresh() {
                page = 1
                requestData()
            }

            override fun onLoadMore() {
                page++
                getFriendList()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.requestFriendObservable.observe(this) { it ->
            parseState(it, {
                val list = it.list
                if (list.size > 0) {
                    if (friendRequestTitleAdapter == null) {
                        friendRequestTitleAdapter = FriendTitleAdapter(false)
                        helper.addBeforeAdapter(0,friendRequestTitleAdapter!!)
                    }
                    if (friendRequestAdapter == null) {
                        friendRequestAdapter = FriendsRequestAdapter()
                        helper.addBeforeAdapter(1,friendRequestAdapter!!)
                        addRequestAdapterListener()
                    }
                    friendRequestAdapter!!.submitList(list)
                    LiveEventBus.get<Int>(EventBusKeyConfig.FRIEND_REQUEST_COUNT).post(it.totalCount.toInt())
                    friendRequestTitleAdapter!!.item = it.totalCount
                } else {
                    LiveEventBus.get<Int>(EventBusKeyConfig.FRIEND_REQUEST_COUNT).post(0)
                    if (friendRequestAdapter!=null){
                        helper.removeAdapter(friendRequestAdapter!!)
                        friendRequestAdapter = null
                    }
                    if (friendRequestTitleAdapter!=null){
                        helper.removeAdapter(friendRequestTitleAdapter!!)
                        friendRequestTitleAdapter = null
                    }
                }
            })
        }
        mViewModel.friendObservable.observe(this) { it ->
            parseState(it, {
                val tcListRes = it.list
                friendTitleAdapter.item = it.totalCount
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
}