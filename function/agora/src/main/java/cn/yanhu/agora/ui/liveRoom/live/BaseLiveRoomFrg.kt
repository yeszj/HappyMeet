package cn.yanhu.agora.ui.liveRoom.live

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.fragment.app.FragmentActivity
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.UserEnterAdapter
import cn.yanhu.agora.adapter.liveRoom.LiveRoomChatMessageAdapter
import cn.yanhu.agora.adapter.liveRoom.MoreSeatRoomAdapter
import cn.yanhu.agora.adapter.liveRoom.ThreeRoomSeatAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.AngleRankInfo
import cn.yanhu.agora.bean.AngleRoomResultInfo
import cn.yanhu.agora.bean.ChatRoomMsgInfo
import cn.yanhu.agora.bean.GiftMsgInfo
import cn.yanhu.agora.bean.InviteSeatRecord
import cn.yanhu.agora.bean.RoomExtraInfo
import cn.yanhu.agora.bean.RoomLeaveResponse
import cn.yanhu.agora.bean.RoomOnlineResponse
import cn.yanhu.agora.databinding.FrgBaseLiveRoomBinding
import cn.yanhu.agora.listener.IRtcEngineEventHandlerListener
import cn.yanhu.agora.listener.OnSendSeatInviteListener
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.agora.manager.dbCache.InviteRecordCacheManager
import cn.yanhu.agora.miniwindow.LiveRoomVideoMiniManager
import cn.yanhu.agora.miniwindow.MiniWindowManager
import cn.yanhu.agora.pop.AdminCloseRoomReasonPop
import cn.yanhu.agora.pop.AdminStickyRoomPop
import cn.yanhu.agora.pop.CrownedUserListPop
import cn.yanhu.agora.pop.LiveRoomOnlineUserPop
import cn.yanhu.agora.pop.LiveRoomSeatManagerPop
import cn.yanhu.agora.pop.ReceiveInviteSeatPop
import cn.yanhu.agora.pop.RoomAngleResultPop
import cn.yanhu.agora.pop.SeatUserOperatePop
import cn.yanhu.agora.pop.SendMessagePop
import cn.yanhu.agora.pop.ToolDialog
import cn.yanhu.agora.queuetask.ApplySeatTask
import cn.yanhu.agora.ui.liveRoom.LiveRoomViewModel
import cn.yanhu.baselib.anim.AnimManager
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.queue.TaskQueueManagerImpl
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.ChatRoomGiftMsg
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.GiftSendModel
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.bean.SendGiftRequest
import cn.yanhu.commonres.bean.StickyInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.CmdMsgTypeConfig
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.ImMessageParamsConfig
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.manager.ServiceConfigKeyManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.task.GiftPopAnimTask
import cn.yanhu.commonres.utils.PermissionXUtils
import cn.yanhu.commonres.view.GiftFrameLayout
import cn.yanhu.imchat.db.ChatUserInfoManager
import cn.yanhu.imchat.manager.CutLiveRoomUtils
import cn.yanhu.imchat.manager.EmMsgManager
import cn.yanhu.imchat.manager.ImUserManager
import cn.yanhu.imchat.pop.ChatListDialog
import cn.yanhu.imchat.pop.SendGiftPop
import cn.zj.netrequest.OnRoomLeaveListener
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.application.OnImLoginListener
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ErrorCode
import com.alibaba.android.arouter.utils.TextUtils
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.chad.library.adapter4.BaseMultiItemAdapter
import com.chad.library.adapter4.BaseQuickAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hyphenate.EMCallBack
import com.hyphenate.EMChatRoomChangeListener
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMChatRoom
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMTextMessageBody
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.yhao.floatwindow.PermissionListener
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import org.json.JSONObject


