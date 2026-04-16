package cn.yanhu.agora.ui.liveRoom.live

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import androidx.databinding.DataBindingUtil
import cn.yanhu.agora.adapter.liveRoom.ThreeRoomSeatAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.PkAgreeResponse
import cn.yanhu.agora.bean.PkContinueInfo
import cn.yanhu.agora.bean.PkInviteMsgInfo
import cn.yanhu.agora.bean.RefusePkInfo
import cn.yanhu.agora.databinding.ViewThreeRoomTopViewBinding
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.miniwindow.LiveRoomVideoMiniManager
import cn.yanhu.agora.pop.LiveRoomSeatManagerPop
import cn.yanhu.agora.pop.RoomWishListPop
import cn.yanhu.agora.pop.roomPk.ReceivePkInvitePop
import cn.yanhu.agora.pop.roomPk.SendPkPop
import cn.yanhu.agora.ui.liveRoom.view.OnClickSeatListener
import cn.yanhu.agora.ui.liveRoom.view.ThreeRoomPkSeatView
import cn.yanhu.agora.ui.liveRoom.view.ThreeRoomPkShowInfoView
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.logComToFile
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.api.commonRxApi
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.PkRoomResultInfo
import cn.yanhu.commonres.bean.RoomPkEnterInfo
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.bean.SendGiftRequest
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.manager.EmMsgManager
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ErrorCode
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.blankj.utilcode.util.VibrateUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.layoutmanager.QuickGridLayoutManager
import com.hyphenate.chat.EMMessage
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.core.BasePopupView
import io.agora.rtc2.IRtcEngineEventHandler
import kotlin.math.abs

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:三人房
 */
class ThreeLiveRoomFrg : BaseLiveRoomFrg() {
    override fun initData() {
        mBinding.rvSeat.layoutManager = QuickGridLayoutManager(mContext, 2)
        seatUserAdapter =
            ThreeRoomSeatAdapter()
        (seatUserAdapter as ThreeRoomSeatAdapter).onRoomItemClickListener =
            object : ThreeRoomSeatAdapter.OnRoomItemClickListener {
                override fun onClickWish() {
                    showWishListPop()
                }
            }
        addTopTitleView()
        super.initData()
        mBinding.rvSeat.adapter = seatUserAdapter
        val pkEnterInfo = roomSourceBean.pkRoomDetail
        if (pkEnterInfo != null) {
            pkRoomId = pkEnterInfo.pkRoomId
            pkUid = pkEnterInfo.pkUid
            val pkState = pkEnterInfo.pkState
            initRoomPkView()
            showPkStatusView(pkEnterInfo, pkState)
            getRoomPkSeatInfo();
            joinPkChannel()
        }
    }

    override fun setHasSeatUpStatus() {
        super.setHasSeatUpStatus()
        seatUserAdapter.notifyItemChanged(0, "showEnterAnim")
    }

    override fun setSeatOutSuccess() {
        super.setSeatOutSuccess()
        seatUserAdapter.notifyItemChanged(0, "hideEnterAnim")
    }

