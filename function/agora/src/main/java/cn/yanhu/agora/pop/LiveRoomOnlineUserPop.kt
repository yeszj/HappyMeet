package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.liveRoom.LiveRoomOnlineUserAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.RoomOnlineResponse
import cn.yanhu.agora.databinding.PopLiveRoomOnlineUserBinding
import cn.yanhu.agora.listener.OnSendSeatInviteListener
import cn.yanhu.baselib.refresh.NoMoreDataFootView
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.imchat.manager.EmMsgManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.util.addOnDebouncedChildClick
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.scwang.smart.refresh.footer.BallPulseFooter

/**
 * @author: zhengjun
 * created: 2024/10/16
 * desc:
 */
@SuppressLint("ViewConstructor")
class LiveRoomOnlineUserPop(
    context: Context,
    private val userList: MutableList<UserDetailInfo>,
    val roomDetailInfo: RoomDetailInfo,
    val onSendSeatInviteListener: OnSendSeatInviteListener


) : BottomPopupView(context) {
    private val userAdapter by lazy { LiveRoomOnlineUserAdapter() }
    override fun getImplLayoutId(): Int {
        return R.layout.pop_live_room_online_user
    }

    private lateinit var mBiding: PopLiveRoomOnlineUserBinding
    private val userIdList: MutableList<String> = mutableListOf()
    override fun onCreate() {
        super.onCreate()
        mBiding = PopLiveRoomOnlineUserBinding.bind(popupImplView)
        val emptyView = NoMoreDataFootView(context)
        emptyView.footViewState(NoMoreDataFootView.FOOT_NO_DATA)
        emptyView.setFootText("暂无在线用户")
        if (roomDetailInfo.isOwner()){
            mBiding.tvAdminSet.visibility = View.VISIBLE
        }else{
            mBiding.tvAdminSet.visibility = View.INVISIBLE
        }
        userAdapter.refreshRoomInfo(roomDetailInfo)
        userAdapter.stateView = emptyView
        mBiding.rvUser.adapter = userAdapter
//        removeSameUser(userList)
//        userAdapter.submitList(userList)
        userAdapter.setOnItemClickListener(object : BaseQuickAdapter.OnItemClickListener<UserDetailInfo>{
            override fun onClick(
                adapter: BaseQuickAdapter<UserDetailInfo, *>,
                view: View,
                position: Int
            ) {
                val item = userAdapter.getItem(position)?:return
                onSendSeatInviteListener.onClickUser(item.userId)
            }
        })
        userAdapter.addOnDebouncedChildClick(R.id.tv_skip
        ) { adapter, _, position -> skipUser(adapter, position) }
        userAdapter.addOnDebouncedChildClick(R.id.tv_admin) { adapter, _, position -> setUserAdmin(adapter, position) }
        userAdapter.addOnDebouncedChildClick(R.id.tv_forbidden
        ) { adapter, _, position -> setUserMute(adapter, position) }
        userAdapter.addOnDebouncedChildClick(
            R.id.tv_invite
        ) { _, _, position -> operateSeat(position) }
        mBiding.ivClose.setOnSingleClickListener { dismiss() }
        mBiding.refresh.setRefreshFooter(BallPulseFooter(context))
        mBiding.refresh.setOnRefreshListener {
            page = 1
            getOnlineUserList()
        }
        mBiding.refresh.setOnLoadMoreListener {
            page++
            getOnlineUserList()
        }
        mBiding.tvAdminSet.setOnSingleClickListener {
            userAdapter.changeAdminSetSwitch()
            if (userAdapter.roomAdminSwitch){
                mBiding.tvAdminSet.text = "完成"
                mBiding.tvAdminSet.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.white))
                mBiding.tvAdminSet.setBackgroundResource(cn.yanhu.commonres.R.drawable.common_shape_click_bg_r20)
            }else{
                mBiding.tvAdminSet.text = "管理员设置"
                mBiding.tvAdminSet.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.color6))
                mBiding.tvAdminSet.setBackgroundResource(R.drawable.shape_transparent)
            }

        }
        getOnlineUserList()
    }

    private fun operateSeat(position: Int) {
        val ownerInfo = roomDetailInfo.ownerInfo ?: return
        val map: MutableMap<String, Any> = HashMap()
        map["fromNickName"] = ownerInfo.nickName
        map["portrait"] = ownerInfo.portrait
        val item = userAdapter.getItem(position) ?: return
        if (item.seatNum > 0) {
            //下麦
            EmMsgManager.sendCmdMessagePeople(
                item.userId, "", ChatConstant.ACTION_MSG_SIT_DOWN
            )
        } else {
            onSendSeatInviteListener.onSendInvite(map, item)

        }
    }

    private fun setUserAdmin(
        adapter: BaseQuickAdapter<UserDetailInfo, *>,
        position: Int
    ) {
        val item = adapter.getItem(position) ?: return
        request({ agoraRxApi.setAdmin(roomDetailInfo.roomId!!, item.userId,if (item.roomAdmin) 0 else 1) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    item.roomAdmin = !item.roomAdmin
                }
            })
    }

    private fun setUserMute(
        adapter: BaseQuickAdapter<UserDetailInfo, *>,
        position: Int
    ) {
        val item = adapter.getItem(position) ?: return
        request({ agoraRxApi.setUserMute(roomDetailInfo.roomId!!, item.userId,if (item.ifMute) 0 else 1) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    item.ifMute = !item.ifMute
                }
            })
    }

    private fun skipUser(
        adapter: BaseQuickAdapter<UserDetailInfo, *>,
        position: Int
    ) {
        val item = adapter.getItem(position) ?: return
        request({ agoraRxApi.kickOut(roomDetailInfo.roomId!!, item.userId) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    DialogUtils.dismissLoading()
                    showToast("已踢出")
                    userAdapter.removeAt(position)
                }
            })
    }

    private fun removeSameUser(userList:MutableList<UserDetailInfo>) {
        for (i in userList.count() - 1 downTo 0) {
            val userInfo = userList[i]
            if (userIdList.contains(userInfo.userId)) {
                userList.removeAt(i)
            } else {
                userIdList.add(userInfo.userId)
            }
        }
    }

    private var page = 1
    private fun getOnlineUserList() {
        request({ agoraRxApi.getOnlineUserList(roomDetailInfo.roomId, page) },
            object : OnRequestResultListener<RoomOnlineResponse> {
                override fun onSuccess(data: BaseBean<RoomOnlineResponse>) {
                    val onlineResponse = data.data ?: return
                    val onlineUsers = onlineResponse.onlineUsers
                    if (page == 1) {
                        userIdList.clear()
                    }
                    removeSameUser(onlineUsers)
                    if (page == 1) {
                        userAdapter.isStateViewEnable = onlineUsers.isEmpty()
                        userAdapter.submitList(onlineUsers)
                        mBiding.refresh.finishRefresh()
                    } else {
                        userAdapter.addAll(onlineUsers)
                        if (onlineUsers.size<10){
                            mBiding.refresh.finishLoadMoreWithNoMoreData()
                        }else{
                            mBiding.refresh.finishLoadMore()
                        }
                    }
                }
            })
    }

    fun refreshOnlineUser(onlineUserList: MutableList<UserDetailInfo>) {
        userAdapter.submitList(onlineUserList)
    }

    fun refreshRoomInfo(roomDetailInfo: RoomDetailInfo) {
        userAdapter.refreshRoomInfo(roomDetailInfo)
    }
    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            userList: MutableList<UserDetailInfo>,
            ownerInfo: RoomDetailInfo,
            onSendSeatInviteListener: OnSendSeatInviteListener
        ): LiveRoomOnlineUserPop {
            val matchPop = LiveRoomOnlineUserPop(mContext, userList, ownerInfo,onSendSeatInviteListener)
            val builder = XPopup.Builder(mContext)
            builder.enableDrag(false).asCustom(matchPop).show()
            return matchPop
        }
    }
}