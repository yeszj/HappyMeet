package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.liveRoom.LiveRoomUserListAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.databinding.PopLiveRoomUserListBinding
import cn.yanhu.agora.listener.OnSendSeatInviteListener
import cn.yanhu.baselib.refresh.NoMoreDataFootView
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.imchat.manager.EmMsgManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.chad.library.adapter4.BaseQuickAdapter
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.scwang.smart.refresh.footer.BallPulseFooter

/**
 * @author: zhengjun
 * created: 2024/10/16
 * desc:
 */
@SuppressLint("ViewConstructor")
class LiveRoomSeatManagerPop(
    context: Context,
    private val userList: MutableList<UserDetailInfo>,
    val roomDetailInfo: RoomDetailInfo,
    val gender:String,
    val onSendSeatInviteListener: OnSendSeatInviteListener

) : BottomPopupView(context) {
    private val userAdapter by lazy { LiveRoomUserListAdapter() }
    override fun getImplLayoutId(): Int {
        return R.layout.pop_live_room_user_list
    }

    private lateinit var mBiding: PopLiveRoomUserListBinding
    private var page:Int = 1
    private var type :Int =0
    private var inRoomUserList: MutableList<UserDetailInfo> = mutableListOf()
    private var onlineUserList: MutableList<UserDetailInfo> = mutableListOf()
    private var friendUserList: MutableList<UserDetailInfo> = mutableListOf()
    override fun onCreate() {
        super.onCreate()
        mBiding = PopLiveRoomUserListBinding.bind(popupImplView)
        val emptyView = NoMoreDataFootView(context)
        emptyView.footViewState(NoMoreDataFootView.FOOT_NO_DATA)
        emptyView.setFootText("暂无在线用户")
        userAdapter.stateView = emptyView
        mBiding.rvUser.adapter = userAdapter
        inRoomUserList = userList
        userAdapter.submitList(userList)
        userAdapter.isStateViewEnable = userList.size <= 0
        userAdapter.addOnItemChildClickListener(R.id.tv_invite,
            object : BaseQuickAdapter.OnItemChildClickListener<UserDetailInfo> {
                override fun onItemClick(
                    adapter: BaseQuickAdapter<UserDetailInfo, *>,
                    view: View,
                    position: Int
                ) {
                    val ownerInfo = roomDetailInfo.ownerInfo?:return
                    val map: MutableMap<String, Any> = HashMap()
                    map["fromNickName"] = ownerInfo.nickName
                    map["portrait"] = ownerInfo.portrait
                    val item = userAdapter.getItem(position) ?: return
                    if (item.seatNum > 0) {
                        EmMsgManager.sendAgreeSeatApplyMsg(
                            item.userId,
                            item.nickName,
                            roomDetailInfo.roomId.toString(),
                            item.seatNum.toString()
                        )
                        userAdapter.removeAt(position)
                        showToast("已同意")
                    } else {
                        onSendSeatInviteListener.onSendInvite(map,item)
                    }
                }

            })
        mBiding.ivClose.setOnSingleClickListener { dismiss() }
        mBiding.refresh.setRefreshFooter(BallPulseFooter(context))
        mBiding.refresh.setOnRefreshListener {
            refreshUserList()
        }
        mBiding.refresh.setOnLoadMoreListener {
            page++
            getUserList()
        }
        mBiding.vgTab.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb1 -> {
                    userAdapter.submitList(inRoomUserList)
                    type = 0
                }
                R.id.rb2 -> {
                    userAdapter.submitList(onlineUserList)
                    type = 1
                }
                R.id.rb3 -> {
                    userAdapter.submitList(friendUserList)
                    type = 2
                }
            }
            refreshUserList()
        }
    }

    private fun refreshUserList() {
        page = 1
        getUserList()
    }

    private fun getUserList() {
        request({ agoraRxApi.getInviteList(roomDetailInfo.roomId, gender, type.toString(),page) },
            object : OnRequestResultListener<MutableList<UserDetailInfo>> {
                override fun onSuccess(data: BaseBean<MutableList<UserDetailInfo>>) {
                    val userList = data.data ?: return
                    if (page == 1){
                        when (type) {
                            0 -> {
                                inRoomUserList = userList
                            }
                            1 -> {
                                onlineUserList = userList
                            }
                            2 -> {
                                friendUserList = userList
                            }
                        }
                        userAdapter.submitList(userList)
                        mBiding.refresh.finishRefresh()
                    }else{
                        when (type) {
                            0 -> {
                                inRoomUserList.addAll(userList)
                            }
                            1 -> {
                                onlineUserList.addAll(userList)
                            }
                            2 -> {
                                friendUserList.addAll(userList)
                            }
                        }
                        userAdapter.addAll(userList)
                        if (userList.size<10){
                            mBiding.refresh.finishLoadMoreWithNoMoreData()
                        }else{
                            mBiding.refresh.finishLoadMore()
                        }
                    }
                }
            })
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            userList: MutableList<UserDetailInfo>,
            ownerInfo: RoomDetailInfo,
            gender: String = "0",
            onSendSeatInviteListener: OnSendSeatInviteListener
        ): LiveRoomSeatManagerPop {
            val matchPop = LiveRoomSeatManagerPop(mContext, userList, ownerInfo,gender,onSendSeatInviteListener)
            val builder =
                XPopup.Builder(mContext)
            builder.enableDrag(false).asCustom(matchPop).show()
            return matchPop
        }
    }
}