    private fun checkExclusiveSwitch() {
        request(
            { commonRxApi.getConfigInfo("exclusive_room_switch") },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    val switch = data.data ?: return
                    seatUserAdapter.notifyItemChanged(0, switch)
                }
            })
    }

    private var roomWishListPop: RoomWishListPop? = null
    private fun showWishListPop() {
        roomSourceBean.wishInfo?.apply {
            if (CommonUtils.isPopShow(roomWishListPop)) {
                return
            }
            startSendComboGift(chatRoomRoseGiftMsg)
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


    private var pkInvitePop: BasePopupView? = null
    private fun showInvitePkPop(message: EMMessage) {
        if (CommonUtils.isPopShow(pkInvitePop)) {
            return
        }
        val content =
            message.getStringAttribute(ChatConstant.CUSTOM_DATA)
        val inviteInfo = GsonUtils.fromJson(content, PkInviteMsgInfo::class.java)
        pkInvitePop = ReceivePkInvitePop.showDialog(
            mContext,
            inviteInfo,
            object : ReceivePkInvitePop.OnPkInviteListener {
                override fun agreePk() {
                    agreePk(inviteInfo)
                }

                override fun rejectPk() {
                    rejectPk(inviteInfo)
                }

            })
    }


    private var finishPkTime = 0L
    override fun onReceiveCmdMsg(it: EMMessage) {
        val source = it.getIntAttribute("source", -1)
        if (source == ChatConstant.ACTION_SET_WISH_SUCCESS) {
            getRoomDetail()
        } else if (source == ChatConstant.ACTION_LIVE_ROOM_PK_INVITE) {
            var delay = 0L
            if (LiveRoomVideoMiniManager.getInstance().isShowing) {
                delay = 500L
                LiveRoomVideoMiniManager.getInstance().closeFloat(2, false);
            }
            ThreadUtils.getMainHandler().postDelayed({
                showInvitePkPop(it)
            }, delay)
        } else if (source == ChatConstant.ACTION_PK_REJECT) {
            val content =
                it.getStringAttribute(ChatConstant.CUSTOM_DATA)
            val refuseInfo = GsonUtils.fromJson(content, RefusePkInfo::class.java)
            if (!refuseInfo.isRandomPk) {
                //不是随机邀请的 拒绝PK 提示
                showToast("对方拒绝了你的PK邀请")
                cancelPkMatchTime()
            }
            LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_PK_INFO).post(false)
        } else if (source == ChatConstant.ACTION_PK_AGREE) {
            //同意Pk 进入pk模式
            val content =
                it.getStringAttribute(ChatConstant.CUSTOM_DATA)
            val agreeResponse = GsonUtils.fromJson(content, PkAgreeResponse::class.java)
            showAgreePkResultView(agreeResponse)
        } else if (source == ChatConstant.ACTION_PK_ROOM_FINISH) {
            runOnUiThread {
                //pk结束
                finishPkTime = if (finishPkTime == 0L) {
                    System.currentTimeMillis()
                } else if (System.currentTimeMillis() - finishPkTime < 5000) {
                    return@runOnUiThread
                } else {
                    System.currentTimeMillis()
                }
                val content: String = it.getStringAttribute(ChatConstant.CUSTOM_DATA)
                val agreeResponse: PkRoomResultInfo =
                    GsonUtils.fromJson<PkRoomResultInfo?>(content, PkRoomResultInfo::class.java)

                if (agreeResponse.roomPkValue > agreeResponse.otherRoomPkValue) {
                    if (isOwner) {
                        sendPkNotice("PK已结束，恭喜红方获胜！")
                    }
                    roomSourceBean.pkSuccessCount = roomSourceBean.pkSuccessCount + 1
                    seatUserAdapter.notifyItemChanged(0,"refreshPkSuccessCount")
                } else if (agreeResponse.roomPkValue < agreeResponse.otherRoomPkValue) {
                    roomSourceBean.pkSuccessCount = 0
                    seatUserAdapter.notifyItemChanged(0,"refreshPkSuccessCount")
                    if (isOwner) {
                        sendPkNotice("PK已结束，恭喜蓝方获胜！")
                    }
                } else {
                    if (isOwner) {
                        sendPkNotice("PK已结束，双方平局！")
                    }
                }
                pkProgressView?.showResult(agreeResponse)
            }

        } else if (source == ChatConstant.ACTION_INVITE_CONTINUE_PK) {
            //收到继续PK
            val content: String = it.getStringAttribute(ChatConstant.CUSTOM_DATA)
            val agreeResponse =
                GsonUtils.fromJson<PkContinueInfo?>(content, PkContinueInfo::class.java)
            runOnUiThread {
                if (localStrUserId == agreeResponse.roomUserId) {
                    //被邀请方 显示邀请弹框
                    showContinuePkInviteDialog()
                }
                pkProgressView?.continuePk(agreeResponse)
            }

        } else if (source == ChatConstant.ACTION_SUBSCRIBE_AUDIO_PK) {
            val content: String = it.getStringAttribute(ChatConstant.CUSTOM_DATA)
            runOnUiThread {
                val item =
                    pkSeatView?.seatInfoList?.get(0)
                val isPkMikeUse = "0" != content
                item?.roomUserSeatInfo?.pkMikeUse = isPkMikeUse
                AgoraManager.getInstance().subScribeAudio(isPkMikeUse)
                showToast(if (isPkMikeUse) "主持已开启对方房间声音" else "主持已关闭对方房间声音")
            }
        } else if (source == ChatConstant.ACTION_PK_VALUE_CHANGE) {
            val content: String = it.getStringAttribute(ChatConstant.CUSTOM_DATA)
            val pkRoomResultInfo =
                GsonUtils.fromJson<PkRoomResultInfo?>(content, PkRoomResultInfo::class.java)
            runOnUiThread { pkProgressView?.setPkProgressValue(pkRoomResultInfo, false) }
        } else if (source == ChatConstant.ACTION_NO_INVITE_PK_ROOM) {
            showToast("暂无主播接受PK，请稍后再试")
            cancelPkMatchTime()
        } else if (source == ChatConstant.ACTION_REFRESH_PK_SEAT) {
            getRoomPkSeatInfo()
        } else if (source == ChatConstant.ACTION_AGREE_CONTINUE_PK) {
            //同意继续PK
            runOnUiThread { pkProgressView?.reStartPk(true) }
        } else if (source == ChatConstant.ACTION_PK_ROOM_END) {
            //PK房间断开连接
            runOnUiThread { this.updateSeatWhenPkEnd() }
        }
    }


    private var continuePkInvitePop: BasePopupView? = null
    private fun showContinuePkInviteDialog() {
        if (CommonUtils.isPopShow(continuePkInvitePop)) {
            return
        }
        continuePkInvitePop = DialogUtils.showConfirmDialog(
            "PK邀请",
            {
                pkProgressView?.continuePkAgree()
            },
            {
                pkProgressView?.breakPk()
            },
            content = "对方邀请你继续PK，是否接受？",
            cancel = "拒绝",
            confirm = "接受PK", isDismissOuTouchOutSide = false
        )
    }

    private fun updateSeatWhenPkEnd() {
        mBinding.isPk = false
        (seatUserAdapter as ThreeRoomSeatAdapter).setIsPk(false)
        pkProgressView?.cancel()
        AgoraManager.getInstance().leavePkChannel()
        mBinding.vgOtherSeat.removeAllViews()
        pkSeatView = null
        mBinding.vgRoomPk.removeAllViews()
        pkProgressView = null
        changeRvChatScroll()
    }

    private fun rejectPk(inviteInfo: PkInviteMsgInfo) {
        request(
            { agoraRxApi.rejectPk(roomId, inviteInfo.id.toInt(), 2) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                }
            })
    }

    private fun agreePk(inviteInfo: PkInviteMsgInfo) {
        request(
            { agoraRxApi.agreePk(roomId, inviteInfo.id.toInt(), 1) },
            object : OnRequestResultListener<PkAgreeResponse> {
                override fun onSuccess(data: BaseBean<PkAgreeResponse>) {
                    val data = data.data ?: return
                    EmMsgManager.sendCmdMessageToChatRoom(
                        roomSourceBean.uid, GsonUtils.toJson(data),
                        ChatConstant.ACTION_PK_AGREE
                    )
                    data.isInvite = true
                    showAgreePkResultView(data)
                }

            })
    }


    private var pkRoomId: String = ""
    private var pkUid: String? = ""
    private var pkAgoraToken: String = ""
    private var pkSeatView: ThreeRoomPkSeatView? = null
    private var pkProgressView: ThreeRoomPkShowInfoView? = null
    private fun showAgreePkResultView(pkAgreeResponse: PkAgreeResponse) {
        runOnUiThread(object : Runnable {
            override fun run() {
                pkRoomId = pkAgreeResponse.pkRoomId
                pkUid = pkAgreeResponse.pkUid
                pkAgoraToken = pkAgreeResponse.agoraToken

                initRoomPkView()

                pkProgressView?.startPK(pkAgreeResponse, roomSourceBean.ownerInfo!!.portrait, true)
                cancelPkMatchTime()
                joinPkChannel()
            }
        });
    }

    private fun initRoomPkView() {
        sendPkPop?.dismiss()
        mBinding.isPk = true
        (seatUserAdapter as ThreeRoomSeatAdapter).setIsPk(true)
        pkProgressView = ThreeRoomPkShowInfoView(mContext)
        pkProgressView?.isRoomOwner = isOwner

        pkProgressView?.onPkOperateListener =
            object : ThreeRoomPkShowInfoView.OnPkOperateListener {
                override fun onStartPk() {
                    sendPkNotice("PK开始啦！")
                }

            }
        mBinding.vgRoomPk.removeAllViews()
        mBinding.vgRoomPk.addView(pkProgressView)
        pkSeatView = ThreeRoomPkSeatView(mContext, pkRoomId)
        pkSeatView?.setOnClickSeatListener(object : OnClickSeatListener {
            override fun onChildClickListener(
                view: View,
                position: Int,
                item: RoomSeatInfo?
            ) {
                val pkMikeUse = item?.roomUserSeatInfo?.pkMikeUse == true
                if (isOwner) {
                    EmMsgManager.sendCmdMessageToChatRoom(
                        roomSourceBean.uid,
                        if (pkMikeUse) "0" else "1",
                        ChatConstant.ACTION_SUBSCRIBE_AUDIO_PK,
                        true
                    )
                    AgoraManager.getInstance().subScribeAudio(!pkMikeUse)
                    item?.roomUserSeatInfo?.pkMikeUse = !pkMikeUse
                    pkProgressView?.operatePkMikeUse(!pkMikeUse)
                    showToast(if (!pkMikeUse) "已开启对方房间声音" else "已关闭对方房间声音")
                } else {
                    showToast(if (pkMikeUse) "主持已开启对方房间声音" else "主持已关闭对方房间声音")
                }
            }
        })
        mBinding.vgOtherSeat.removeAllViews()
        mBinding.vgOtherSeat.addView(pkSeatView)
        AgoraManager.getInstance().subScribeAudio(true)
        ThreadUtils.getMainHandler().postDelayed({
            changeRvChatScroll()
        },50)
    }

    private fun joinPkChannel() {
        logComToFile(TAG, "进入PK房间：$pkRoomId")
        request({ agoraRxApi.getAgoraToken(pkRoomId) }, object : OnRequestResultListener<String> {
            override fun onSuccess(data: BaseBean<String>) {
                pkAgoraToken = data.data ?: return
                var duration = 0L
                if (AgoraManager.getInstance().mRtcEngine == null) {
                    duration = 200
                }

                ThreadUtils.getMainHandler().postDelayed({
                    val join = AgoraManager.getInstance().joinPkChannel(
                        localUserId,
                        pkRoomId,
                        pkAgoraToken,
                        isInSeatByUserId(localUserId),
                        object : IRtcEngineEventHandler() {
                            override fun onUserJoined(uid: Int, elapsed: Int) {
                                super.onUserJoined(uid, elapsed)
                                getRoomPkSeatInfo(true)
                            }


                            override fun onUserOffline(uid: Int, reason: Int) {
                                super.onUserOffline(uid, reason)
                                getRoomPkSeatInfo()
                            }

                            override fun onRemoteVideoStateChanged(
                                uid: Int,
                                state: Int,
                                reason: Int,
                                elapsed: Int
                            ) {
                                super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
                            }

                            override fun onRemoteVideoStats(stats: RemoteVideoStats?) {
                                super.onRemoteVideoStats(stats)
                            }
                        })
                    if (join != 0 && 27 != abs(join)) {
                        logComToFile(TAG, "进入PK失败joined = $join")
                        showToast("进入PK失败joined = $join")
                    }
                }, duration)

            }

        })
    }

    private fun getRoomPkSeatInfo(isReload: Boolean = false) {
        request(
            { agoraRxApi.getSeatList(pkRoomId) },
            object : OnRequestResultListener<MutableList<RoomSeatInfo>> {
                override fun onSuccess(data: BaseBean<MutableList<RoomSeatInfo>>) {
                    val roomSeatResList = data.data ?: return
                    if (roomSeatResList.isEmpty()) {
                        updateSeatWhenPkEnd()
                    } else {
                        pkSeatView?.setSeatList(roomSeatResList,isReload)
                    }

                }

            }, false
        )
    }

    private fun sendGift(sendGiftRequest: SendGiftRequest, item: GiftInfo) {
        request2(
            { imChatRxApi.sendGift(sendGiftRequest) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    showToast("赠送成功")
                    VibrateUtils.vibrate(50)
                    val map = HashMap<String, Any>()
                    map["giftName"] = item.name
                    map["giftIcon"] = item.giftIcon
                    map["num"] = sendGiftRequest.num
                    map["svga"] = item.svga
                    sendGiftSuccess(item, roomSourceBean.ownerInfo!!)
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
        request(
            { agoraRxApi.getInviteList(roomId, gender, "0", 1) },
            object : OnRequestResultListener<MutableList<UserDetailInfo>> {
                override fun onSuccess(data: BaseBean<MutableList<UserDetailInfo>>) {
                    val userList = data.data ?: return
                    if (CommonUtils.isPopShow(liveRoomUserListPop)) {
                        return
                    }
                    liveRoomUserListPop = LiveRoomSeatManagerPop.showDialog(
                        this@ThreeLiveRoomFrg,
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

    override fun exitGroupSuccess() {
        topTitleBinding.tvGroupMember.visibility = View.INVISIBLE
        topTitleBinding.tvJoinGroup.visibility = View.VISIBLE
    }

    override fun refreshOnlineUser(onlineNum: Int?) {
        super.refreshOnlineUser(onlineNum)
        if (onlineNum == null) {
            return
        }
        topTitleBinding.tvOnlineNum.text = onlineNum.toString()
    }

    override fun getRoomInfoSuccess() {
        super.getRoomInfoSuccess()
        topTitleBinding.roomInfo = roomSourceBean
        val wishInfo = roomSourceBean.wishInfo
        if (wishInfo != null) {
            (seatUserAdapter as ThreeRoomSeatAdapter).updateWishInfo(wishInfo)
            if (CommonUtils.isPopShow(roomWishListPop)) {
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
        if (roomSourceBean.isPrivateRoom()) {
            checkGiftSwitch()
        } else {
            mBinding.ivSendGift.visibility = View.VISIBLE
        }
        if (isOwner) {
            checkExclusiveSwitch()
        }


    }

    private fun showPkStatusView(pkEnterInfo: RoomPkEnterInfo, pkState: Int) {
        val pkAgreeResponse = PkAgreeResponse(
            false,
            pkEnterInfo.getPkId(),
            roomId,
            pkRoomId,
            pkUid,
            "",
            pkEnterInfo.getPkTime(),
            mutableListOf(),
            pkEnterInfo.getOtherRoomPortrait(),
            pkEnterInfo.countDownTime
        )
        pkProgressView?.pkInfo = pkAgreeResponse
        pkProgressView?.setPkProgressValue(pkEnterInfo, pkState == 1)
        if (pkState == 0) {
            //进行中
            pkProgressView?.startPK(
                pkAgreeResponse,
                pkEnterInfo.getNowRoomPortrait(),
                false
            )
        } else if (pkState == 1) {
            //本轮结束
            pkProgressView?.showPkInfo(pkEnterInfo)
            pkProgressView?.showResult(pkEnterInfo)
        } else {
            //已邀请 显示主持连线中
            pkProgressView?.showPkInfo(pkEnterInfo)
            val pkContinueInfo =
                PkContinueInfo(pkEnterInfo.getPkId(), pkEnterInfo.getNowRoomOwnerUserId())
            pkProgressView?.continuePk(pkContinueInfo)
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

                    cn.yanhu.agora.R.id.iv_avatar -> {
                        val roomUserSeatInfo = item.roomUserSeatInfo ?: return
                        showUserPop(roomUserSeatInfo.userId)
                    }

                    cn.yanhu.agora.R.id.iv_rose -> {
                        val roomUserSeatInfo = item.roomUserSeatInfo ?: return
                        checkSendGift(roomUserSeatInfo, roseGiftInfo!!)
                    }

                    cn.yanhu.agora.R.id.vg_autoSeat -> {
                        showSetAutoSeat()
                    }
                }
            }

        }

    override fun refreshAutoSeat() {
        super.refreshAutoSeat()
        (seatUserAdapter as ThreeRoomSeatAdapter).roomDetailInfo = roomSourceBean
        seatUserAdapter.notifyItemChanged(0, "updateToggleAuto")
    }

    private var sendPkPop: SendPkPop? = null
    override fun initListener() {
        super.initListener()

        LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_PK_INFO).observe(this) { o ->
            if (o is Boolean && o) {
                cancelPkMatchTime()
            }
        }
//        LiveEventBus.get<Boolean>(EventBusKeyConfig.AGREE_OR_REJECT_PK).observe(this, { o ->
//            if (o is PkAgreeResponse) {
//                //接受PK
//                agreePk(o as PkAgreeResponse)
//            } else if (o is Boolean) {
//                if (!o as Boolean?) {
//                    //不是随机邀请的 拒绝PK 提示
//                    ToastUtils.show("对方拒绝了你的PK邀请")
//                    cancelPkMatchTime()
//                }
//                LiveEventBus.get(EventBusKeyConfig.REFRESH_PK_INFO).post(false)
//            }
//        })
        mBinding.vgPk.setOnSingleClickListener {
            if (!CommonUtils.isPopShow(sendPkPop)) {
                sendPkPop =
                    SendPkPop.showDialog(mContext, roomId, object : SendPkPop.OnPkOperateListener {
                        override fun onSendInvitePk(isRandom: Boolean) {
                            var maxTime = 15
                            if (isRandom) {
                                maxTime = 60
                            }
                            mBinding.pkTimeProgress.setMaxNum(maxTime.toFloat())
                            mBinding.pkTimeProgress.visibility = View.VISIBLE
                            pkCountDownTime(maxTime)
                        }
                    })
            }


        }
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
            cn.yanhu.agora.R.id.vg_autoSeat, childItemClickListener
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

    private var pkRandomTimer: CountDownTimer? = null
    private fun pkCountDownTime(maxTime: Int) {
        mBinding.pkTimeProgress.setProgressNum(maxTime.toFloat(), 0)
        pkRandomTimer = object : CountDownTimer(1000L * maxTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val second = millisUntilFinished / 1000
                mBinding.pkTimeProgress.setProgressNum(second.toFloat(), 0)
            }

            override fun onFinish() {
                mBinding.pkTimeProgress.visibility = View.INVISIBLE
                if (maxTime == 60) {
                    showToast("暂无主播接受PK，请稍后再试")
                }
            }
        }
        pkRandomTimer?.start()
    }

    private fun cancelPkMatchTime() {
        if (pkRandomTimer != null) {
            pkRandomTimer!!.cancel()
            mBinding.pkTimeProgress.visibility = View.INVISIBLE
        }
    }

    private fun showSwitchRoomTypePop() {
        val content = if (roomSourceBean.isPrivateRoom()) {
            "确定要转为大厅直播吗？"
        } else {
            val spans = Spans.builder()
                .text("转为专属房间后只保留麦上的男女嘉宾，确认要转换吗？\n\n").size(
                    CommonUtils.getSpByDimen(com.zj.dimens.R.dimen.sp_15)
                )
                .text("温馨提示：")
                .color(
                    CommonUtils.getColor(
                        R.color.colorMain
                    )
                )
            if (roomSourceBean.exclusiveRoomPrice > 0) {
                spans.text("转专属房间")
                    .text("主持需支付${roomSourceBean.exclusiveRoomPrice}玫瑰/次")
                    .color(
                        CommonUtils.getColor(
                            R.color.colorMain
                        )
                    )
                    .text("，且")
            }

            spans.text("男嘉宾需支付${roomSourceBean.exclusiveSeatPrice}玫瑰/分钟")
                .color(
                    CommonUtils.getColor(
                        R.color.colorMain
                    )
                )
                .text("，男嘉宾同意后可转换成功。专属房超过15分钟没有男嘉宾上麦，将会自动关房")

            spans.build()
        }
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
        val type = if (roomSourceBean.isPrivateRoom()) 1 else 2
        mViewModel.switchRoomType(
            roomId,
            type.toString(),
            object : OnRequestResultListener<Boolean> {
                override fun onSuccess(data: BaseBean<Boolean>) {
                    if (type == 1) {
                        roomType = type
                        roomSourceBean.roomType = type
                        EmMsgManager.sendCmdMessageToChatRoom(
                            roomSourceBean.uid, "", ChatConstant.ACTION_MSG_SWITCH_TYPE_PLAZA
                        )
                        mBinding.ivSendGift.visibility = View.VISIBLE
                        showToast("房间已切换为大厅")
                    } else {
                        showToast("已发送消息至男嘉宾，男嘉宾同意后可转至专属房间")
                    }
                }

                override fun onFail(code: Int?, msg: String?) {
                    if (code == ErrorCode.CODE_NO_BALANCE) {
                        showRechargePop()
                    }
                }
            })
    }

    private fun checkGiftSwitch() {
        request(
            { commonRxApi.getConfigInfo("private_room_gift_switch") },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    val result = data.data ?: return
                    if (result == "0") {
                        mBinding.ivSendGift.visibility = View.VISIBLE
                    } else {
                        mBinding.ivSendGift.visibility = GONE
                    }
                }

            })
    }

    override fun getRoomSeatSuccess(seatList: MutableList<RoomSeatInfo>) {
        seatUserAdapter.items.forEach {
            seatList.forEach { roomSeatInfo ->
                if (it.roomUserSeatInfo?.userId == roomSeatInfo.roomUserSeatInfo?.userId) {
                    roomSeatInfo.pkStatus = it.pkStatus
                }
            }
        }
        seatUserAdapter.submitList(seatList)
    }

    override fun refreshSeatMicStatus(seatPosition: Int, mickUser: Boolean) {
        seatUserAdapter.getItem(seatPosition)?.mikeUser = mickUser
    }

    override fun refreshSeatInfo(it: MutableList<RoomSeatInfo>, uid: Int) {
        val items = seatUserAdapter.items
        if (items.isEmpty()) {
            return
        }
        ThreadUtils.getMainHandler().post {
            for (i in 0 until it.size) {
                val seatInfo = it[i]
                if (seatInfo.roomUserSeatInfo?.userId?.toInt() == uid) {
                    seatUserAdapter.safeUpdateItem(
                        items as MutableList,
                        i,
                        seatInfo
                    )
                    logComToFile(TAG, "更新麦位信息成功userId=$uid，position=$i")
                    if (isOwner) {
                        getPkSeatUserList()
                    }
                    return@post
                }
            }
        }
    }

    override fun updateSeatRoseInfo() {
        for (i in 0 until seatList.size) {
            val seatInfo = seatList[i]
            val roomUserSeatInfo = seatInfo.roomUserSeatInfo
            if (roomUserSeatInfo != null) {
                val item = seatUserAdapter.getItem(i)
                if (item != null) {
                    item.roomUserSeatInfo?.roseNum = roomUserSeatInfo.roseNum
                    item.roomUserSeatInfo?.userList = roomUserSeatInfo.userList
                    seatUserAdapter.notifyItemChanged(i, true)
                }
            }
        }
    }

    override fun userLeaveChanged(uid: Int) {
        ThreadUtils.getMainHandler().post {
            for (i in 0 until seatUserAdapter.items.size) {
                val item = seatUserAdapter.getItem(i) ?: break
                if (item.roomUserSeatInfo?.userId?.toInt() == uid) {
                    if (roomSourceBean.ownerInfo?.userId != uid.toString()) {
                        item.roomUserSeatInfo = null
                    }
                    seatUserAdapter.notifyItemChanged(i)
                }
            }
            seatList = seatUserAdapter.items.toMutableList()
        }
    }

    override fun userVideoStatusChanged(uid: Int, isShowPreload: Boolean) {
        var list = seatUserAdapter.items
        list.forEach {
            if (it.roomUserSeatInfo?.userId?.toInt() == uid) {
                if (isShowPreload) {
                    if (networkType == 1) {
                        it.ifLeave = true
                    }
                } else {
                    it.ifLeave = false
                }
                return
            }
        }
    }

    override fun userNetChanged(uid: String, ifNetDisConnect: Boolean) {
        var list = seatUserAdapter.items
        list.forEach {
            if (it.roomUserSeatInfo?.userId == uid && it.ifNetDisConnect != ifNetDisConnect) {
                it.ifNetDisConnect = ifNetDisConnect
                return
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        cancelPkMatchTime()
    }

}


