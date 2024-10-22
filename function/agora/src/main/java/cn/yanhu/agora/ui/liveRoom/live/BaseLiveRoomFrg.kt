package cn.yanhu.agora.ui.liveRoom.live

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.UserEnterAdapter
import cn.yanhu.agora.adapter.liveRoom.LiveRoomChatMessageAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.ChatRoomMsgInfo
import cn.yanhu.agora.bean.GiftMsgInfo
import cn.yanhu.agora.bean.RoomLeaveResponse
import cn.yanhu.agora.databinding.FrgBaseLiveRoomBinding
import cn.yanhu.agora.listener.IRtcEngineEventHandlerListener
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.manager.AgoraPhoneManager
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.agora.miniwindow.LiveRoomVideoMiniManager
import cn.yanhu.agora.pop.ReceiveInviteSeatPop
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
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.ChatRoomGiftMsg
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.GiftSendModel
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.bean.SendGiftRequest
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.CmdMsgTypeConfig
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.task.GiftPopAnimTask
import cn.yanhu.commonres.utils.PermissionXUtils
import cn.yanhu.commonres.view.GiftFrameLayout
import cn.yanhu.imchat.db.ChatUserInfoManager
import cn.yanhu.imchat.manager.EmMsgManager
import cn.yanhu.imchat.manager.ImUserManager
import cn.yanhu.imchat.pop.ChatListDialog
import cn.yanhu.imchat.pop.SendGiftPop
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ErrorCode
import com.alibaba.android.arouter.utils.TextUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.google.gson.Gson
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
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler


