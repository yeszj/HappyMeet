package cn.yanhu.agora.ui.liveRoom.live

import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import cn.yanhu.agora.adapter.liveRoom.ThreeRoomSeatAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.RoomOnlineResponse
import cn.yanhu.agora.databinding.ViewThreeRoomTopViewBinding
import cn.yanhu.agora.pop.LiveRoomSeatManagerPop
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.imchat.manager.EmMsgManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.layoutmanager.QuickGridLayoutManager

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
class ThreeLiveRoomFrg : BaseLiveRoomFrg() {
    override fun initData() {
        mBinding.rvSeat.layoutManager = QuickGridLayoutManager(mContext, 2)
        seatUserAdapter =
            ThreeRoomSeatAdapter()
        addTopTitleView()
        super.initData()
        mBinding.rvSeat.adapter = seatUserAdapter
        if (isOwner) {
            mBinding.vgAutoSeat.visibility = View.VISIBLE
        } else {
            mBinding.vgAutoSeat.visibility = View.INVISIBLE
        }


    }


    private var liveRoomUserListPop: LiveRoomSeatManagerPop? = null
    fun showUserList(gender: String) {
        request({ agoraRxApi.getInviteList(roomId, gender, "0", 1) },
            object : OnRequestResultListener<MutableList<UserDetailInfo>> {
                override fun onSuccess(data: BaseBean<MutableList<UserDetailInfo>>) {
                    val userList = data.data ?: return
                    if (CommonUtils.isPopShow(liveRoomUserListPop)) {
                        return
                    }
                    liveRoomUserListPop = LiveRoomSeatManagerPop.showDialog(
                        mContext,
                        userList,
                        roomSourceBean, gender, onSendSeatInviteListener = inviteSeatListener
                    )
                }
            })
    }

    private lateinit var topTitleBinding: ViewThreeRoomTopViewBinding
    private fun addTopTitleView() {
        val rankView =
            LayoutInflater.from(mContext)
                .inflate(cn.yanhu.agora.R.layout.view_three_room_top_view, null)
        topTitleBinding = DataBindingUtil.bind(rankView)!!
        topTitleBinding.tvOnlineNum.setOnSingleClickListener {
            showOnlineUserList()
        }
        topTitleBinding.ivExit.setOnSingleClickListener {
            showFloatWindow(1)
        }
        mBinding.flTopView.addView(rankView)
    }

    override fun refreshOnlineUser(onlineResponse: RoomOnlineResponse) {
        super.refreshOnlineUser(onlineResponse)
        topTitleBinding.tvOnlineNum.text = onlineResponse.onlineNum.toString()
    }


    override fun getRoomInfoSuccess() {
        super.getRoomInfoSuccess()
        topTitleBinding.roomInfo = roomSourceBean
    }