/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
open class BaseLiveRoomFrg : BaseFragment<FrgBaseLiveRoomBinding, LiveRoomViewModel>(
    R.layout.frg_base_live_room, LiveRoomViewModel::class.java
), IRtcEngineEventHandlerListener, EMChatRoomChangeListener {

    protected var roomId: String = ""
    protected var roomType: Int = 0
    private val chatRoomMsgAdapter by lazy { LiveRoomChatMessageAdapter() }
    private var messageDialog: SendMessagePop? = null
    private var currentUser: BaseUserInfo? = null
    private var isShowEmoji = false
    lateinit var roomSourceBean: RoomDetailInfo

    var isOwner: Boolean = false
    protected var localUserId: Int = 0
    var seatList = mutableListOf<RoomSeatInfo>()
    lateinit var seatUserAdapter: BaseMultiItemAdapter<RoomSeatInfo>
    private var seatHeight = 0
    private var sevenSeatHeight = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_142) * 3
    private var nineSeatHeight = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_360)
    private var nineSongSeatHeight = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_80) * 5
    private var sevenSongSeatHeight = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_120) * 4

    protected var hasExpand = false
    override fun initData() {
        initPageData()
        seatList = roomSourceBean.roomSeatResList
        hasExpand = seatList.indexOfFirst { it.isExpand } >= 0
        setRvSeatHeight()
        setRvChatMessageTop(false)
        initSeatStatus()

        AgoraManager.getInstance().setRtcEngineEventHandlerListener(this)

        mBinding.roomInfo = roomSourceBean
        getRoomInfoSuccess()
        getRoomSeatSuccess(seatList)
        setSeatApplyStatus()
        initRvChatTop()
        initChatMsgAdapterData()
        setUserEnterAdapter()
        joinChatRoom()
        joinChannel()
        requestData()
        setUnReadMsgCount()
        getOnlineUser()
        if (roomSourceBean.isSongRoom()) {
            getRoomExtraInfo()
        }
    }

    private fun getRoomExtraInfo() {
        mViewModel.getExtraInfo(roomId, object : OnRequestResultListener<RoomExtraInfo> {
            override fun onSuccess(data: BaseBean<RoomExtraInfo>) {
                val extraInfo = data.data
                mBinding.tvSongNum.text = extraInfo?.songCountDesc
            }
        })
    }

    private fun setRvSeatHeight() {
        when (roomType) {
            RoomListBean.TYPE_SEVEN_FRIEND, RoomListBean.TYPE_SEVEN_ANGLE -> {
                seatHeight = sevenSeatHeight
            }

            RoomListBean.TYPE_SEVEN_SONG -> {
                seatHeight = if (hasExpand) {
                    sevenSongSeatHeight
                } else {
                    sevenSeatHeight
                }
            }

            RoomListBean.TYPE_NINE_SONG -> {
                seatHeight = if (hasExpand) {
                    nineSongSeatHeight
                } else {
                    nineSeatHeight
                }
            }

            RoomListBean.TYPE_NINE_FRIEND, RoomListBean.TYPE_NINE_ANGLE -> {
                seatHeight = nineSeatHeight
            }

            RoomListBean.FRG_THREE_ROOM -> {
                seatHeight =
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_360) + CommonUtils.getDimension(
                        com.zj.dimens.R.dimen.dp_32
                    )
            }
        }
        // ViewUtils.setViewHeight(mBinding.rvSeat,seatHeight)
        mBinding.rvSeat.isNestedScrollingEnabled = false

    }

    private fun initPageData() {
        localUserId = AppCacheManager.userId.toInt()
        roomSourceBean = requireArguments().getSerializable(IntentKeyConfig.DATA) as RoomDetailInfo
        mBinding.rvSeat.itemAnimator?.changeDuration = 0
        mBinding.rvChat.itemAnimator?.changeDuration = 0
        mBinding.rvChat.adapter = chatRoomMsgAdapter
        roomId = roomSourceBean.roomId.toString()
        roomType = roomSourceBean.roomType
        isOwner = roomSourceBean.ownerInfo?.userId == AppCacheManager.userId
    }

    private fun initSeatStatus() {
        if (isOwner) {
            mBinding.ivSeatStatus.visibility = View.INVISIBLE
            mBinding.vgApplySeat.visibility = View.INVISIBLE
        } else {
            if (roomSourceBean.getFragmentType() == RoomListBean.FRG_THREE_ROOM) {
                mBinding.ivSeatStatus.visibility = View.VISIBLE
                mBinding.vgApplySeat.visibility = View.INVISIBLE
            } else {
                mBinding.ivSeatStatus.visibility = View.INVISIBLE
                mBinding.vgApplySeat.visibility = View.VISIBLE
            }
        }
    }

    private fun initChatMsgAdapterData() {
        val list: MutableList<ChatRoomMsgInfo> = mutableListOf()
        list.add(
            ChatRoomMsgInfo(
                ChatRoomMsgInfo.ITEM_SYSTEM_TYPE,
                "平台公示：平台只提供交友介绍认识的服务，我们提倡文明直播、积极阳光交友，严禁涉黄、涉政、涉恐、低俗、辱骂等行为。发现违规行为将被封禁。保护网络绿色环境，从你我做起。",
                null
            )
        )
        chatRoomMsgAdapter.submitList(list)
    }

    protected var onlineUserList: MutableList<UserDetailInfo> = mutableListOf()

    /**
     * 获取在线用户
     */
    protected fun getOnlineUser() {
        getRoseRankList()
        request({ agoraRxApi.getOnlineUserList(roomSourceBean.roomId, 1) },
            object : OnRequestResultListener<RoomOnlineResponse> {
                override fun onSuccess(data: BaseBean<RoomOnlineResponse>) {
                    val onlineResponse = data.data ?: return
                    refreshOnlineUser(onlineResponse)
                    onlineUserList = onlineResponse.onlineUsers
                    if (CommonUtils.isPopShow(onlineUserListPop)) {
                        onlineUserListPop?.refreshOnlineUser(onlineUserList)
                    }
                }
            })
    }

    open fun refreshOnlineUser(onlineResponse: RoomOnlineResponse) {}

    private var onlineUserListPop: LiveRoomOnlineUserPop? = null

    /**
     * 在线用户列表弹框
     */
    protected fun showOnlineUserList() {
        if (CommonUtils.isPopShow(onlineUserListPop)) {
            return
        }
        onlineUserListPop = LiveRoomOnlineUserPop.showDialog(
            mContext, onlineUserList, roomSourceBean, inviteSeatListener
        )
    }


    val inviteSeatListener = object : OnSendSeatInviteListener {
        override fun onSendInvite(map: MutableMap<String, Any>, userInfo: UserDetailInfo) {
            val inviteRecord =
                InviteRecordCacheManager.getInviteRecordByUserId(userInfo.userId)

            if (inviteRecord != null) {
                val inviteTime = inviteRecord.inviteTime
                val currentTimeMillis = System.currentTimeMillis()
                val differTime = currentTimeMillis - inviteTime
                if (differTime < 15 * 60 * 1000) {
                    //主持人邀请同1个用户，15分钟最多邀请1次
                    showToast("已经邀请过啦^_^")
                    return
                }
            }

            val needSeatId = getNeedSeatId()
            if (needSeatId == -1) {
                showToast("暂无可用座位")
                return
            }
            map["seatId"] = needSeatId.toString()
            map["roomId"] = roomId
            map["roomInfo"] = GsonUtils.toJson(roomSourceBean)
            EmMsgManager.sendCmdMessagePeople(
                userInfo.userId, ChatConstant.ACTION_MSG_SET_UP, map
            )
            val inviteSeatRecord = InviteSeatRecord(userInfo.userId, System.currentTimeMillis())
            InviteRecordCacheManager.saveInviteRecord(inviteSeatRecord)
            showToast("上麦邀请已发送")
        }

        override fun onClickUser(userId: String) {
            showUserPop(userId)
        }
    }

    /**
     * im未读消息数
     */
    private fun setUnReadMsgCount() {
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<Int>() {
            override fun onSuccess(unreadMessageCount: Int) {
                if (unreadMessageCount > 0) {
                    val count = if (unreadMessageCount > 99) {
                        "99+"
                    } else {
                        unreadMessageCount.toString()
                    }
                    mBinding.tvChatUnReadCount.text = count
                    mBinding.tvChatUnReadCount.visibility = View.VISIBLE
                } else {
                    mBinding.tvChatUnReadCount.visibility = View.INVISIBLE
                }
            }

            override fun doInBackground(): Int {
                return try {
                    return EMClient.getInstance().chatManager().unreadMessageCount

                } catch (e: Exception) {
                    -1
                }
            }
        })
    }

    /**
     * 设置申请上麦按钮状态
     */
    private fun setSeatApplyStatus() {
        if (isInSeatByUserId(AppCacheManager.userId.toInt())) {
            setHasSeatUpStatus()
        } else {
            setSeatStatus()
        }
    }

    private fun joinChannel() {
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<Int>() {
            override fun onSuccess(joinChannel: Int) {
                if (joinChannel == 0) {
                    getRoomSeatSuccess(roomSourceBean.roomSeatResList)
                } else {
                    showToast("加入直播间失败，请重新尝试进入直播间")
                    leaveRoomFinish()
                }
            }

            override fun doInBackground(): Int {
                return try {
                    preJoinRoom()
                } catch (e: Exception) {
                    -1
                }
            }
        })
    }

    private fun setUserEnterAdapter() {
        mBinding.userEnterView.setRewardAnimAdapter(
            UserEnterAdapter(mContext, object : UserEnterAdapter.OnClickUserListener {
                override fun onClickUser(userId: String) {
                }
            })
        )
        if (!roomSourceBean.isAdmin()) {
            addUserEnterAnim(ImUserManager.getSelfUserInfo())
        }

    }

    private fun isFreeSeat(): Boolean {
        return if (AppCacheManager.isMan()) {
            !seatList[1].seatUserRose
        } else {
            !seatList[2].seatUserRose
        }
    }

    private var seatStatus = 0
    override fun initListener() {
        chatRoomMsgAdapter.addOnItemChildClickListener(
            R.id.userAvatar,
            object : BaseQuickAdapter.OnItemChildClickListener<ChatRoomMsgInfo> {
                override fun onItemClick(
                    adapter: BaseQuickAdapter<ChatRoomMsgInfo, *>, view: View, position: Int
                ) {
                    val item = chatRoomMsgAdapter.getItem(position) ?: return
                    item.sendUserInfo?.apply {
                        showUserPop(
                            this.userId
                        )
                    }
                }
            })
        chatRoomMsgAdapter.addOnItemChildLongClickListener(R.id.userAvatar,
            object : BaseQuickAdapter.OnItemChildLongClickListener<ChatRoomMsgInfo> {
                override fun onItemLongClick(
                    adapter: BaseQuickAdapter<ChatRoomMsgInfo, *>, view: View, position: Int
                ): Boolean {
                    val item = chatRoomMsgAdapter.getItem(position) ?: return true
                    showInputDialog(item.sendUserInfo, true)
                    return true
                }

            })
        mBinding.ivMessage.setOnSingleClickListener {
            ChatListDialog.showDialog(mContext)
        }
        mBinding.ivSeatStatus.setOnSingleClickListener {
            clickSeatBtn()
        }
        mBinding.vgApplySeat.setOnSingleClickListener {
            clickSeatBtn()
        }

        mBinding.bgInput.setOnSingleClickListener {
            showInputDialog(null, true)
        }
        mBinding.ivExtension.setOnSingleClickListener {
            showInputDialog(null, false)
        }
        mBinding.ivSendGift.setOnSingleClickListener {
            val roomUserSeatInfo = roomSourceBean.roomSeatResList[0].roomUserSeatInfo!!
            showSendGiftPop(roomUserSeatInfo)
        }
        LiveEventBus.get<EMMessage>(EventBusKeyConfig.RECEIVE_CMD_MSG).observe(this) {
            dealCmdMsg(it)
        }
        LiveEventBus.get<MutableList<EMMessage>>(EventBusKeyConfig.RECEIVE_CHAT_MSG).observe(this) {
            setUnReadMsgCount()
            updateReceivedMsg(it)
        }
        mBinding.toggleAutoSeat.setOnSingleClickListener {
            showSetAutoSeat()

        }
        mBinding.ivSetting.setOnSingleClickListener {
            showToolDialog()
        }
        registerNetChange()
    }

    protected fun showSetAutoSeat(): BasePopupView {
        val autoSeat = !roomSourceBean.autoSeat
        return DialogUtils.showConfirmDialog(
            if (autoSeat) "开启自动上麦" else "关闭自动上麦",
            {
                autoSeat(autoSeat)
                autoSeat.also {
                    roomSourceBean.autoSeat = it
                    refreshAutoSeat()
                }
            },
            {},
            if (autoSeat) "用户申请上麦后将自动同意，是否确认开启？" else "关闭后，用户需申请才可上麦"
        )
    }

    protected open fun refreshAutoSeat() {}

    private fun registerNetChange() {
        LiveEventBus.get<Boolean>(EventBusKeyConfig.CHECK_NET).observe(this) {
            if (networkType == 1) {
                showToast("网络良好")
            } else {
                showToast("wi-Fi/移动网络状况不佳，可能会影响直播效果，请检查您的网络")
            }
        }
    }

    private fun showToolDialog() {
        val toolDialog = ToolDialog()
        toolDialog.setLiveRoomInfo(roomSourceBean, object : ToolDialog.OnClickListener {
            override fun onClickWarning() {
                showInputWarningPop()
            }

            override fun onClickClose() {
                showAdminClosePop()
            }

            override fun onStickyRoom() {
                showAdminStickyPop()

            }
        })
        toolDialog.show(mContext.supportFragmentManager, "show_tool")
    }

    private var adminStickyRoomPop: AdminStickyRoomPop? = null
    private fun showAdminStickyPop() {
        request({ agoraRxApi.getConfigInfo(ServiceConfigKeyManager.KEY_ROOM_TOP_TIMES) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    val content = data.data
                    val contentList = GsonUtils.fromJson<MutableList<StickyInfo>>(
                        content, object : TypeToken<List<StickyInfo>>() {}.type
                    )
                    if (CommonUtils.isPopShow(adminStickyRoomPop)) {
                        return
                    }
                    adminStickyRoomPop =
                        AdminStickyRoomPop.showDialog(mContext, roomId, contentList)
                }
            })
    }

    private var adminCloseRoomReasonPop: AdminCloseRoomReasonPop? = null
    private fun showAdminClosePop() {
        if (CommonUtils.isPopShow(adminCloseRoomReasonPop)) {
            return
        }
        adminCloseRoomReasonPop = AdminCloseRoomReasonPop.showDialog(mContext,
            roomSourceBean.closeReasons,
            object : AdminCloseRoomReasonPop.OnCloseRoomListener {
                override fun onClose(reason: String) {
                    mViewModel.closeRoom(roomId, reason, roomSourceBean.uuid)
                }
            })
    }

    private fun showInputWarningPop() {
        DialogUtils.showAsInputConfirmDialog("发送警告", {
            if (it.isEmpty()) {
                showToast("警告内容不可为空")
                return@showAsInputConfirmDialog
            }
            val map = java.util.HashMap<String, Any>()
            map[ImMessageParamsConfig.KEY_WARNING_CONTENT] = it
            val userId: String = roomSourceBean.ownerInfo!!.userId
            EmMsgManager.sendCmdMessagePeople(userId, ChatConstant.ACTION_MSG_ADMIN_ALERT, map)
            showToast("已发送警告")
            mViewModel.saveRoomWarnRecord(userId, roomId, it, 1)
        }, {}, hint = "请输入警告内容")
    }

    private var sendGiftPop: SendGiftPop? = null
    protected fun showSendGiftPop(roomUserSeatInfo: UserDetailInfo) {
        if (CommonUtils.isPopShow(sendGiftPop)) {
            return
        }
        roomUserSeatInfo.roomId = roomId.toInt()
        sendGiftPop = SendGiftPop.showDialog(mContext,
            roomUserSeatInfo,
            SendGiftRequest.SOURCE_LIVE_ROOM,
            0,
            object : SendGiftPop.OnSendGiftListener {
                override fun onSendGift(item: GiftInfo) {
                    sendGiftSuccess(item, roomUserSeatInfo)
                }

                override fun onShowUserInfo(userId: String) {
                    showUserPop(userId)
                }
            })
    }

    protected fun sendGiftSuccess(
        item: GiftInfo, roomUserSeatInfo: UserDetailInfo
    ) {
        LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_USER_INFO)
            .post(true)
        playSvga(item, ImUserManager.getSelfUserInfo())
        val giftMsgInfo = GiftMsgInfo(item, roomUserSeatInfo)
        sendMessage(GsonUtils.toJson(giftMsgInfo), ChatRoomMsgInfo.ITEM_GIFT_TYPE)
    }


    private var isOnNewIntent = false
    fun onNewIntent(intent: Intent) {
        isOnNewIntent = true
        logcom("重新进入")
        if (intent.getBooleanExtra("isFinish", false)) {
            showOffDialog()
        }
        closeMiniWindow()
        addHostSurfaceView()
    }


    protected open fun addHostSurfaceView() {
        if (roomSourceBean.isSongRoom()) {
            return
        }
        seatUserAdapter.notifyItemChanged(0)
    }

    private fun closeMiniWindow() {
        if (LiveRoomVideoMiniManager.getInstance().isShowing) {
            LiveRoomVideoMiniManager.getInstance().closeMiniWindow()
        }
    }

    private fun showOffDialog() {
        DialogUtils.showConfirmDialog("关闭直播", {
            closeRoom()
        }, {

        }, "确定要关闭直播吗？", context = mContext)
    }

    private fun clickSeatBtn() {
        when (seatStatus) {
            0 -> {
                userSetSeat(if (roomSourceBean.autoSeat) SEAT_TYPE_AUTO else SEAT_TYPE_APPLY)
            }

            1 -> {
                showToast("已申请，等待房主同意")
            }

            else -> {
                //下麦
                userDownSeat()
            }
        }
    }

    protected fun setHasSeatUpStatus() {
        seatStatus = 2
        mBinding.ivSeatStatus.setImageResource(R.drawable.ic_down_seat)
        mBinding.tvApply.text = "下麦"
        mBinding.tvRoseSeatNum.visibility = View.GONE
    }

    protected fun setHasApplyStatus() {
        mBinding.ivSeatStatus.setImageResource(R.drawable.ic_already_seat)
        mBinding.tvApply.text = "已申请上麦"
        mBinding.tvRoseSeatNum.text = "排队中"
        mBinding.tvRoseSeatNum.visibility = View.VISIBLE
        seatStatus = 1
    }


    private fun autoSeat(autoSeat: Boolean) {
        mViewModel.autoSeat(roomId,
            (if (autoSeat) 1 else 0).toString(),
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    logcom("开启/关闭自动上麦")
                    roomSourceBean.autoSeat = autoSeat
                    refreshAutoSeat()
                    //需要透传消息提示观众房主已开启自动上麦/关闭自动上麦
                    if (autoSeat) {
                        EmMsgManager.sendCmdMessageToChatRoom(
                            roomSourceBean.uid, "", ChatConstant.ACTION_MSG_OPEN_AUTO_SEAT
                        )
                    } else {
                        EmMsgManager.sendCmdMessageToChatRoom(
                            roomSourceBean.uid, "", ChatConstant.ACTION_MSG_CLOSE_AUTO_SEAT
                        )
                    }
                }
            })
    }

    //获取悬浮窗权限
    open fun showFloatWindow(type: Int) {
        if (type == 1 && !isInSeatByUserId(AppCacheManager.userId.toInt())) {
            //如果是点击关闭或者物理键盘返回且不在麦位上直接关闭房间
            closeRoom()
        } else {
            PermissionXUtils.checkAlertPermission(mContext,
                "关闭房间",
                object : PermissionXUtils.OnAlertPermissionListener {
                    override fun onSuccess() {
                        doShowFloatWindow()
                    }

                    override fun onFail() {}
                    override fun onClose() {
                        closeRoom()
                    }
                })
        }
    }

    private fun closeRoom() {
        leave()
    }


    private var roomAngleResultPop: RoomAngleResultPop? = null
    private var angelAnimList = mutableListOf<AngleRoomResultInfo>()
    private fun showAngleResultTip(angleResultInfo: AngleRoomResultInfo, type: Int) {
        val angleUser = angleResultInfo.angelInfo
        val guardUser = angleResultInfo.guardInfo
        if (angleUser == null && guardUser == null) {
            leave()
        } else {
            if (CommonUtils.isPopShow(roomAngleResultPop)) {
                angelAnimList.add(angleResultInfo)
                return
            }
            roomAngleResultPop = RoomAngleResultPop.showDialog(
                mContext,
                angleUser,
                guardUser,
                type,
                object : SimpleCallback() {
                    override fun onDismiss(popupView: BasePopupView?) {
                        super.onDismiss(popupView)
                        if (angelAnimList.size > 0) {
                            val get = angelAnimList[0]
                            showAngleResultTip(get, type)
                            angelAnimList.removeAt(0)
                        }
                    }
                })
            if (isOwner) {
                EmMsgManager.sendCmdMessageToChatRoom(
                    roomSourceBean.uid,
                    GsonUtils.toJson(angleResultInfo),
                    ChatConstant.ACTION_ANGLE_ROOM_RESULT
                )
            }
        }
    }

    private var crownedUserListPop: CrownedUserListPop? = null
    protected fun showCrownedListPop(type: Int) {
        mViewModel.getFitAngelRankList(
            roomId,
            object : OnRequestResultListener<MutableList<AngleRankInfo>> {
                override fun onSuccess(data: BaseBean<MutableList<AngleRankInfo>>) {
                    if (CommonUtils.isPopShow(crownedUserListPop)) {
                        return
                    }
                    crownedUserListPop = CrownedUserListPop.showDialog(
                        mContext, data.data!!, object : CrownedUserListPop.OnClickCrownedListener {
                            override fun onCrowned(angleResultInfo: AngleRoomResultInfo) {
                                showAngleResultTip(angleResultInfo, type)
                            }
                        }, roomId, type
                    )
                }
            })
    }


    private fun leave() {
        if (isOwner) {
            isOwnerClose = true
        }
        roomLeave()
    }

    private fun doShowFloatWindow() {
        val surfaceView = if (roomSourceBean.isSongRoom()) {
            SongLiveRoomFrg.getOwnerSurfaceView(owenId = roomSourceBean.ownerInfo!!.userId)
        } else {
            val findViewHolderForAdapterPosition =
                seatUserAdapter.recyclerView.findViewHolderForAdapterPosition(0)
            when (findViewHolderForAdapterPosition) {
                is ThreeRoomSeatAdapter.VH -> {
                    findViewHolderForAdapterPosition.binding.anchorSeatInfo.itemVideoSf.tag as View?
                }

                is MoreSeatRoomAdapter.VH -> {
                    findViewHolderForAdapterPosition.binding.anchorSeatInfo.itemVideoSf.tag as View?
                }

                is MoreSeatRoomAdapter.VH2 -> {
                    findViewHolderForAdapterPosition.binding.itemVideoSf.tag as View?
                }

                else -> {
                    null
                }
            }
        }

        surfaceView?.apply {
            LiveRoomVideoMiniManager.getInstance()
                .show(mContext, 2, roomSourceBean.ownerInfo, object : PermissionListener {
                    override fun onSuccess() {
                        MiniWindowManager.switchLiveToMiniFloat(mContext)
                    }

                    override fun onFail() {}
                }, this)
        }
    }

    //更新接受消息数据
    open fun updateReceivedMsg(messages: List<EMMessage>) {
        val list: ArrayList<ChatRoomMsgInfo> = ArrayList()
        for (message in messages) {
            if (message.conversationId() != roomSourceBean.uid) {
                break
            }
            if (message.body is EMTextMessageBody) {
                val content = (message.body as EMTextMessageBody).message
                val sendType = message.getIntAttribute(ChatConstant.CUSTOM_SEND_ATYPE)
                val userInfo = message.getStringAttribute(ChatConstant.CUSTOM_SEND_USER_INFO)
                val sendUserInfo = GsonUtils.fromJson(userInfo, UserDetailInfo::class.java)
                val altInfo = message.getStringAttribute(ChatConstant.ATE_USER_INFO, "")
                var altUserInfo: BaseUserInfo? = null
                if (!TextUtils.isEmpty(altInfo)) {
                    altUserInfo = GsonUtils.fromJson(altInfo, BaseUserInfo::class.java)
                }
                val chatRoomMsgInfo = ChatRoomMsgInfo(sendType, content, sendUserInfo, altUserInfo)
                list.add(chatRoomMsgInfo)
                if (sendType == ChatRoomMsgInfo.ITEM_GIFT_TYPE) {
                    val fromJson = GsonUtils.fromJson(content, ChatRoomGiftMsg::class.java)
                    runOnUiThread {
                        playSvga(fromJson.giftInfo, sendUserInfo)
                    }
                } else if (sendType == ChatRoomMsgInfo.ITEM_WELCOME_TYPE) {
//                    if(isOwner){
//                        sendMessage(roomSourceBean.welcomeMsg,ChatRoomMsgInfo.ITEM_HOST_WELCOME_TYPE)
//                    }
                    addUserEnterAnim(sendUserInfo)
                }
            }
        }
        chatRoomMsgAdapter.addAll(list)
        scrollChatToBottom(50)
    }

    private var applyQueueTask: TaskQueueManagerImpl = TaskQueueManagerImpl()

    /**
     * 透传消息处理逻辑
     */
    private fun dealCmdMsg(it: EMMessage) {
        try {
            if (mContext.isFinishing) {
                return
            }
            val source = it.getIntAttribute("source", -1)
            if (source == ChatConstant.ACTION_MSG_APPLY_SET_UP) { //申请上麦
                logcom("有人申请上麦")
                runOnUiThread {
                    //同时只弹出一个申请弹窗，
                    applyQueueTask.addTask(ApplySeatTask(it, roomId))
                }
                refreshThreeRoomInfo()
            } else if (source == ChatConstant.ACTION_MSG_SET_UP) { //邀请上麦
                //显示收到上麦邀请弹框
                showReceiveInvitePop(it)
            } else if (source == ChatConstant.ACTION_MSG_APPLY_SET_UP_SUCCESS) { //申请上麦成功
                val roomId: String = it.getStringAttribute("roomId")
                val seatId: String = it.getStringAttribute("seatId")
                if (this@BaseLiveRoomFrg.roomId == roomId) {
                    runOnUiThread {
                        userSetSeat(SEAT_TYPE_AUTO, seatId)
                    }
                }
            } else if (source == ChatConstant.ACTION_MSG_ROSE_LACK_ALERT) { //玫瑰不足，提示男嘉宾
                val selfUserInfo = ImUserManager.getSelfUserInfo()
                sendMessage(
                    "@" + selfUserInfo.nickName + "，你的玫瑰余额不足，请及时充值",
                    ChatRoomMsgInfo.ITEM_SYSTEM_TYPE
                )
            } else if (source == ChatConstant.ACTION_MSG_ROSE_LACK_KICK_OUT) { //玫瑰不足，踢出男嘉宾
                //专属房间余额不足 退出房间
                showToast("玫瑰余额用尽")
                LiveRoomEndActivity.lunch(mContext, LiveRoomManager.HOUSE_NOT_FUNDS, "", roomId)
                AgoraManager.getInstance().setDownVideo(localUserId, true)
                leaveRoomFinish()
            } else if (source == ChatConstant.ACTION_MSG_SERVICE_SIT_DOWN) { //服务端强制下麦
                runOnUiThread {
                    val alert: String =
                        it.getStringAttribute(ImMessageParamsConfig.KEY_WARNING_CONTENT, "")
                    if (!TextUtils.isEmpty(alert)) {
                        DialogUtils.showConfirmDialog(
                            "下麦提醒",
                            {},
                            {},
                            content = alert,
                            cancel = "",
                            confirm = "我知道了",
                            isHideCancel = true
                        )
                    }
                    setSeatOutSuccess()
                }
            } else if (source == ChatConstant.ACTION_MSG_ADMIN_ALERT) { //管理员警告提示
                runOnUiThread {
                    DialogUtils.showConfirmDialog(
                        "警告提醒",
                        {},
                        {},
                        content = it.getStringAttribute(
                            ImMessageParamsConfig.KEY_WARNING_CONTENT, ""
                        ),
                        cancel = "",
                        confirm = "我知道了",
                        isHideCancel = true
                    )
                }
            } else if (source == ChatConstant.OPERATE_LEAVE) { //服务端发送强制离开
                runOnUiThread {
                    isLeave = true
                    leaveRoomFinish()
                }
            } else if (source == ChatConstant.ACTION_MSG_SIT_DOWN) {
                //客户端自己发送的强制下麦
                val alert: String =
                    it.getStringAttribute(ImMessageParamsConfig.KEY_WARNING_CONTENT, "")
                if (!TextUtils.isEmpty(alert)) {
                    DialogUtils.showConfirmDialog(
                        "下麦提醒",
                        {},
                        {},
                        content = alert,
                        cancel = "",
                        confirm = "我知道了",
                        isHideCancel = true
                    )
                }
                userDownSeat()
            } else if (source == ChatConstant.ACTION_USER_OUT_TIME_LEAVE) {
                val userId = it.getStringAttribute(ChatConstant.CUSTOM_DATA)
                updateUserLeaveView(userId.toInt())
            } else if (source == ChatConstant.ACTION_MSG_OPEN_AUTO_SEAT) {
                showToast("房主已开启自动上麦权限")
                roomSourceBean.autoSeat = true
            } else if (source == ChatConstant.ACTION_MSG_CLOSE_AUTO_SEAT) {
                showToast("房主已关闭自动上麦权限")
                roomSourceBean.autoSeat = false
            } else if (source == ChatConstant.ACTION_MSG_SWITCH_TYPE_PLAZA) { //房间类型切换至大厅
                showToast("房主已将房间类型切换至大厅")
                getRoomDetail()
            } else if (source == ChatConstant.ACTION_MSG_SWITCH_MIKE) { //开/关麦
                val seatId: Int = it.getIntAttribute("position")
                val mikeUse: Int = it.getIntAttribute("mike_use", 0)
                logcom("position：$seatId  mike_use：$mikeUse")
                val seatPosition = seatList.indexOfFirst { it.id == seatId }
                val mickUser = mikeUse == 1
                refreshSeatMicStatus(seatPosition, mickUser)
            } else if (source == ChatConstant.ACTION_MSG_CLOSE_MIKE_ASSIGN) { //关指定麦
                showToast("房主关闭了你的麦克风")
                val seatId: Int = it.getIntAttribute("position")
                updateMyMicStatus(0, seatId)
            } else if (source == ChatConstant.ACTION_MSG_OPEN_MIKE_ASSIGN) { //开指定麦
                showToast("房主开启了你的麦克风")
                val seatId: Int = it.getIntAttribute("position")
                updateMyMicStatus(1, seatId)
            } else if (source == ChatConstant.ACTION_MSG_SWITCH_TYPE_CONFIRM) { //切换房间为专属房间,通知用户房间结束
                runOnUiThread {
                    roomSourceBean.roomType = 2
                    if (isOwner || isInSeatByUserId(AppCacheManager.userId.toInt())) {
                        showToast("房间已切换至专属房间")
                        getRoomDetail()
                    } else {
                        val bundle = Bundle()
                        bundle.putInt("type", LiveRoomManager.HOUSE_CUT_EXTRA_KICK)
                        LiveRoomEndActivity.lunch(
                            mContext, LiveRoomManager.HOUSE_CUT_EXTRA_KICK, "", roomId
                        )
                        leaveRoomFinish()
                    }
                }
            } else if (source == ChatConstant.ACTION_MSG_SWITCH_TYPE) { //切换房间为专属房间,通知男嘉宾
                val data: JSONObject = it.getJSONObjectAttribute("data")
                logcom("切换房间为专属房间，通知男嘉宾$data")
                val roomId = data.getInt("roomId")
                val price = data.getInt("price")
                runOnUiThread {
                    showSwitchTypeConfirmDialog(roomId, price)
                }
            } else if (source == ChatConstant.ACTION_REFUSE_SWITCH_PRIVATE) {
                showToast("男嘉宾拒绝转为专属房")
            } else if (source == ChatConstant.ACTION_ANGLE_ROOM_RESULT) {
                //天使房关闭 显示天使结果
                val angleResultString = it.getStringAttribute(ChatConstant.CUSTOM_DATA)
                val angleRoomResultInfo =
                    GsonUtils.fromJson(angleResultString, AngleRoomResultInfo::class.java)
                showAngleResultTip(
                    angleRoomResultInfo,
                    if (roomSourceBean.isSongRoom()) CrownedUserListPop.TYPE_SONG else CrownedUserListPop.TYPE_ANGLE
                )
            } else if (source == ChatConstant.ACTION_REFRESH_SEAT) {
                refreshSeatInfo()
            } else {
                onReceiveCmdMsg(it)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected open fun onReceiveCmdMsg(it: EMMessage) {

    }

    protected fun changeRvChatScroll() {
        val softInputVisible = KeyboardUtils.isSoftInputVisible(mContext)
        val isShowExpression =
            messageDialog?.isShow == true && messageDialog?.isShowExpression() == true
        setRvSeatHeight()
        setRvChatMessageTop(softInputVisible || isShowExpression)
    }


    private fun showSwitchTypeConfirmDialog(roomId: Int, price: Int) {
        val content = Spans.builder().text("红娘申请转为专属房间进行私密约\n")
            .text("专属房间需消耗${price}玫瑰/分钟，是否同意？").color(
                CommonUtils.getColor(
                    cn.yanhu.baselib.R.color.colorMain
                )
            ).build()

        DialogUtils.showConfirmDialog(
            "专属私密约会",
            {
                mViewModel.switchTypeConfirm(
                    roomId.toString(),
                    object : OnRequestResultListener<Boolean> {
                        override fun onSuccess(data: BaseBean<Boolean>) {

                        }

                    })
            },
            {
                EmMsgManager.sendCmdMessagePeople(
                    roomSourceBean.ownerInfo!!.userId, "", ChatConstant.ACTION_REFUSE_SWITCH_PRIVATE
                )
            },
            confirm = "同意",
            content = content,
            cancelBg = cn.yanhu.baselib.R.drawable.shape_cancel_btn_r30
        )
    }

    private var receiveInviteSeatPop: ReceiveInviteSeatPop? = null
    private fun showReceiveInvitePop(it: EMMessage) {
        val topActivity = ActivityUtils.getTopActivity()
        if (CommonUtils.isPopShow(receiveInviteSeatPop) || topActivity == null) {
            return
        }
        receiveInviteSeatPop = ReceiveInviteSeatPop.showDialog(topActivity,
            it,
            object : ReceiveInviteSeatPop.OnClickSeatUpListener {
                override fun onClickSeatUp() {
                    val inviteRoomId = it.getStringAttribute("roomId", "")
                    if (!TextUtils.isEmpty(inviteRoomId) && inviteRoomId != roomId) {
                        val inviteSeatId = it.getStringAttribute("seatId", "")
                        userSetOtherRoomSeat(inviteRoomId, inviteSeatId)
                    } else {
                        userSetSeat(SEAT_TYPE_AUTO)
                    }
                }
            })
    }

    private fun userSetOtherRoomSeat(roomId: String, seatId: String) {
        CutLiveRoomUtils.showChangeAlert(object : CutLiveRoomUtils.ChangeListener {
            override fun sure() {
                request({
                    agoraRxApi.userSetSeat(
                        roomId, SEAT_TYPE_AUTO, seatId, AppCacheManager.userId
                    )
                }, object : OnRequestResultListener<String> {
                    override fun onSuccess(data: BaseBean<String>) {
                        LiveRoomManager.toLiveRoomPage(
                            ActivityUtils.getTopActivity() as FragmentActivity, roomId
                        )
                    }

                    override fun onFail(code: Int?, msg: String?) {
                        super.onFail(code, msg)
                        userSeatFail(code, msg)
                    }
                }, false
                )
            }
        })

    }

    /*
     * 用户下麦
     *
     * */
    private fun userDownSeat() {
        if (LiveRoomVideoMiniManager.getInstance().isShowing) { //下麦时关闭悬浮窗，回到直播间
            LiveRoomVideoMiniManager.getInstance().closeFloat(1)
        }
        if (roomSourceBean.roomType == 2) { //专属房间下麦直接离开
            AgoraManager.getInstance().setDownVideo(localUserId, true)
            logcom("专属房间下麦直接离开，调用离开接口")
            roomLeave()
            return
        }
        mViewModel.userSetSeat(roomId,
            SEAT_TYPE_SIT_DOWN,
            getMySeatId().toString(),
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    setSeatOutSuccess()
                }
            })
    }

    private fun setSeatOutSuccess() {
        setSeatStatus()
        logcom("用户下麦：$localUserId")
        AgoraManager.getInstance().setDownVideo(localUserId, true)
        userLeaveChanged(localUserId)
    }

    @SuppressLint("SetTextI18n")
    private fun setSeatStatus() {
        seatStatus = 0
        if (isFreeSeat()) {
            mBinding.tvApply.text = "免费上麦"
            mBinding.tvRoseSeatNum.visibility = View.GONE
            mBinding.ivSeatStatus.setImageResource(R.drawable.ic_free_seat_room)
        } else {
            mBinding.tvApply.text = "申请上麦"
            mBinding.tvRoseSeatNum.visibility = View.VISIBLE
            mBinding.tvRoseSeatNum.text = "${seatList[1].seatRoseNum}玫瑰/次"
            mBinding.ivSeatStatus.setImageResource(R.drawable.ic_apply_seat_room)
        }
    }

    private var isLeave = false
    private var roomLeaveInfo: RoomLeaveResponse? = null

    //离开频道
    fun roomLeave(onRoomLeaveListener: OnRoomLeaveListener? = null) {
        if (isLeave) {
            return
        }
        isLeave = true
        mViewModel.roomLeave(roomId,
            roomSourceBean.uuid,
            object : OnRequestResultListener<RoomLeaveResponse> {
                override fun onSuccess(data: BaseBean<RoomLeaveResponse>) {
                    roomLeaveInfo = data.data
                    destroyRoom()
                    onRoomLeaveListener?.onLeaveSuccess()
                    leaveRoomFinish()
                    clearAgora()
                }

                override fun onFail(code: Int?, msg: String?) {
                    onRoomLeaveListener?.onLeaveSuccess()
                    leaveRoomFinish()
                }

            })
    }

    protected fun userSetSeat(operate: String, seatNum: String = "") {
        val seatId = if (TextUtils.isEmpty(seatNum)) getNeedSeatId().toString() else seatNum
        mViewModel.userSetSeat(roomId, operate, seatId, object : OnRequestResultListener<String> {
            override fun onSuccess(data: BaseBean<String>) {
                if (operate == SEAT_TYPE_APPLY) {
                    //1：申请上麦
                    val selfUserInfo = ImUserManager.getSelfUserInfo()
                    showToast("您已申请上麦，请等待主持人通过")
                    val map: MutableMap<String, Any> = HashMap()
                    map["fromUid"] = AppCacheManager.userId
                    map["seatId"] = seatId
                    map["fromNickName"] = selfUserInfo.nickName
                    map["portrait"] = selfUserInfo.portrait
                    EmMsgManager.sendCmdMessagePeople(
                        roomSourceBean.ownerInfo!!.userId,
                        ChatConstant.ACTION_MSG_APPLY_SET_UP,
                        map,
                    )
                    setHasApplyStatus()

                } else if (operate == SEAT_TYPE_AUTO) {
                    //自动上麦
                    AgoraManager.getInstance().setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
                    refreshSeatInfo(AppCacheManager.userId.toInt())
                    requestInSeat[AppCacheManager.userId.toInt()] = false
                    setHasSeatUpStatus()
                }
            }

            override fun onFail(code: Int?, msg: String?) {
                super.onFail(code, msg)
                userSeatFail(code, msg)

            }

        })
    }

    private fun userSeatFail(code: Int?, msg: String?) {
        when (code) {
            ErrorCode.CODE_NO_BALANCE -> {
                showRechargePop()
            }

            ErrorCode.CODE_NEED_REAL_NAME -> {
                showRealNameAuthPop()
            }

            else -> {
                showToast(msg)
            }
        }
    }

    private fun showRealNameAuthPop() {
        DialogUtils.showConfirmDialog(
            "上麦提醒",
            {
                RouteIntent.lunchToRealNamPage()
            },
            {},
            "申请上麦请先完成实名认证",
            cancel = "取消",
            confirm = "去认证",
            cancelBg = cn.yanhu.baselib.R.drawable.shape_cancel_btn_r30
        )
    }

    /**
     * 获取将要上麦的座位id
     */
    private fun getNeedSeatId(): Int {
        if (roomSourceBean.getFragmentType() == RoomListBean.FRG_THREE_ROOM) {
            return if (AppCacheManager.isMan()) 2 else 3
        } else {
            for (j in seatList.indices) {
                val roomSeatResListDTO = seatList[j]
                roomSeatResListDTO.roomUserSeatInfo ?: return roomSeatResListDTO.id
            }
            return -1
        }
    }


    /**
     * 显示聊天输入弹框
     * user 不为null时表示是@用户
     * isKeyboard true 软禁盘自动显示 false 不自动显示
     */
    open fun showInputDialog(user: BaseUserInfo?, isKeyboard: Boolean) {
        if (CommonUtils.isPopShow(messageDialog)) {
            return
        }
        currentUser = user
        messageDialog = SendMessagePop.showDialog(mContext,
            isKeyboard,
            currentUser,
            object : SendMessagePop.OnMessageSendListener {
                override fun onShowEmoji(height: Int) {
                    isShowEmoji = height > 0
                    ViewUtils.setMarginBottom(mBinding.rvChat, height)
                    setRvChatMessageTop(true)
                }

                override fun onSendMessage(content: String, hasAlt: Boolean) {
                    clickSendMessage(content, if (hasAlt) currentUser else null)
                }

                override fun onSendEmoji(url: String) {
                    clickSendEmoji(url)
                }
            },
            object : SimpleCallback() {
                override fun onDismiss(popupView: BasePopupView) {
                    super.onDismiss(popupView)
                    isShowEmoji = false
                    setRvChatMessageTop(false)
                    ViewUtils.setMarginBottom(mBinding.rvChat, 0)
                    KeyboardUtils.hideSoftInput(context as Activity?)
                }

                override fun onKeyBoardStateChanged(popupView: BasePopupView, height: Int) {
                    super.onKeyBoardStateChanged(popupView, height)
                    if (isShowEmoji) {
                        return
                    }
                    setRvChatMessageTop(height != 0)
                    if (height == 0) {
                        ViewUtils.setMarginBottom(mBinding.rvChat, height)
                    } else {
                        ThreadUtils.getMainHandler().postDelayed({
                            ViewUtils.setMarginBottom(
                                mBinding.rvChat, height + CommonUtils.getDimension(
                                    com.zj.dimens.R.dimen.dp_30
                                )
                            )
                            scrollChatToBottom(0)
                        }, 450)
                    }
                }
            })
    }

    /**
     *  发送自定义表情
     */
    private fun clickSendEmoji(url: String) {
        sendMessage(url, ChatRoomMsgInfo.ITEM_EMOJI_TYPE)
    }

    /**
     *  发送文本消息
     */
    private fun clickSendMessage(content: String, currentUser: BaseUserInfo?) {
        sendMessage(content, ChatRoomMsgInfo.ITEM_DEFAULT_TYPE, currentUser)
    }

    override fun lazyLoad() {
    }

    open fun sendMessage(
        trim: String, sendType: Int, altUser: BaseUserInfo? = null
    ) {
        val message = EMMessage.createTextSendMessage(trim, roomSourceBean.uid)
        val selfUserInfo = ImUserManager.getSelfUserInfo()
        //附加值
        if (altUser != null) {
            message.setAttribute(ChatConstant.ATE_USER_INFO, GsonUtils.toJson(altUser))
        }
        message.setAttribute(ChatConstant.CUSTOM_SEND_USER_INFO, GsonUtils.toJson(selfUserInfo))
        message.setAttribute(ChatConstant.CUSTOM_SEND_ATYPE, sendType)
        //聊天类型
        message.chatType = EMMessage.ChatType.ChatRoom
        message.setMessageStatusCallback(object : EMCallBack {
            override fun onSuccess() {
                runOnUiThread {
                    val chatRoomMsgInfo = ChatRoomMsgInfo(sendType, trim, selfUserInfo, altUser)
                    chatRoomMsgAdapter.add(chatRoomMsgInfo)
                    scrollChatToBottom(50)
                }
            }

            override fun onError(code: Int, error: String) {
                logcom("code=" + code + "error=" + error)
            }
        })
        EMClient.getInstance().chatManager().sendMessage(message)
    }

    private val giftAnimTaskManager: TaskQueueManagerImpl = TaskQueueManagerImpl()
    private fun showGiftSvgAnim(giftInfo: GiftInfo?) {
        if (giftInfo == null || TextUtils.isEmpty(giftInfo.svga)) {
            return
        }
        giftAnimTaskManager.addTask(
            GiftPopAnimTask(
                giftInfo,
                mBinding.svgGiftAnim,
                mBinding.videoGiftAnimView
            )
        )
    }

    //播放动画
    private fun playSvga(giftInfo: GiftInfo, sendUserInfo: UserDetailInfo) {
        if (giftInfo.type == GiftInfo.TYPE_SONG) {
            getRoomExtraInfo()
        }
        starGiftAnimation(
            createGiftSendModel(
                sendUserInfo.nickName,
                sendUserInfo.portrait,
                "送出 ${giftInfo.name}",
                giftInfo.giftIcon,
                giftInfo.sendNumber
            )!!
        )
        showGiftSvgAnim(giftInfo)
        refreshSeatRoseInfo()
    }

    /*
     * 礼物飘屏效果
     * */
    val giftSendModelList: MutableList<GiftSendModel> = ArrayList()

    private fun createGiftSendModel(
        nickName: String, portrait: String, sig: String, giftIcon: String, count: Int
    ): GiftSendModel? {
        return GiftSendModel(nickName, portrait, sig, giftIcon, count)
    }

    private fun starGiftAnimation(model: GiftSendModel) {
        if (!mBinding.callGiftDialogOne.isShowing) {
            sendGiftAnimation(mBinding.callGiftDialogOne, model)
        } else if (!mBinding.callGiftDialogTwo.isShowing) {
            sendGiftAnimation(mBinding.callGiftDialogTwo, model)
        } else {
            giftSendModelList.add(model)
        }
    }

    private fun sendGiftAnimation(view: GiftFrameLayout, model: GiftSendModel) {
        view.setModel(model)
        val animatorSet: AnimatorSet = view.startAnimation(model.giftCount)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                synchronized(giftSendModelList) {
                    if (giftSendModelList.size > 0) {
                        view.startAnimation(
                            giftSendModelList[giftSendModelList.size - 1].giftCount
                        )
                        giftSendModelList.removeAt(giftSendModelList.size - 1)
                    }
                }
            }
        })
    }

    override fun requestData() {
        super.requestData()
        logcom(LiveRoomActivity.LIVE_ROOM_TAG, "获取房间详情---roomId${roomId}")
        getRoomDetail()
        refreshSeatInfo()
    }

    private var seatUserOperatePop: SeatUserOperatePop? = null
    protected fun showUserPop(userId: String) {
        request({ agoraRxApi.getUserInfoByUserId(userId) },
            object : OnRequestResultListener<UserDetailInfo> {
                override fun onSuccess(data: BaseBean<UserDetailInfo>) {
                    if (CommonUtils.isPopShow(seatUserOperatePop)) {
                        return
                    }
                    seatUserOperatePop = SeatUserOperatePop.showDialog(mContext,
                        data.data!!,
                        object : SeatUserOperatePop.OnOperateUserListener {
                            override fun onSendGift(userInfo: UserDetailInfo) {
                                showSendGiftPop(userInfo)
                            }

                            override fun onAlt(userInfo: UserDetailInfo) {
                                sendGiftPop?.dismiss()
                                showInputDialog(userInfo, true)
                            }

                            override fun onAddFriend(userInfo: UserDetailInfo) {
                                showAddFriendPop(userInfo)
                            }

                        })
                }
            })
    }

    private fun showAddFriendPop(userInfo: UserDetailInfo) {
        if (userInfo.addFriendWay == 0) {
            addFriend(userInfo)
        } else {
            DialogUtils.showConfirmDialog(
                "添加好友",
                {
                    addFriend(userInfo)
                },
                {},
                content = "是否同意花费${userInfo.needRoseNum}玫瑰，添加好友？",
                cancel = "取消",
                confirm = "加好友",
                cancelBg = cn.yanhu.baselib.R.drawable.shape_cancel_btn_r30
            )
        }
    }

    private fun addFriend(userInfo: UserDetailInfo) {
        if (userInfo.addFriendWay == 0) {
            applyFriend(userInfo)
        } else {
            addFriendByRose(userInfo)
        }

    }

    private fun addFriendByRose(userInfo: UserDetailInfo) {
        request(
            { agoraRxApi.becomeFriendRose(userInfo.userId) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    userInfo.isFriend = true
                    ChatUserInfoManager.saveUserInfo(userInfo)
                    showToast("添加好友成功")
                    EmMsgManager.sendCmdMessagePeople(
                        userInfo.userId, CmdMsgTypeConfig.ADD_FRIEND, null
                    )
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    if (code == ErrorCode.CODE_NO_BALANCE) {
                        showRechargePop()
                    } else {
                        showToast(msg)
                    }
                }
            },
            isShowToast = false
        )
    }

    private fun applyFriend(userInfo: UserDetailInfo) {
        request(
            { agoraRxApi.addFriend(userInfo.userId) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    showToast("好友请求已发送～")
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    showToast(msg)
                }
            },
            isShowToast = false
        )
    }

    private fun showRechargePop() {
        ApplicationProxy.instance.showRechargePop(mContext, true)
    }

    private fun getRoomDetail() {
        mViewModel.getRoomDetail(roomId, roomType)
    }

    protected fun refreshSeatInfo(uid: Int = 0) {
        refreshThreeRoomInfo()
        mViewModel.getSeatList(roomId, object : OnRequestResultListener<MutableList<RoomSeatInfo>> {
            override fun onSuccess(data: BaseBean<MutableList<RoomSeatInfo>>) {
                val it = data.data ?: return
                seatList = it
                if (uid == 0) {
                    getRoomSeatSuccess(seatList)
                } else {
                    refreshSeatInfo(seatList, uid)
                }
            }
        })
    }

    /**
     * 更新麦位上用户的玫瑰数量
     */
    private fun refreshSeatRoseInfo() {
        getRoseRankList()
        mViewModel.getSeatList(roomId, object : OnRequestResultListener<MutableList<RoomSeatInfo>> {
            override fun onSuccess(data: BaseBean<MutableList<RoomSeatInfo>>) {
                val it = data.data ?: return
                seatList = it
                updateSeatRoseInfo()
            }
        })
    }

    protected open fun updateSeatRoseInfo() {
        for (i in 0 until seatList.size) {
            val seatInfo = seatList[i]
            val roomUserSeatInfo = seatInfo.roomUserSeatInfo
            if (roomUserSeatInfo != null) {
                val item = seatUserAdapter.getItem(i)
                if (item != null) {
                    item.roomUserSeatInfo?.roseNum = roomUserSeatInfo.roseNum
                    item.roomUserSeatInfo?.userList = roomUserSeatInfo.userList
                }
            }

        }
    }


    protected open fun refreshSeatInfo(it: MutableList<RoomSeatInfo>, uid: Int) {
        ThreadUtils.getMainHandler().post {
            for (i in 0 until it.size) {
                val seatInfo = it[i]
                if (seatInfo.roomUserSeatInfo?.userId?.toInt() == uid) {
                    seatUserAdapter[i] = seatInfo
                    seatUserAdapter.notifyItemChanged(i)
                    return@post
                }
            }
        }
    }

    override fun registerNecessaryObserver() {
        //super.registerNecessaryObserver()
        mViewModel.roomDetailLivedata.observe(this) { it ->
            parseState(it, {
                mBinding.roomInfo = it
                roomSourceBean = it
                if (seatList.size > 0) {
                    roomSourceBean.roomSeatResList = seatList
                }
                isOwner = it.ownerInfo?.userId == AppCacheManager.userId
                getRoomInfoSuccess()
            })
        }
        mViewModel.closeRoomObserver.observe(this) {
            parseState(it, {
                showToast("房间已强制关闭")
            })
        }
    }


    private fun joinChatRoom() {
        EMClient.getInstance().chatroomManager().addChatRoomChangeListener(this)
        EMClient.getInstance().chatroomManager()
            .joinChatRoom(roomSourceBean.uid, object : EMValueCallBack<EMChatRoom> {
                override fun onSuccess(value: EMChatRoom?) {
                    logcom("加入聊天室成功")
                    if (!roomSourceBean.isAdmin()) {
                        sendMessage("进入了房间", ChatRoomMsgInfo.ITEM_WELCOME_TYPE)
                    }
                    AgoraManager.getInstance().isInitSuccess = true
                }

                override fun onError(error: Int, errorMsg: String?) {
                    logcom("加入聊天室失败，$error————msg$errorMsg")
                    if (error == 201 && !hasReLogin) {
                        reLoginIm()
                    } else {
                        showToast("直播间异常，请重新尝试进入直播间")
                        leaveRoomFinish()
                    }
                }
            })
    }

    private var hasReLogin = false
    private fun reLoginIm() {
        hasReLogin = true
        ApplicationProxy.instance.reLoginImSdk(object : OnImLoginListener {
            override fun onSuccess() {
                joinChatRoom()
            }

            override fun onError(code: Int, error: String?) {
                leaveRoomFinish()
            }
        })
    }

    private var isFinish = false
    private var isOwnerClose = false

    private fun leaveRoomFinish() {
        if (isFinish) {
            return
        }
        if (isOwnerClose) {
            LiveRoomEndActivity.lunch(
                mContext,
                LiveRoomManager.HOUSE_OWNER_OFF,
                if (roomLeaveInfo == null) "" else Gson().toJson(roomLeaveInfo),
                roomId
            )
        }
        isFinish = true
        mContext.finish()
    }

    /**
     * 添加新用户进房漂流提示动画
     */
    private fun addUserEnterAnim(user: BaseUserInfo) {

        val carUrl = user.carUrl
        if (!TextUtils.isEmpty(carUrl)) {
            val giftInfo = GiftInfo()
            giftInfo.svga = carUrl
            showGiftSvgAnim(giftInfo)
        }
        val enterAnimUrl = user.enterAnimUrl
        if (!TextUtils.isEmpty(enterAnimUrl)) {
            val chatRoomGiftMsg = ChatRoomGiftMsg(user)
            if (enterAnimUrl.endsWith(".svga")) {
                chatRoomGiftMsg.giftStayTime = 3000
            } else {
                chatRoomGiftMsg.giftStayTime = 1500
            }
            mBinding.userEnterView.put(chatRoomGiftMsg)
        }
    }


    private fun initRvChatTop() {
        mBinding.rvSeat.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val maxTop: Int = getMaxTopHeight()
                ViewUtils.setMarginVertical(mBinding.rvChat, maxTop, 0)
                mBinding.rvSeat.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }


    /**
     * @param isShow true软键盘显示 或者表情ui显示
     */
    private fun setRvChatMessageTop(isShow: Boolean) {
        mBinding.rvSeat.post {
            val maxTop: Int = getMaxTopHeight()
            val minTop: Int = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20)
            if (isShow) {
                if (mBinding.viewMask.visibility == View.INVISIBLE) {
                    mBinding.viewMask.visibility = View.VISIBLE
                    AnimManager.showMarginTopAnimator(mBinding.rvChat, maxTop, minTop, 300)
                }
            } else {
                if (mBinding.viewMask.visibility == View.VISIBLE) {
                    mBinding.viewMask.visibility = View.INVISIBLE
                    AnimManager.showMarginTopAnimator(mBinding.rvChat, minTop, maxTop, 200)
                } else {
                    val topMargin =
                        (mBinding.rvChat.layoutParams as ViewGroup.MarginLayoutParams).topMargin
                    if (topMargin != maxTop) {
                        AnimManager.showMarginTopAnimator(mBinding.rvChat, topMargin, maxTop, 200)
                    }
                }
            }
            scrollChatToBottom(400)
        }

    }

    private fun getMaxTopHeight(): Int {
        val titleHeight = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_40)
        return (if (seatHeight > 0) seatHeight else mBinding.rvSeat.height) + titleHeight + mBinding.flTopView.height
    }

    /**
     * 聊天列表滑动到底部
     */
    private fun scrollChatToBottom(duration: Long) {
        ThreadUtils.getMainHandler().postDelayed(
            { mBinding.rvChat.scrollToPosition(chatRoomMsgAdapter.itemCount - 1) }, duration
        )
    }


    private fun preJoinRoom(): Int {
        AgoraManager.getInstance().init(mContext, 0, null)
        logcom(LiveRoomActivity.LIVE_ROOM_TAG, "加载房间---roomId${roomId}")
        //声网初始化
        AgoraManager.getInstance().setVideoEncoderConfiguration(250, 280)
        return AgoraManager.getInstance()
            .joinChannel(AppCacheManager.userId.toInt(), roomId, roomSourceBean.agoraToken)

    }


    private fun destroyRoom() {
        // 移除聊天室回调
        if (!TextUtils.isEmpty(AppCacheManager.userId)) {
            AgoraManager.getInstance().setDownVideo(AppCacheManager.userId.toInt(), true)
        }
        AgoraManager.getInstance().leaveChannel()
        EMClient.getInstance().chatroomManager()
            .leaveChatRoom(roomSourceBean.uid, object : EMCallBack {
                override fun onSuccess() {
                }

                override fun onError(code: Int, error: String?) {
                }
            })
        pauseAnimView()
        clearAnimView()
        logcom(LiveRoomActivity.LIVE_ROOM_TAG, "销毁房间---roomId${roomId}")
        EMClient.getInstance().chatroomManager().removeChatRoomListener(this@BaseLiveRoomFrg)
        handler.removeCallbacksAndMessages(null)
        closeMiniWindow()
        // clearAgora()
    }

    private fun clearAgora() {
        AgoraManager.getInstance().clearRtcConnection()
        AgoraManager.getInstance().onDestory()
    }

    private fun pauseAnimView() {
        mBinding.userEnterView.onPause()
    }

    private fun destroyAnimView() {
        mBinding.userEnterView.onDestroy()
    }

    private fun clearAnimView() {
        mBinding.svgGiftAnim.clearsAfterDetached = true
        mBinding.svgGiftAnim.stopAnimation(true)
        mBinding.svgGiftAnim.clear()
        mBinding.userEnterView.clear()
    }


    override fun onResume() {
        super.onResume()
        mBinding.userEnterView.onResume()
        if (isOnNewIntent) {
            isOnNewIntent = false
            return
        }
        closeMiniWindow()
        addHostSurfaceView()
    }

    override fun onPause() {
        super.onPause()
        pauseAnimView()
    }

    open fun onCustomStop() {
        var isFloatPermission = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isFloatPermission = Settings.canDrawOverlays(mContext)
        }
        if (!mContext.isFinishing && !isLeave && isFloatPermission) {
            showFloatWindow(2)
        }
    }

    fun exitRoom() {
        logcom(LiveRoomActivity.LIVE_ROOM_TAG, "onDestroyView---roomId${roomId}")
        if (!isLeave) {
            request2(
                { agoraRxApi.roomLeave(roomId, roomSourceBean.uuid) },
                object : OnRequestResultListener<RoomLeaveResponse> {
                    override fun onSuccess(data: BaseBean<RoomLeaveResponse>) {
                    }

                    override fun onFail(code: Int?, msg: String?) {
                    }
                },
                false
            )
            destroyRoom()
            destroyAnimView()
        } else {
            destroyRoom()
            destroyAnimView()
        }
    }


    val requestInSeat: MutableMap<Int, Boolean> = mutableMapOf()

    /**
     * 处理声网相关回调逻辑
     */
    override fun agoraListener(type: Int, uid: Int) {
        runOnUiThread {
            if (type == AgoraManager.TOKEN_EXPIRE || type == AgoraManager.TOKEN_WILL_EXPIRE) { //token已过期、token即将过期
                fetchToken()
            } else if (type == AgoraManager.USER_SET_UP) { //检测到有观众上麦，更新视图

                requestInSeat[uid] = true
                logcom("上麦请求$uid")
                removeUserLeaveRecord(uid)
                refreshSeatInfo(uid)

            } else if (type == AgoraManager.USER_SIT_DOWN) { //检测到有观众下麦，更新视图
                logcom("下麦请求$uid")
                updateUserLeaveView(uid)
            } else if (type == AgoraManager.USER_QUIT) { //检测到有观众离开
                logcom("用户离开$uid")
                updateUserLeaveView(uid)
            } else if (type == AgoraManager.USER_SHORT_LEAVE) { //检测主播暂时离开
                //updateUserLeaveView(uid);
            } else if (type == AgoraManager.USER_ERROR_CAMERA_DISABLED) { //主播相机设备由于设备策略而无法打开
            } else if (type == AgoraManager.USER_REMOTE_VIDEO_INIT_FINISH) { //远端用户视频渲染成功
                removeUserLeaveRecord(uid)
                userVideoStatusChanged(uid, false)
            } else if (type == AgoraManager.REMOTE_USER_VIDEO_UP) { //远端视频恢复正常播放
                removeUserLeaveRecord(uid)
                userVideoStatusChanged(uid, false)
            } else if (type == AgoraManager.REMOTE_USER_VIDEO_DOWN) { //远端视频卡顿、停止
                if (!leaveMap.containsKey(uid)) {
                    val leaveThread = LeaveThread(uid)
                    leaveMap[uid] = leaveThread
                    leaveThread.start()
                }
            } else if (type == AgoraManager.USER_NETWOKR_GOOD) { //网络良好
                networkType = 1
            } else if (type == AgoraManager.USER_NETWOKR_BAD || type == AgoraManager.USER_NETWOKR_DOWN) { //网络不佳、网络断开
                networkType = if (type == AgoraManager.USER_NETWOKR_BAD) {
                    0
                } else {
                    -1
                }
            } else if (type == AgoraManager.USER_REMOTE_VIDEO_PLAY_FAIL) {
                userVideoStatusChanged(uid, true)
            }
        }
    }

    private var networkType = 1;

    private fun updateUserLeaveView(uid: Int) {
        if (uid.toString() == roomSourceBean.ownerInfo?.userId) {
            removeUserLeaveRecord(uid)
            userVideoStatusChanged(uid, true)
        } else {
            removeUserLeaveRecord(uid)
            AgoraManager.getInstance().setDownVideo(uid, localUserId == uid)
            userLeaveChanged(uid)
        }
    }


    private var leaveMap: MutableMap<Int, Thread> = mutableMapOf()

    //溢出用户离开记录
    private fun removeUserLeaveRecord(uid: Int): Boolean {
        logcom("leaveMap是否包含：" + leaveMap.containsKey(uid))
        if (leaveMap.containsKey(uid)) {
            leaveMap.remove(uid)
            return true
        }
        return false
    }

    //嘉宾离线超时1分钟，强制踢出房间
    protected fun operateLeave(operatedUserId: Int) {
        logcom("强制下麦：$operatedUserId")
        mViewModel.operateLeave(roomId,
            operatedUserId.toString(),
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    updateUserLeaveView(operatedUserId)
                    EmMsgManager.sendCmdMessageToChatRoom(
                        roomSourceBean.uid,
                        operatedUserId.toString(),
                        ChatConstant.ACTION_USER_OUT_TIME_LEAVE
                    )
                }
            })
    }

    var handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 2) {
                logcom("handler：强制下麦")
                val downUserId = msg.obj as Int
                if (isOwner && removeUserLeaveRecord(downUserId)) {
                    //判断是否在离开倒计时集合，为true直接强制踢出房间
                    logcom("downWheet：强制下麦成功")
                    operateLeave(downUserId)
                }
            } else if (msg.what == 3) {
                //5后显示暂时离线
                val downUserId = msg.obj as Int
                if (leaveMap.containsKey(downUserId)) {
                    userVideoStatusChanged(downUserId, true)
                }
            }
        }
    }


    inner class LeaveThread(private val leaveUserId: Int) : Thread() {
        override fun run() {
            super.run()
            var millisecond = 0
            logcom("uid：" + leaveUserId + "开始离线倒计时")
            logcom("map地址：" + leaveMap[leaveUserId])
            logcom("线程地址：$this")
            while (leaveMap.containsKey(leaveUserId) && leaveMap.get(leaveUserId) == this) {
                try {
                    sleep(1000)
                    millisecond += 1000
                    if (millisecond == 5000) {
                        val obtain = Message.obtain()
                        obtain.what = 3
                        obtain.obj = leaveUserId
                        handler.sendMessage(obtain)
                    }
                    if (millisecond >= 60000) {
                        val obtain = Message.obtain()
                        obtain.what = 2
                        obtain.obj = leaveUserId
                        handler.sendMessage(obtain)
                        break
                    }
                } catch (e: InterruptedException) {
                    logcom(e.message)
                    break
                }
            }
            logcom("uid：" + leaveUserId + "离线倒计时结束")
        }
    }

    private var isRequestToken: Boolean = false
    private fun fetchToken() {
        if (isRequestToken) {
            return
        }
        isRequestToken = true
        mViewModel.getAgoraToken(roomId)
        mViewModel.getTokenObserver.observe(this) { it ->
            parseState(it, {
                roomSourceBean.agoraToken = it
                isRequestToken = false
                AgoraManager.getInstance().renewToken(it)
            }, {
                isRequestToken = false
            })
        }
    }

    override fun onAudioVolumeIndication(
        speakers: Array<out IRtcEngineEventHandler.AudioVolumeInfo>?, totalVolume: Int
    ) {
        if (isOwner) {
            for (speaker in speakers!!) {
                val uid = speaker.uid
                if (requestInSeat.containsKey(uid)) {
                    val aBoolean = requestInSeat[uid]
                    if (!isInSeatByUserId(uid) && (aBoolean == null || !aBoolean)) {
                        //不在座位上 还有声音 出现了鬼麦现象 踢下麦
                        requestInSeat.remove(uid)
                        EmMsgManager.sendCmdMessagePeople(
                            uid.toString(), "", ChatConstant.ACTION_MSG_SIT_DOWN
                        )
                    }
                }
            }
        }
    }

    private fun isInSeatByUserId(uid: Int): Boolean {
        for (j in seatList.indices) {
            val roomSeatResListDTO = seatList.get(j) ?: return false
            val roomUserSeatInfo = roomSeatResListDTO.roomUserSeatInfo
            if (roomUserSeatInfo != null) {
                val userId: Int = roomUserSeatInfo.userId.toInt()
                if (userId == uid) {
                    return true
                }
            }
        }
        return false
    }

    open fun getRoseRankList() {}

    /**
     * 获取我的座位id
     */
    private fun getMySeatId(): Int {
        for (j in seatList.indices) {
            val roomSeatResListDTO = seatList[j]
            val roomUserSeatInfo = roomSeatResListDTO.roomUserSeatInfo
            if (roomUserSeatInfo != null) {
                val userId: Int = roomUserSeatInfo.userId.toInt()
                if (userId == AppCacheManager.userId.toInt()) {
                    return roomSeatResListDTO.id
                }
            }
        }
        return -1
    }

    /**
     * 有用户下麦
     */
    protected open fun userLeaveChanged(uid: Int) {
        refreshThreeRoomInfo()
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

    private fun refreshThreeRoomInfo() {
        getOnlineUser()
        if (isOwner && roomSourceBean.getFragmentType() == RoomListBean.FRG_THREE_ROOM) {
            getRoomDetail()
        }
    }

    /**
     * 设置是否显示 预览页面
     */
    protected fun userVideoStatusChanged(uid: Int, isShowProload: Boolean) {
        seatList.forEach {
            if (it.roomUserSeatInfo?.userId?.toInt() == uid) {
                it.ifLeave = isShowProload
                return
            }
        }
    }


    protected open fun getRoomInfoSuccess() {
        if (roomSourceBean.isSongRoom()) {
            return
        }
        if (seatUserAdapter is MoreSeatRoomAdapter) {
            (seatUserAdapter as MoreSeatRoomAdapter).roomDetailInfo = roomSourceBean
            if ((seatUserAdapter as MoreSeatRoomAdapter).roomDetailInfo == null) {
                seatUserAdapter.notifyItemChanged(0)
            } else {
                seatUserAdapter.notifyItemChanged(0, true)
            }
        } else if (seatUserAdapter is ThreeRoomSeatAdapter) {
            (seatUserAdapter as ThreeRoomSeatAdapter).roomDetailInfo = roomSourceBean
            if ((seatUserAdapter as ThreeRoomSeatAdapter).roomDetailInfo == null) {
                seatUserAdapter.notifyItemChanged(0)
            } else {
                seatUserAdapter.notifyItemChanged(0, true)
            }
        }
    }

    protected open fun getRoomSeatSuccess(seatList: MutableList<RoomSeatInfo>) {
        seatUserAdapter.submitList(seatList)
    }


    companion object {
        const val SEAT_TYPE_AUTO = "2" //自动上麦
        const val SEAT_TYPE_APPLY = "1"//申请上麦
        const val SEAT_TYPE_SIT_DOWN = "3"//下麦
    }

    private fun isNotMyRoom(roomId: String?): Boolean {
        return roomSourceBean.uid != roomId
    }

    override fun onChatRoomDestroyed(roomId: String?, roomName: String?) {
        logcom("房主解散聊天室：$roomId")
        if (isNotMyRoom(roomId)) {
            return
        }
        LiveDataEventManager.sendLiveDataMessage(
            EventBusKeyConfig.CLOSELIVEROOM, this@BaseLiveRoomFrg.roomId
        )
        if (isOwnerClose) {
            return
        }
        if (roomSourceBean.admin == 1) { //管理员
            leaveRoomFinish()
            return
        }
        if (isOwner) {
            if (!isLeave) {
                //管理员强制关闭
                LiveRoomEndActivity.lunch(
                    mContext,
                    LiveRoomManager.HOUSE_ADMINISTRATOR_OFF,
                    "",
                    this@BaseLiveRoomFrg.roomId
                )
            } else {
                LiveRoomEndActivity.lunch(
                    mContext,
                    LiveRoomManager.HOUSE_OWNER_OFF,
                    if (roomLeaveInfo == null) "" else GsonUtils.toJson(roomLeaveInfo),
                    this@BaseLiveRoomFrg.roomId
                )
            }
        } else {
            LiveRoomEndActivity.lunch(
                mContext, LiveRoomManager.HOUSE_OFF, "", this@BaseLiveRoomFrg.roomId
            )
        }
        isLeave = true
        leaveRoomFinish()
    }

    //是否开、关麦提示弹窗
    protected fun switchMikeAlert(mikeUse: Boolean, seatNum: Int) {
        if (mikeUse) {
            DialogUtils.showConfirmDialog("开启麦克风", {
                switchMike(1, seatNum, localUserId)
            }, {}, "当前正在交友中，确定开启麦克风吗？", cancel = "取消", confirm = "确定开启")
        } else {
            DialogUtils.showConfirmDialog("关闭麦克风", {
                switchMike(0, seatNum, localUserId)
            }, {}, "当前正在交友中，确定关闭麦克风吗？", cancel = "取消", confirm = "确定关闭")
        }
    }


    protected fun ownerSwitchMikeAlert(mikeUse: Boolean, seatNum: Int, targetUserId: String) {
        roomSourceBean.ownerInfo?.apply {
            if (mikeUse) {
                DialogUtils.showConfirmDialog("开启麦克风", {
                    switchMike(1, seatNum, this.userId.toInt(), true, targetUserId)
                }, {}, "确定开启此用户麦克风吗？", cancel = "取消", confirm = "确定开启")
            } else {
                DialogUtils.showConfirmDialog("关闭麦克风", {
                    switchMike(0, seatNum, this.userId.toInt(), true, targetUserId)
                }, {}, "确定关闭此用户麦克风吗？", cancel = "取消", confirm = "确定关闭")
            }
        }
    }

    /*
     * 打开/关闭麦克风，0：关闭，1：打开
     * */
    private fun switchMike(
        operate: Int,
        seatNum: Int,
        operatedUserId: Int,
        isOwnerOperate: Boolean = false,
        targetUserId: String = ""
    ) {
        mViewModel.switchMike(Integer.valueOf(roomId),
            operate,
            seatNum,
            operatedUserId,
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    logcom("打开/关闭麦克风")
                    if (isOwnerOperate) {
                        //是房主操作开关麦
                        val map: MutableMap<String, Any> = HashMap()
                        map["position"] = seatNum

                        EmMsgManager.sendCmdMessagePeople(
                            targetUserId,
                            if (operate == 0) ChatConstant.ACTION_MSG_CLOSE_MIKE_ASSIGN else ChatConstant.ACTION_MSG_OPEN_MIKE_ASSIGN,
                            map,
                            EMMessage.ChatType.Chat
                        )
                    } else {
                        updateMyMicStatus(operate, seatNum)
                    }
                }
            })
    }

    private fun updateMyMicStatus(operate: Int, seatId: Int) {
        AgoraManager.getInstance().muteLocalAudioStream(operate == 0)
        val seatPosition = seatList.indexOfFirst {
            it.id == seatId
        }
        val mickUser = operate == 1
        refreshSeatMicStatus(seatPosition, mickUser)
        val map: MutableMap<String, Any> = HashMap()
        map["position"] = seatId
        map["mike_use"] = operate
        EmMsgManager.sendCmdMessagePeople(
            roomSourceBean.uid,
            ChatConstant.ACTION_MSG_SWITCH_MIKE,
            map,
            EMMessage.ChatType.ChatRoom
        )
    }

    protected open fun refreshSeatMicStatus(seatPosition: Int, mickUser: Boolean) {
        seatUserAdapter.getItem(seatPosition)?.mikeUser = mickUser
    }

    override fun onMemberJoined(roomId: String?, participant: String?) {
        logcom("新朋友加入聊天室：$participant")
        if (isNotMyRoom(roomId)) {
            return
        }
        refreshThreeRoomInfo()
    }

    override fun onMemberExited(roomId: String?, roomName: String?, participant: String?) {
        logcom("有成员退出聊天室：$roomName——————$participant")
        if (isNotMyRoom(roomId)) {
            return
        }
        val userId = participant!!.toInt()
        AgoraManager.getInstance().setDownVideo(userId, localUserId == userId)
        updateUserLeaveView(userId)
        refreshThreeRoomInfo()
    }

    override fun onRemovedFromChatRoom(
        reason: Int, roomId: String?, roomName: String?, participant: String?
    ) {
    }

    override fun onMuteListAdded(
        chatRoomId: String?, mutes: MutableList<String>?, expireTime: Long
    ) {
        logcom("成员被禁言：$mutes——————")
        if (isNotMyRoom(chatRoomId)) {
            return
        }
        for (mute in mutes!!) {
            if (mute == localUserId.toString()) {
                showToast("你已被房主禁言，无法发言")
            }
        }
    }

    override fun onMuteListRemoved(chatRoomId: String?, mutes: MutableList<String>?) {
        logcom("成员取消禁言：$mutes——————")
        if (isNotMyRoom(chatRoomId)) {
            return
        }
        for (mute in mutes!!) {
            if (mute == localUserId.toString()) {
                showToast("你已被房主取消禁言")
            }
        }
    }

    override fun onWhiteListAdded(chatRoomId: String?, whitelist: MutableList<String>?) {
    }

    override fun onWhiteListRemoved(chatRoomId: String?, whitelist: MutableList<String>?) {
    }

    override fun onAllMemberMuteStateChanged(chatRoomId: String?, isMuted: Boolean) {
    }

    override fun onAdminAdded(chatRoomId: String?, admin: String?) {
    }

    override fun onAdminRemoved(chatRoomId: String?, admin: String?) {
    }

    override fun onOwnerChanged(chatRoomId: String?, newOwner: String?, oldOwner: String?) {
    }

    override fun onAnnouncementChanged(chatRoomId: String?, announcement: String?) {
    }

    private var liveRoomUserListPop: LiveRoomSeatManagerPop? = null
    protected fun showSeatUserList() {
        request({ agoraRxApi.getInviteList(roomId, "0", "0", 1) },
            object : OnRequestResultListener<MutableList<UserDetailInfo>> {
                override fun onSuccess(data: BaseBean<MutableList<UserDetailInfo>>) {
                    val userList = data.data ?: return
                    if (CommonUtils.isPopShow(liveRoomUserListPop)) {
                        return
                    }
                    liveRoomUserListPop = LiveRoomSeatManagerPop.showDialog(
                        mContext,
                        userList,
                        roomSourceBean,
                        onSendSeatInviteListener = inviteSeatListener
                    )
                }
            })
    }
}