/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
open class BaseLiveRoomFrg : BaseFragment<FrgBaseLiveRoomBinding, LiveRoomViewModel>(
    R.layout.frg_base_live_room, LiveRoomViewModel::class.java
), IRtcEngineEventHandlerListener, EMChatRoomChangeListener {

    protected var roomId: String = ""
    private var roomType: Int = 0
    private val chatRoomMsgAdapter by lazy { LiveRoomChatMessageAdapter() }
    private var messageDialog: SendMessagePop? = null
    private var currentUser: BaseUserInfo? = null
    private var isShowEmoji = false
    lateinit var roomSourceBean: RoomDetailInfo

    var isOwner: Boolean = false
    protected var localUserId: Int = 0
    var seatList = mutableListOf<RoomSeatInfo>()

    override fun initData() {
        localUserId = AppCacheManager.userId.toInt()
        roomSourceBean = requireArguments().getSerializable(IntentKeyConfig.DATA) as RoomDetailInfo
        mBinding.rvSeat.itemAnimator?.changeDuration = 0
        mBinding.rvChat.itemAnimator?.changeDuration = 0
        mBinding.rvChat.adapter = chatRoomMsgAdapter
        roomId = roomSourceBean.roomId.toString()
        roomType = roomSourceBean.roomType
        isOwner = roomSourceBean.ownerInfo?.userId == AppCacheManager.userId
        if (isOwner) {
            mBinding.vgAutoSeat.visibility = View.VISIBLE
            mBinding.ivSeatStatus.visibility = View.INVISIBLE
        } else {
            mBinding.vgAutoSeat.visibility = View.INVISIBLE
            mBinding.ivSeatStatus.visibility = View.VISIBLE
        }
        mBinding.roomInfo = roomSourceBean
        AgoraManager.getInstence().init(mContext, 0, null)
        getRoomInfoSuccess()
        seatList = roomSourceBean.roomSeatResList
        getRoomSeatSuccess(seatList)
        setSeatApplyStatus()
        initRvChatTop()
        val list: MutableList<ChatRoomMsgInfo> = mutableListOf()
        list.add(ChatRoomMsgInfo(ChatRoomMsgInfo.ITEM_SYSTEM_TYPE, "", null))
        chatRoomMsgAdapter.submitList(list)
        setUserEnterAdapter()
        joinChatRoom()
        joinChannel()
        requestData()
        setUnReadMsgCount()
    }

    private fun setUnReadMsgCount() {
        val unreadMessageCount: Int = EmMsgManager.getPrivateChatUnreadCount()
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

    private fun setSeatApplyStatus() {
        if (isInSeatByUserId(AppCacheManager.userId.toInt())) {
            setHasSeatUpStatus()
        } else {
            seatStatus = 0
            if (isFreeSeat()) {
                mBinding.ivSeatStatus.setImageResource(R.drawable.ic_free_seat_room)
            } else {
                mBinding.ivSeatStatus.setImageResource(R.drawable.ic_apply_seat_room)
            }
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
        addUserEnterAnim(ImUserManager.getSelfUserInfo())
    }


    private fun isFreeSeat(): Boolean {
        return if (AppCacheManager.isMan()) {
            seatList[1].seatUserRose
        } else {
            seatList[2].seatUserRose
        }
    }

    protected var seatStatus = 0
    override fun initListener() {
        mBinding.ivMessage.setOnSingleClickListener {
            ChatListDialog.showDialog(mContext)
        }
        mBinding.ivSeatStatus.setOnSingleClickListener {
            clickSeatBtn()
        }
        mBinding.ivExit.setOnSingleClickListener {
            showFloatWindow(-1)
        }
        mBinding.bgInput.setOnSingleClickListener {
            showInputDialog(null, true)
        }
        mBinding.ivExtension.setOnSingleClickListener {
            showInputDialog(null, false)
        }
        mBinding.ivSendGift.setOnSingleClickListener {
            val roomUserSeatInfo = roomSourceBean.roomSeatResList[0].roomUserSeatInfo!!
            roomUserSeatInfo.roomId = roomId.toInt()

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
            val autoSeat = !roomSourceBean.autoSeat
            DialogUtils.showConfirmDialog(
                if (autoSeat) "开启自动上麦" else "关闭自动上麦",
                {
                    autoSeat(autoSeat)
                    autoSeat.also { roomSourceBean.autoSeat = it }
                },
                {
                },
                if (autoSeat) "用户申请上麦后将自动同意，是否确认开启？" else "关闭后，用户需申请才可上麦"
            )

        }
        mBinding.ivSetting.setOnSingleClickListener {
            showToolDialog()
        }
        registerNetChange()
    }

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
        toolDialog.setLiveRoomInfo(
            roomSourceBean,
            object : ToolDialog.OnClickListener {
                override fun onClickWarning() {
                }
                override fun onClickClose() {
                    mViewModel.closeRoom(roomId,"房间已强制关闭",roomSourceBean.uuid)
                }
            })
        toolDialog.show(mContext.supportFragmentManager, "show_tool")
    }

    protected fun showSendGiftPop(roomUserSeatInfo: UserDetailInfo) =
        SendGiftPop.showDialog(mContext,
            roomUserSeatInfo,
            SendGiftRequest.SOURCE_LIVE_ROOM,
            0,
            object : SendGiftPop.OnSendGiftListener {
                override fun onSendGift(item: GiftInfo) {
                    playSvga(item, ImUserManager.getSelfUserInfo())
                    val giftMsgInfo = GiftMsgInfo(item, roomSourceBean.ownerInfo!!)
                    sendMessage(GsonUtils.toJson(giftMsgInfo), ChatRoomMsgInfo.ITEM_GIFT_TYPE)
                }
            })


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
        if (seatStatus == 0) {
            userSetSeat(if (roomSourceBean.autoSeat) SEAT_TYPE_AUTO else SEAT_TYPE_APPLY)
        } else if (seatStatus == 1) {
            showToast("已申请，等待房主同意")
        } else {
            //下麦
            userDownSeat("", AppCacheManager.userId.toInt())
        }
    }

    protected fun setHasSeatUpStatus() {
        seatStatus = 2
        mBinding.ivSeatStatus.setImageResource(R.drawable.ic_down_seat)
    }

    protected fun setHasApplyStatus() {
        mBinding.ivSeatStatus.setImageResource(R.drawable.ic_already_seat)
        seatStatus = 1
    }


    private fun autoSeat(autoSeat: Boolean) {
        mViewModel.autoSeat(
            roomId,
            (if (autoSeat) 1 else 0).toString(),
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    logcom("开启/关闭自动上麦")
                    roomSourceBean.autoSeat = autoSeat
                    //需要透传消息提示观众房主已开启自动上麦/关闭自动上麦
                    if (autoSeat) {
                        EmMsgManager.sendCmdMessageToChatRoom(
                            roomSourceBean.uid,
                            "",
                            ChatConstant.ACTION_MSG_OPEN_AUTO_SEAT
                        )
                    } else {
                        EmMsgManager.sendCmdMessageToChatRoom(
                            roomSourceBean.uid,
                            "",
                            ChatConstant.ACTION_MSG_CLOSE_AUTO_SEAT
                        )
                    }
                }
            })
    }

    //获取悬浮窗权限
    open fun showFloatWindow(type: Int) {
        PermissionXUtils.checkAlertPermission(
            mContext,
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

    private fun closeRoom() {
        if (isOwner) {
            isOwnerClose = true
        }
        roomLeave()
    }

    protected open fun doShowFloatWindow() {

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
                    playSvga(fromJson.giftInfo, sendUserInfo)
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
    private fun dealCmdMsg(it: EMMessage) {
        try {
            val source = it.getIntAttribute("source", -1)
            if (source == ChatConstant.ACTION_MSG_APPLY_SET_UP) { //申请上麦
                logcom("有人申请上麦")
                runOnUiThread {
                    //同时只弹出一个申请弹窗，
                    applyQueueTask.addTask(ApplySeatTask(it, roomId))
                }
            } else if (source == ChatConstant.ACTION_MSG_SET_UP) { //邀请上麦
                //显示收到上麦邀请弹框
                showReceiveInvitePop(it)
            } else if (source == ChatConstant.ACTION_MSG_APPLY_SET_UP_SUCCESS) { //申请上麦成功
                val roomId: String = it.getStringAttribute("roomId")
                if (this@BaseLiveRoomFrg.roomId == roomId) {
                    runOnUiThread {
                        userSetSeat(SEAT_TYPE_AUTO)
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
                AgoraManager.getInstence().setDownVideo(localUserId, true)
                leaveRoomFinish()
            } else if (source == ChatConstant.ACTION_MSG_ADMIN_SIT_DOWN) { //管理员强制下麦
                runOnUiThread {
                    val alert: String =
                        it.getStringAttribute("alert", "您违反直播秩序，已被管理员强制下麦")

                    DialogUtils.showConfirmDialog("下麦提醒", {

                    }, {

                    }, content = alert, cancel = "", confirm = "我知道了", isHideCancel = true)
                    userDownSeat("", localUserId)
                }
            } else if (source == ChatConstant.ACTION_MSG_ADMIN_ALERT) { //管理员警告提示
                runOnUiThread {
                    DialogUtils.showConfirmDialog(
                        "警告提醒",
                        {
                        },
                        {
                        },
                        content = it.getStringAttribute("alert"),
                        cancel = "",
                        confirm = "我知道了",
                        isHideCancel = true
                    )
                }
            } else if (source == ChatConstant.OPERATE_LEAVE) { //强制离开
                runOnUiThread { leaveRoomFinish() }
            } else if (source == ChatConstant.ACTION_MSG_SIT_DOWN) { //强制下麦
                userDownSeat("", localUserId)
            } else if (source == ChatConstant.ACTION_USER_OUT_TIME_LEAVE) {
                val userId = it.getStringAttribute(ChatConstant.CUSTOM_DATA)
                updateUserLeaveView(userId.toInt())
            } else if (source == ChatConstant.ACTION_MSG_OPEN_AUTO_SEAT) { //自动上麦
                showToast("房主已开启自动上麦权限")
                roomSourceBean.autoSeat = true
            } else if (source == ChatConstant.ACTION_MSG_CLOSE_AUTO_SEAT) { //自动上麦
                showToast("房主已关闭自动上麦权限")
                roomSourceBean.autoSeat = false
            } else if (source == ChatConstant.ACTION_MSG_SWITCH_TYPE_PLAZA) { //房间类型切换至大厅
                showToast("房主已将房间类型切换至大厅")
                getRoomDetail()
            } else if (source == ChatConstant.ACTION_MSG_SWITCH_MIKE) { //开/关麦
                val position: Int = it.getIntAttribute("position")
                val mikeUse: Int = it.getIntAttribute("mike_use", 0)
                logcom("position：$position  mike_use：$mikeUse")
                val seatPosition = position - 1
                val mickUser = mikeUse == 1
                refreshSeatMicStatus(seatPosition,mickUser)
            } else if (source == ChatConstant.ACTION_MSG_CLOSE_MIKE_ASSIGN) { //关指定麦
                showToast("房主关闭了你的麦克风，你无法发言")
                val position: Int = it.getIntAttribute("position")
                AgoraManager.getInstence().muteLocalAudioStream(true)
                updateMyMicStatus(0, position)
            } else if (source == ChatConstant.ACTION_MSG_OPEN_MIKE_ASSIGN) { //开指定麦
                showToast("房主打开了你的麦克风")
                val position: Int = it.getIntAttribute("position")
                AgoraManager.getInstence().muteLocalAudioStream(false)
                updateMyMicStatus(1, position)
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var receiveInviteSeatPop: ReceiveInviteSeatPop? = null
    private fun showReceiveInvitePop(it: EMMessage) {
        if (CommonUtils.isPopShow(receiveInviteSeatPop)) {
            return
        }
        receiveInviteSeatPop = ReceiveInviteSeatPop.showDialog(
            mContext,
            it,
            object : ReceiveInviteSeatPop.OnClickSeatUpListener {
                override fun onClickSeatUp() {
                    userSetSeat(SEAT_TYPE_AUTO)
                }
            })
    }


    /*
     * 用户下麦
     *
     * */
    private fun userDownSeat(downCause: String, operatedUserId: Int) {
        if (LiveRoomVideoMiniManager.getInstance().isShowing) { //下麦时关闭悬浮窗，回到直播间
            LiveRoomVideoMiniManager.getInstance().closeFloat(1)
        }
        if (roomSourceBean.roomType == 2) { //专属房间下麦直接离开
            AgoraManager.getInstence().setDownVideo(localUserId, true)
            logcom("专属房间下麦直接离开，调用离开接口")
            roomLeave()
            return
        }
        logcom("下麦原因：$downCause")
        if (!TextUtils.isEmpty(downCause)) {
            showToast(downCause)
        }
        mViewModel.userSetSeat(
            roomId,
            SEAT_TYPE_SIT_DOWN,
            if (AppCacheManager.isMan()) "2" else "3",
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    seatStatus = 0
                    if (isFreeSeat()) {
                        mBinding.ivSeatStatus.setImageResource(R.drawable.ic_free_seat_room)
                    } else {
                        mBinding.ivSeatStatus.setImageResource(R.drawable.ic_apply_seat_room)
                    }
                    logcom("用户下麦：$operatedUserId")
                    AgoraManager.getInstence().setDownVideo(localUserId, true)
                    userLeaveChanged(localUserId)
                }
            })
    }

    private var isLeave = false
    private var roomLeaveInfo: RoomLeaveResponse? = null

    //离开频道
    private fun roomLeave() {
        isLeave = true
        mViewModel.roomLeave(roomId, roomSourceBean.uuid)
        mViewModel.roomLeaveObserver.observe(this) {
            parseState(it, {
                roomLeaveInfo = it
                leaveRoomFinish()
            }, {
                leaveRoomFinish()
            })
        }
    }

    protected fun userSetSeat(operate: String) {
        mViewModel.userSetSeat(
            roomId,
            operate,
            if (AppCacheManager.isMan()) "2" else "3", object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    if (operate == SEAT_TYPE_APPLY) {
                        //1：申请上麦
                        val selfUserInfo = ImUserManager.getSelfUserInfo()
                        showToast("您已申请上麦，请等待主持人通过")
                        val map: MutableMap<String, Any> = HashMap()
                        map["fromUid"] = AppCacheManager.userId
                        map["seatId"] = if (AppCacheManager.isMan()) "2" else "3"
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
                        AgoraManager.getInstence().setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
                        refreshSeatInfo()
                        requestInSeat[AppCacheManager.userId.toInt()] = false
                        setHasSeatUpStatus()
                    }
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    if (code == ErrorCode.CODE_NO_BALANCE){
                        showRechargePop()
                    }
                }

            }
        )
    }


    /**
     * 显示聊天输入弹框
     * user 不为null时表示是@用户
     * isKeyboard true 软禁盘自动显示 false 不自动显示
     */
    open fun showInputDialog(user: BaseUserInfo?, isKeyboard: Boolean) {
        if (CommonUtils.isPopShow(messageDialog)){
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

                override fun onSendMessage(content: String) {
                    clickSendMessage(content, currentUser)
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
     * TODO 发送自定义表情
     */
    private fun clickSendEmoji(url: String) {
        sendMessage(url, ChatRoomMsgInfo.ITEM_EMOJI_TYPE)
    }

    /**
     * TODO 发送文本消息
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

    private fun showGiftSvgAnim(url: String) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        giftAnimTaskManager.addTask(GiftPopAnimTask(url))
    }

    //播放动画
    private fun playSvga(giftInfo: GiftInfo, sendUserInfo: UserDetailInfo) {
        starGiftAnimation(
            createGiftSendModel(
                sendUserInfo.nickName,
                sendUserInfo.portrait,
                "送出 ${giftInfo.name}",
                giftInfo.giftIcon,
                giftInfo.sendNumber
            )!!
        )
        showGiftSvgAnim(giftInfo.svga)
    }

    /*
     * 礼物飘屏效果
     * */
    val giftSendModelList: MutableList<GiftSendModel> = ArrayList()

    private fun createGiftSendModel(
        nickName: String,
        portrait: String,
        sig: String,
        giftIcon: String,
        count: Int
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
                            giftSendModelList[giftSendModelList.size - 1].getGiftCount()
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
    protected fun showUserPop(item: RoomSeatInfo) {
        request({ agoraRxApi.getUserInfoByUserId(item.roomUserSeatInfo!!.userId) },
            object : OnRequestResultListener<UserDetailInfo> {
                override fun onSuccess(data: BaseBean<UserDetailInfo>) {
                    if (CommonUtils.isPopShow(seatUserOperatePop)) {
                        return
                    }
                    seatUserOperatePop = SeatUserOperatePop.showDialog(
                        mContext,
                        data.data!!,
                        object : SeatUserOperatePop.OnOperateUserListener {
                            override fun onSendGift(userInfo: UserDetailInfo) {
                                showSendGiftPop(userInfo)
                            }

                            override fun onAlt(userInfo: UserDetailInfo) {
                                showInputDialog(userInfo, true)
                            }

                            override fun onAddFriend(userInfo: UserDetailInfo) {
                                showAddFriendPop(userInfo)
                            }

                        })
                }
            })
    }

    private fun showAddFriendPop(userInfo: UserDetailInfo): BasePopupView {
        return DialogUtils.showConfirmDialog(
            "添加好友",
            {
                addFriend(userInfo)
            },
            {
            },
            content = "是否同意花费${userInfo.needRoseNum}玫瑰，添加好友？",
            cancel = "取消",
            confirm = "加好友",
            cancelBg = cn.yanhu.baselib.R.drawable.shape_cancel_btn_r30
        )
    }

    private fun addFriend(userInfo: UserDetailInfo) {
        request({ agoraRxApi.addFriend(userInfo.userId) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    userInfo.isFriend = true
                    ChatUserInfoManager.saveUserInfo(userInfo)
                    EmMsgManager.sendCmdMessagePeople(
                        userInfo.userId,
                        CmdMsgTypeConfig.ADD_FRIEND,
                        null
                    )
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    if (code == ErrorCode.CODE_NO_BALANCE){
                        showRechargePop()
                    }
                }
            })
    }

    private fun showRechargePop() {
        ApplicationProxy.instance.showRechargePop(mContext, true)
    }

    private fun getRoomDetail() {
        mViewModel.getRoomDetail(roomId, roomType)
    }

    protected fun refreshSeatInfo(uid: Int = 0) {
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

    open fun refreshSeatInfo(it: MutableList<RoomSeatInfo>, uid: Int) {

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
        mViewModel.closeRoomObserver.observe(this){
            parseState(it,{
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
                    sendMessage("进入了房间", ChatRoomMsgInfo.ITEM_WELCOME_TYPE)
                    AgoraManager.getInstence().isInitSuccess = true
                }

                override fun onError(error: Int, errorMsg: String?) {
                    logcom("加入聊天室失败，$error————msg$errorMsg")
                    showToast("直播间异常，请重新尝试进入直播间")
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
//        val chatRoomGiftMsg = ChatRoomGiftMsg(user)
//        chatRoomGiftMsg.giftStayTime = 1500
//        mBinding.userEnterView.put(chatRoomGiftMsg)
    }


    private fun initRvChatTop() {
        mBinding.rvSeat.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val maxTop: Int =
                    mBinding.rvSeat.height + CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_84)
                ViewUtils.setMarginTop(mBinding.rvChat, maxTop)
                ViewUtils.setMarginBottom(mBinding.rvChat, 0)
                mBinding.rvSeat.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }


    /**
     * @param isShow true软键盘显示 或者表情ui显示
     */
    private fun setRvChatMessageTop(isShow: Boolean) {
        mBinding.rvSeat.post {
            val maxTop: Int =
                mBinding.rvSeat.height + CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_84)
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
                }
            }
            scrollChatToBottom(400)
        }

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
        logcom(LiveRoomActivity.LIVE_ROOM_TAG, "加载房间---roomId${roomId}")
        //声网初始化
        AgoraManager.getInstence().setVideoEncoderConfiguration(250, 280)
        AgoraManager.getInstence().setRtcEngineEventHandlerListener(this)
        return AgoraManager.getInstence()
            .joinChannel(AppCacheManager.userId.toInt(), roomId, roomSourceBean.agoraToken)

    }


    private fun destroyRoom() {
        // 移除聊天室回调
        roomLeave()
        closeMiniWindow()
        EMClient.getInstance().chatroomManager().leaveChatRoom(roomSourceBean.uid)
        handler?.removeCallbacksAndMessages(null)
        EMClient.getInstance().chatroomManager().removeChatRoomListener(this)
        AgoraManager.getInstence().leaveChannel()
        clearAgora()
        logcom(LiveRoomActivity.LIVE_ROOM_TAG, "销毁房间---roomId${roomId}")
        pauseAnimView()
        clearAnimView()
    }

    private fun clearAgora() {
        AgoraManager.getInstence().setDownVideo(AppCacheManager.userId.toInt(), true)
        AgoraManager.getInstence().clearRtcConnection()
        AgoraManager.getInstence().onDestory()
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

    open fun onCustomStop(){
        var isFloatPermission = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isFloatPermission = Settings.canDrawOverlays(mContext)
        }
        if (!mContext.isFinishing && !isLeave && isFloatPermission) {
            showFloatWindow(2)
        }
    }

    fun exitRoom(){
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
                //refreshSeatInfo(uid)
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
            AgoraManager.getInstence().setDownVideo(uid, localUserId == uid)
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
    private fun operateLeave(operatedUserId: Int) {
        logcom("强制下麦：$operatedUserId")
        mViewModel.operateLeave(
            roomId,
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
            logcom("map地址：" + leaveMap.get(leaveUserId))
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
                AgoraPhoneManager.getInstance().renewToken(it)
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

    open fun isInSeatByUserId(uid: Int): Boolean {
        return false
    }

    /**
     * 有用户下麦
     */
    protected open fun userLeaveChanged(uid: Int) {

    }

    /**
     * 设置是否显示 预览页面
     */
    protected open fun userVideoStatusChanged(uid: Int, isShowProload: Boolean) {

    }


    open fun getRoomInfoSuccess() {

    }

    open fun getRoomSeatSuccess(seatList: MutableList<RoomSeatInfo>) {

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
            EventBusKeyConfig.CLOSELIVEROOM,
            this@BaseLiveRoomFrg.roomId
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
                mContext,
                LiveRoomManager.HOUSE_OFF,
                "",
                this@BaseLiveRoomFrg.roomId
            )

        }
        isLeave = true
        leaveRoomFinish()
    }

    //是否开、关麦提示弹窗
    protected fun switchMikeAlert(mikeUse: Boolean, seatNum: Int) {
        if (mikeUse) {
            switchMike(1, seatNum, localUserId)
        } else {
            DialogUtils.showConfirmDialog("关闭麦克风", {
                switchMike(0, seatNum, localUserId)
            }, {
            }, "当前正在交友中，确定关闭麦克风吗？", cancel = "取消", confirm = "确定关闭")
        }
    }


    /*
     * 打开/关闭麦克风，0：关闭，1：打开
     * */
    private fun switchMike(operate: Int, seatNum: Int, operatedUserId: Int) {
        mViewModel.switchMike(
            Integer.valueOf(roomId),
            operate,
            seatNum,
            operatedUserId,
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    logcom("打开/关闭麦克风")
                    //判断是否本人开关麦
                    if (localUserId != operatedUserId) {
                        val map: MutableMap<String, Any> = HashMap()
                        map["position"] = seatNum
                        EmMsgManager.sendCmdMessagePeople(
                            roomSourceBean.uid,
                            if (operate == 0) ChatConstant.ACTION_MSG_CLOSE_MIKE_ASSIGN else ChatConstant.ACTION_MSG_OPEN_MIKE_ASSIGN,
                            map, EMMessage.ChatType.ChatRoom
                        )
                    } else {
                        updateMyMicStatus(operate, seatNum)
                    }
                }
            })
    }

    private fun updateMyMicStatus(operate: Int, seatNum: Int) {
        AgoraManager.getInstence().muteLocalAudioStream(operate == 0)
        val seatPosition = seatNum - 1
        val mickUser = operate == 1
        refreshSeatMicStatus(seatPosition,mickUser)
        val map: MutableMap<String, Any> = HashMap()
        map["position"] = seatNum
        map["mike_use"] = operate
        EmMsgManager.sendCmdMessagePeople(
            roomSourceBean.uid,
            ChatConstant.ACTION_MSG_SWITCH_MIKE,
            map,
            EMMessage.ChatType.ChatRoom
        )
    }

     open fun refreshSeatMicStatus(seatPosition:Int,mickUser:Boolean){}

    override fun onMemberJoined(roomId: String?, participant: String?) {
        logcom("新朋友加入聊天室：$participant")
        if (isNotMyRoom(roomId)) {
            return
        }
        if (isOwner) {
            getRoomDetail()
        }
    }

    override fun onMemberExited(roomId: String?, roomName: String?, participant: String?) {
        logcom("有成员退出聊天室：$roomName——————$participant")
        if (isNotMyRoom(roomId)) {
            return
        }
        val userId = participant!!.toInt()
        AgoraManager.getInstence().setDownVideo(userId, localUserId == userId)
        updateUserLeaveView(userId)
        if (isOwner) {
            getRoomDetail()
        }
    }

    override fun onRemovedFromChatRoom(
        reason: Int,
        roomId: String?,
        roomName: String?,
        participant: String?
    ) {
    }

    override fun onMuteListAdded(
        chatRoomId: String?,
        mutes: MutableList<String>?,
        expireTime: Long
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
}