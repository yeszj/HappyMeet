package cn.yanhu.agora.ui.liveRoom.live

import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import cn.yanhu.agora.adapter.liveRoom.ThreeRoomSeatAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.RoomOnlineResponse
import cn.yanhu.agora.databinding.ViewThreeRoomTopViewBinding
import cn.yanhu.agora.pop.LiveRoomSeatManagerPop
import cn.yanhu.agora.pop.RoomWishListPop
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.api.commonRxApi
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.bean.SendGiftRequest
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.manager.EmMsgManager
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ErrorCode
import com.blankj.utilcode.util.VibrateUtils
import com.chad.library.adapter4.BaseMultiItemAdapter
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.layoutmanager.QuickGridLayoutManager
import com.hyphenate.chat.EMMessage

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
        (seatUserAdapter as ThreeRoomSeatAdapter).onRoomItemClickListener = object : ThreeRoomSeatAdapter.OnRoomItemClickListener{
            override fun onClickWish() {
                showWishListPop()
            }
        }
        addTopTitleView()
        super.initData()
        mBinding.rvSeat.adapter = seatUserAdapter
        if (isOwner) {
            mBinding.vgAutoSeat.visibility = View.VISIBLE
        } else {
            mBinding.vgAutoSeat.visibility = View.INVISIBLE
        }

    }

    private fun checkExclusiveSwitch() {
        request({ commonRxApi.getConfigInfo("exclusive_room_switch") },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    val switch = data.data ?: return
                    seatUserAdapter.notifyItemChanged(0, switch)
                }
            })
    }

    private var roomWishListPop:RoomWishListPop?=null
    private fun showWishListPop() {
        roomSourceBean.wishInfo?.apply {
            if (CommonUtils.isPopShow(roomWishListPop)){
                return
            }
            roomWishListPop = RoomWishListPop.showDialog(
                mContext,
                roomId,
                this,
                isOwner,
                object : RoomWishListPop.OnSendGiftListener {
                    override fun onSendGift(giftInfo: GiftInfo) {
                        startSendGift(giftInfo)
                    }
                })
        }
    }




    private fun startSendGift(item: GiftInfo) {
        val sendGiftRequest = SendGiftRequest()
        sendGiftRequest.roomId = roomId
        sendGiftRequest.toUid = roomSourceBean.ownerInfo?.userId
        sendGiftRequest.giftId = item.id
        sendGiftRequest.num = 1
        sendGiftRequest.source = SendGiftRequest.SOURCE_LIVE_ROOM
        sendGiftRequest.callId = 0
        sendGift(sendGiftRequest, item)
    }

    override fun onReceiveCmdMsg(it: EMMessage) {
        val source = it.getIntAttribute("source", -1)
         if(source == ChatConstant.ACTION_SET_WISH_SUCCESS){
            getRoomDetail()
        }
    }

    private fun sendGift(sendGiftRequest: SendGiftRequest, item: GiftInfo) {
        request2({ imChatRxApi.sendGift(sendGiftRequest) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    showToast("赠送成功")
                    VibrateUtils.vibrate(50)
                    val map = HashMap<String, Any>()
                    map["giftName"] = item.name
                    map["giftIcon"] = item.giftIcon
                    map["num"] = sendGiftRequest.num
                    map["svga"] = item.svga
                    sendGiftSuccess(item,roomSourceBean.ownerInfo!!)
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    if (code == ErrorCode.CODE_NO_BALANCE) {
                        ApplicationProxy.instance.showRechargePop(mContext, true)
                    }
                }
            })
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
        topTitleBinding.ivRoomCover.setOnSingleClickListener {
            showUserPop(roomSourceBean.ownerInfo!!.userId)
        }
        topTitleBinding.tvGroupMember.setOnSingleClickListener {
            showGroupMemberPop()
        }
        topTitleBinding.tvJoinGroup.setOnSingleClickListener {
            showJoinGroupPop()
        }
        mBinding.flTopView.addView(rankView)
    }

    override fun joinGroupSuccess() {
        topTitleBinding.tvGroupMember.visibility = View.VISIBLE
        topTitleBinding.tvJoinGroup.visibility = View.INVISIBLE
    }

    override fun refreshOnlineUser(onlineResponse: RoomOnlineResponse) {
        super.refreshOnlineUser(onlineResponse)
        topTitleBinding.tvOnlineNum.text = onlineResponse.onlineNum.toString()
    }


    override fun getRoomInfoSuccess() {
        super.getRoomInfoSuccess()
        topTitleBinding.roomInfo = roomSourceBean
        val wishInfo = roomSourceBean.wishInfo
        if (wishInfo != null) {
            (seatUserAdapter as ThreeRoomSeatAdapter).updateWishInfo(wishInfo)
            if (CommonUtils.isPopShow(roomWishListPop)){
                roomWishListPop?.refreshWishList(wishInfo)
            }
        }
        if (roomSourceBean.ifClubMember || roomSourceBean.ownerInfo?.userId == AppCacheManager.userId) {
            topTitleBinding.tvGroupMember.visibility = View.VISIBLE
            topTitleBinding.tvJoinGroup.visibility = View.INVISIBLE
        } else {
            topTitleBinding.tvGroupMember.visibility = View.INVISIBLE
            topTitleBinding.tvJoinGroup.visibility = View.VISIBLE
        }
        if (isOwner){
            checkExclusiveSwitch()
        }
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
                            ownerSwitchMikeAlert(!item.mikeUser, item.id, roomUserSeatInfo.userId)
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

                    cn.yanhu.agora.R.id.iv_rose -> {
                        val roomUserSeatInfo = item.roomUserSeatInfo ?: return
                        sendRose(roomUserSeatInfo)
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
        seatUserAdapter.addOnItemChildClickListener(
            cn.yanhu.agora.R.id.iv_rose,
            childItemClickListener
        )
    }

    private fun showSwitchRoomTypePop() {
        val content = Spans.builder()
            .text(if (roomSourceBean.isPrivateRoom()) "确定要转为大厅直播吗？" else "转为专属房间后只保留麦上的男女嘉宾，确认要转换吗？\n\n").size(
                CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.sp_14))
            .text(if (roomSourceBean.isPrivateRoom()) "" else "专属房需付费，男嘉宾同意才可转换，房间内禁止涉黄、涉政等违规行为").size(CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.sp_13))
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