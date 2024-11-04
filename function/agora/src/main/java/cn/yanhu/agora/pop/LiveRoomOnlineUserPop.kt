package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.liveRoom.LiveRoomOnlineUserAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.RoomOnlineResponse
import cn.yanhu.agora.databinding.PopLiveRoomOnlineUserBinding
import cn.yanhu.baselib.refresh.NoMoreDataFootView
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
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * @author: zhengjun
 * created: 2024/10/16
 * desc:
 */
@SuppressLint("ViewConstructor")
class LiveRoomOnlineUserPop(
    context: Context,
    private val userList: MutableList<UserDetailInfo>,
    val roomDetailInfo: RoomDetailInfo

) : BottomPopupView(context) {
    private val userAdapter by lazy { LiveRoomOnlineUserAdapter() }
    override fun getImplLayoutId(): Int {
        return R.layout.pop_live_room_online_user
    }

    private lateinit var mBiding: PopLiveRoomOnlineUserBinding
    override fun onCreate() {
        super.onCreate()
        mBiding = PopLiveRoomOnlineUserBinding.bind(popupImplView)
        val emptyView = NoMoreDataFootView(context)
        emptyView.footViewState(NoMoreDataFootView.FOOT_NO_DATA)
        emptyView.setFootText("暂无在线用户")
        userAdapter.stateView = emptyView
        mBiding.rvUser.adapter = userAdapter
        userAdapter.submitList(userList)
        userAdapter.isStateViewEnable = userList.size <= 0
        userAdapter.addOnItemChildClickListener(
            R.id.tv_invite,
            object : BaseQuickAdapter.OnItemChildClickListener<UserDetailInfo> {
                override fun onItemClick(
                    adapter: BaseQuickAdapter<UserDetailInfo, *>, view: View, position: Int
                ) {
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
                        EmMsgManager.sendCmdMessagePeople(
                            item.userId, ChatConstant.ACTION_MSG_SET_UP, map
                        )
                        showToast("上麦邀请已发送")
                    }
                }

            })
        mBiding.ivClose.setOnSingleClickListener { dismiss() }
        mBiding.refresh.setOnRefreshListener {
            page = 1
            getOnlineUserList()
        }
        mBiding.refresh.setOnLoadMoreListener {
            page++
            getOnlineUserList()
        }
    }

    private var page = 1;
    private fun getOnlineUserList() {
        request({ agoraRxApi.getOnlineUserList(roomDetailInfo.roomId, 1) },
            object : OnRequestResultListener<RoomOnlineResponse> {
                override fun onSuccess(data: BaseBean<RoomOnlineResponse>) {
                    val onlineResponse = data.data ?: return
                    val onlineUsers = onlineResponse.onlineUsers
                    if (page == 1) {
                        userAdapter.submitList(onlineUsers)
                        mBiding.refresh.finishRefresh()
                    } else {
                        userAdapter.addAll(onlineUsers)
                        if (onlineUsers.size <= 0) {
                            mBiding.refresh.finishLoadMoreWithNoMoreData()
                        } else {
                            mBiding.refresh.finishLoadMore()
                        }
                    }
                }
            })
    }

    fun refreshOnlineUser(onlineUserList: MutableList<UserDetailInfo>) {
        userAdapter.submitList(onlineUserList)
    }


    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            userList: MutableList<UserDetailInfo>,
            ownerInfo: RoomDetailInfo,
        ): LiveRoomOnlineUserPop {
            val matchPop = LiveRoomOnlineUserPop(mContext, userList, ownerInfo)
            val builder = XPopup.Builder(mContext)
            builder.enableDrag(false).asCustom(matchPop).show()
            return matchPop
        }
    }
}