    private val childItemClickListener =
        object : BaseQuickAdapter.OnItemChildClickListener<RoomSeatInfo> {
            override fun onItemClick(
                adapter: BaseQuickAdapter<RoomSeatInfo, *>, view: View, position: Int
            ) {
                val item = seatUserAdapter.getItem(position) ?: return

                when (view.id) {
                    cn.yanhu.agora.R.id.tv_switch -> {
                        //切换成专属房间/大厅
                        showSwitchRoomTypePop()
                    }

                    cn.yanhu.agora.R.id.iv_voiceStatus -> {
                        //开关麦
                        val roomUserSeatInfo = item.roomUserSeatInfo ?: return
                        if (localUserId.toString() == roomUserSeatInfo.userId) {
                            switchMikeAlert(!item.mikeUser, item.id)
                        } else if (isOwner) {
                            ownerSwitchMikeAlert(!item.mikeUser, item.id,roomUserSeatInfo.userId)
                        }
                    }
                    cn.yanhu.agora.R.id.tv_manApplyCount -> {
                        showUserList("1")
                    }

                    cn.yanhu.agora.R.id.tv_womanApplyCount -> {
                        showUserList("2")
                    }

                    cn.yanhu.agora.R.id.vg_parent, cn.yanhu.agora.R.id.anchorSeatInfo -> {
                        val roomUserSeatInfo = item.roomUserSeatInfo
                        if (roomUserSeatInfo == null) {
                            if (isOwner) {
                                //邀请上麦弹框
                                showUserList(if (item.id == 2) "1" else "2")
                            } else {
                                //上麦
                                if (AppCacheManager.isMan()) {
                                    if (position == 1) {
                                        userSetSeat(if (roomSourceBean.autoSeat) SEAT_TYPE_AUTO else SEAT_TYPE_APPLY)
                                    } else {
                                        showToast("该座位仅对女用户开放")
                                    }
                                } else {
                                    if (position == 2) {
                                        userSetSeat(if (roomSourceBean.autoSeat) SEAT_TYPE_AUTO else SEAT_TYPE_APPLY)
                                    } else {
                                        showToast("该座位仅对男用户开放")
                                    }
                                }
                            }
                        } else {
                            if (roomUserSeatInfo.userId == AppCacheManager.userId) {
                                showUserPop(roomUserSeatInfo.userId)
                            } else {
                                showSendGiftPop(roomUserSeatInfo)
                            }
                        }
                    }
                }
            }

        }

    override fun initListener() {
        super.initListener()
        seatUserAdapter.addOnItemChildClickListener(
            cn.yanhu.agora.R.id.anchorSeatInfo, childItemClickListener
        )
        seatUserAdapter.addOnItemChildClickListener(
            cn.yanhu.agora.R.id.vg_parent, childItemClickListener
        )
        seatUserAdapter.addOnItemChildClickListener(
            cn.yanhu.agora.R.id.iv_voiceStatus, childItemClickListener
        )
        seatUserAdapter.addOnItemChildClickListener(
            cn.yanhu.agora.R.id.tv_switch, childItemClickListener
        )
        seatUserAdapter.addOnItemChildClickListener(
            cn.yanhu.agora.R.id.tv_manApplyCount,
            childItemClickListener
        )
        seatUserAdapter.addOnItemChildClickListener(
            cn.yanhu.agora.R.id.tv_womanApplyCount,
            childItemClickListener
        )
    }

    private fun showSwitchRoomTypePop() {
        val content = Spans.builder()
            .text(if (roomSourceBean.isPrivateRoom()) "确定要转为大厅直播吗？" else "转为专属房间后只保留麦上的男女嘉宾，确认要转换吗？\n\n")
            .text(if (roomSourceBean.isPrivateRoom()) "" else "温馨提示：专属房间需要付费，男嘉宾同意后才可转换成功")
            .color(
                CommonUtils.getColor(
                    R.color.colorMain
                )
            ).build()

        DialogUtils.showConfirmDialog(
            if (roomSourceBean.isPrivateRoom()) "转为大厅房间" else "转为专属房间",
            {
                switchRoomType()
            },
            {

            },
            content,
            cancelBg = R.drawable.shape_cancel_btn_r30
        )
    }

    private fun switchRoomType() {
        val roomType = if (roomSourceBean.isPrivateRoom()) 1 else 2
        mViewModel.switchRoomType(roomId,
            roomType.toString(),
            object : OnRequestResultListener<Boolean> {
                override fun onSuccess(data: BaseBean<Boolean>) {
                    if (roomType == 1) {
                        roomSourceBean.roomType = roomType
                        EmMsgManager.sendCmdMessageToChatRoom(
                            roomSourceBean.uid, "", ChatConstant.ACTION_MSG_SWITCH_TYPE_PLAZA
                        )
                        showToast("房间已切换为大厅")
                    } else {
                        showToast("已发送消息至男嘉宾，男嘉宾同意后可转至专属房间")
                    }
                }
            })
    }

}