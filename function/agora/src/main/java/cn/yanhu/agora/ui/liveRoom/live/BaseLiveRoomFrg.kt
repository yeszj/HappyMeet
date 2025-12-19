package cn.yanhu.agora.ui.liveRoom.live

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
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import cn.happy.beautyface.ui.utils.BeautyManager
import cn.happy.beautyface.ui.utils.SenseTimeBeautySDK
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.UserEnterAdapter
import cn.yanhu.agora.adapter.liveRoom.LiveRoomChatMessageAdapter
import cn.yanhu.agora.adapter.liveRoom.ThreeRoomSeatAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.AngleRankInfo
import cn.yanhu.agora.bean.AngleRoomResultInfo
import cn.yanhu.agora.bean.ChatRoomMsgInfo
import cn.yanhu.agora.bean.GiftSendCntInfo
import cn.yanhu.agora.bean.InviteSeatRecord
import cn.yanhu.agora.bean.PkConfigInfo
import cn.yanhu.agora.bean.PkSeatUserInfo
import cn.yanhu.agora.bean.RoomExtraInfo
import cn.yanhu.agora.bean.RoomGroupMemberRes
import cn.yanhu.agora.bean.RoomLeaveResponse
import cn.yanhu.agora.databinding.FrgBaseLiveRoomBinding
import cn.yanhu.agora.listener.IRtcEngineEventHandlerListener
import cn.yanhu.agora.listener.OnSendSeatInviteListener
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.agora.manager.VideoCanvasPool
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
import cn.yanhu.agora.pop.RoomGroupMemberPop
import cn.yanhu.agora.pop.RoomPkSendPop
import cn.yanhu.agora.pop.SeatUserOperatePop
import cn.yanhu.agora.pop.SelectPkUserPop
import cn.yanhu.agora.pop.SendMessagePop
import cn.yanhu.agora.pop.ToolDialog
import cn.yanhu.agora.queuetask.ApplySeatTask
import cn.yanhu.agora.queuetask.SendCmdTask
import cn.yanhu.agora.ui.liveRoom.LiveRoomViewModel
import cn.yanhu.baselib.anim.AnimManager
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.queue.TaskQueueManagerImpl
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.countDown
import cn.yanhu.baselib.utils.ext.logComToFile
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.setClickScaleListener
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.adapter.GiftAnimAdapter
import cn.yanhu.commonres.api.commonRxApi
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.ChatRoomGiftMsg
import cn.yanhu.commonres.bean.CommonTipsInfo
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.bean.RoomListBean.Companion.TYPE_SEVEN_SONG
import cn.yanhu.commonres.bean.RoomPkInfo
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.bean.SeatUserInfo
import cn.yanhu.commonres.bean.SendGiftRequest
import cn.yanhu.commonres.bean.StickyInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.bean.response.GiftResponse
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.CmdMsgTypeConfig
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.ImMessageParamsConfig
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.manager.RoomSwitchCacheManager
import cn.yanhu.commonres.manager.ServiceConfigKeyManager
import cn.yanhu.commonres.pop.CommonTipDialog
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.task.GiftPopAnimTask
import cn.yanhu.commonres.utils.PermissionXUtils
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.db.ChatUserInfoManager
import cn.yanhu.imchat.manager.CutLiveRoomUtils
import cn.yanhu.imchat.manager.EmMsgManager
import cn.yanhu.imchat.manager.ImUserManager
import cn.yanhu.imchat.manager.SendGiftCheckManager
import cn.yanhu.imchat.pop.ChatListDialog
import cn.yanhu.imchat.pop.SendGiftPop
import cn.zj.netrequest.BuildConfig
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
import com.blankj.utilcode.util.VibrateUtils
import com.chad.library.adapter4.BaseMultiItemAdapter
import com.chad.library.adapter4.BaseQuickAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hyphenate.EMCallBack
import com.hyphenate.EMChatRoomChangeListener
import com.hyphenate.EMError
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMChatRoom
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMTextMessageBody
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.pcl.sdklib.listener.OnPayResultListener
import com.pcl.sdklib.manager.PayManager
import com.yhao.floatwindow.PermissionListener
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.ceil


/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
@Suppress("DEPRECATION")
open class BaseLiveRoomFrg : BaseFragment<FrgBaseLiveRoomBinding, LiveRoomViewModel>(
    R.layout.frg_base_live_room, LiveRoomViewModel::class.java
), IRtcEngineEventHandlerListener, EMChatRoomChangeListener {

    var roomId: String = ""
    protected var roomType: Int = 0
    private val chatRoomMsgAdapter by lazy { LiveRoomChatMessageAdapter() }
    private var messageDialog: SendMessagePop? = null
    private var currentUser: BaseUserInfo? = null
    private var isShowEmoji = false
    lateinit var roomSourceBean: RoomDetailInfo

    var isOwner: Boolean = false
    protected var localUserId: Int = 0
    protected var localStrUserId: String = "0"

    var seatList = mutableListOf<RoomSeatInfo>()
    lateinit var seatUserAdapter: BaseMultiItemAdapter<RoomSeatInfo>
    private var seatHeight = 0
    private var sevenSeatHeight = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_142) * 3
    private var nineSeatHeight = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_360)
    private var nineSongSeatHeight = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_80) * 5
    private var sevenSongSeatHeight = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_120) * 4
    private var ifMute = false//是否被禁言
    protected var hasExpand = false
    private var needComboCnt = 10
    private var isShowContinueClick: Boolean = false//普通礼物是否显示连击
    private var selfUserInfo: UserDetailInfo? = null
    override fun initData() {
        selfUserInfo = ImUserManager.getSelfUserInfo()
        initPageData()
        seatList = roomSourceBean.roomSeatResList
        hasExpand = seatList.indexOfFirst { it.isExpand } >= 0
        isSendAll = false
        setRvSeatHeight()
        setRvChatMessageTop(false)
        initSeatStatus()
        getCurrentSendGiftPop()?.dismiss()
        AgoraManager.getInstance().setRtcEngineEventHandlerListener(this)
        getGiftComboSwitch()
        mBinding.roomInfo = roomSourceBean
        getRoomInfoSuccess()
        loadPkConfigInfo()
        setSeatApplyStatus()
        initRvChatTop()
        initChatMsgAdapterData()
        setUserEnterAdapter()
        joinChatRoom()
        joinChannel()
        requestData()
        setUnReadMsgCount()
        if (roomSourceBean.isSongRoom()) {
            getRoomExtraInfo()
        }
        setGiftNormalAnimAdapter()
        getRoseGift()
        setApplySeatNum()
    }


    private fun setApplySeatNum() {
        if (!roomSourceBean.autoSeat && !roomSourceBean.isThreeRoom() && isOwner) {
            request(
                { agoraRxApi.getInviteList(roomId, "0", "0", 1) },
                object : OnRequestResultListener<MutableList<UserDetailInfo>> {
                    @SuppressLint("SetTextI18n")
                    override fun onSuccess(data: BaseBean<MutableList<UserDetailInfo>>) {
                        val userList = data.data ?: return
                        refreshApplySeatNum(userList)
                    }
                }, isShowToast = false
            )
        }

    }

    @SuppressLint("SetTextI18n")
    private fun refreshApplySeatNum(userList: MutableList<UserDetailInfo>) {
        var num = 0
        userList.forEach {
            if (it.seatNum > 0) {
                num++
            }
        }
        applySeatCount = num
        mBinding.tvApplyNum.text = "${applySeatCount}人"
    }

    private fun getGiftComboSwitch() {
        request(
            { commonRxApi.getConfigInfo(ServiceConfigKeyManager.GIF_COMBO_SWITCH) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    val value = data.data
                    isShowContinueClick = if (roomSourceBean.isPrivateRoom()) {
                        false
                    } else {
                        value == "1"
                    }
                }
            })
    }

    private var pkConfigInfo: PkConfigInfo? = null
    private fun loadPkConfigInfo() {
        if (roomSourceBean.isShowPkFunc() && isOwner) {
            mViewModel.getPkConfigInfo(roomId, object : OnRequestResultListener<PkConfigInfo> {
                override fun onSuccess(data: BaseBean<PkConfigInfo>) {
                    pkConfigInfo = data.data
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    logInfoCom("loadPkConfigInfo fail $code $msg")
                }
            })
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

            TYPE_SEVEN_SONG, RoomListBean.TYPE_SEVEN_FRIEND, RoomListBean.TYPE_SEVEN_ANGLE -> {
                seatHeight = if (hasExpand) {
                    sevenSongSeatHeight
                } else {
                    sevenSeatHeight
                }
            }

            RoomListBean.TYPE_NINE_SONG, RoomListBean.TYPE_NINE_FRIEND, RoomListBean.TYPE_NINE_ANGLE -> {
                seatHeight = if (hasExpand) {
                    nineSongSeatHeight
                } else {
                    nineSeatHeight
                }
            }

            RoomListBean.TYPE_PUBLIC, RoomListBean.TYPE_PRIVATE -> {
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
        localStrUserId = AppCacheManager.userId
        localUserId = localStrUserId.toInt()
        roomSourceBean = requireArguments().getSerializable(IntentKeyConfig.DATA) as RoomDetailInfo
        mBinding.rvSeat.itemAnimator?.changeDuration = 0
        mBinding.rvChat.itemAnimator?.changeDuration = 0
        mBinding.rvChat.adapter = chatRoomMsgAdapter
        roomId = roomSourceBean.roomId.toString()
        roomType = roomSourceBean.roomType
        isOwner = roomSourceBean.isOwner()
        checkIfMute()
    }

    private fun checkIfMute() {
        EMClient.getInstance().chatroomManager()
            .asyncCheckIfInMuteList(roomSourceBean.uid, object : EMValueCallBack<Boolean> {
                override fun onSuccess(inMuteList: Boolean) {
                    ifMute = inMuteList
                }

                override fun onError(error: Int, errorMsg: String) {
                }
            })
    }

    private fun initSeatStatus() {
        if (isOwner) {
            mBinding.ivSeatStatus.visibility = View.INVISIBLE
            mBinding.vgApplySeat.visibility = View.GONE
            if (!roomSourceBean.autoSeat && !roomSourceBean.isThreeRoom()) {
                mBinding.vgApplyList.visibility = View.VISIBLE
            } else {
                mBinding.vgApplyList.visibility = View.GONE
            }
        } else {
            mBinding.ivSeatStatus.visibility = View.INVISIBLE
            mBinding.vgApplySeat.visibility = View.VISIBLE
            mBinding.vgApplyList.visibility = View.GONE
        }
    }

    private fun initChatMsgAdapterData() {
        chatRoomMsgAdapter.add(
            ChatRoomMsgInfo(
                ChatRoomMsgInfo.ITEM_SYSTEM_TYPE,
                "平台公示：平台只提供交友介绍认识的服务，我们提倡文明直播、积极阳光交友，严禁涉黄、涉政、涉恐、低俗、辱骂等行为。发现违规行为将被封禁。保护网络绿色环境，从你我做起。",
                null
            )
        )
    }


    open fun refreshOnlineUser(onlineNum: Int) {}

    private var onlineUserListPop: LiveRoomOnlineUserPop? = null

    /**
     * 在线用户列表弹框
     */
    protected fun showOnlineUserList() {
        if (CommonUtils.isPopShow(onlineUserListPop)) {
            return
        }
        onlineUserListPop = LiveRoomOnlineUserPop.showDialog(
            mContext, mutableListOf(), roomSourceBean, inviteSeatListener
        )
    }


    val inviteSeatListener = object : OnSendSeatInviteListener {
        override fun onSendInvite(map: MutableMap<String, Any>, userInfo: UserDetailInfo) {
            val inviteRecord = InviteRecordCacheManager.getInviteRecordByUserId(userInfo.userId)

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

                } catch (_: Exception) {
                    -1
                }
            }
        })
    }

    /**
     * 设置申请上麦按钮状态
     */
    private fun setSeatApplyStatus() {
        if (isInSeatByUserId(localUserId)) {
            setHasSeatUpStatus()
        } else {
            setSeatStatus()
        }
    }

    private fun joinChannel() {
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<Int>() {
            override fun onSuccess(joinChannel: Int) {
                if (joinChannel == 0 || abs(joinChannel) == 17) {
                    logcom("加入直播间成功")
                    startStatusCheck()
                    refreshSeatInfo()
                } else {
                    showToast("加入直播间失败，请重新尝试进入直播间")
                    logComToFile(TAG, "加入直播间失败:joinChannel = $joinChannel")
                    leaveRoomFinish()
                }
            }

            override fun doInBackground(): Int {
                return try {
                    preJoinRoom()
                } catch (_: Exception) {
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
            addUserEnterAnim(selfUserInfo)
        }

    }

    private fun isFreeSeat(): Boolean {
        return if (AppCacheManager.isMan()) {
            !seatList[1].seatUserRose
        } else {
            !seatList[2].seatUserRose
        }
    }

    private fun getSeatInRoseNum(): String {
        return if (AppCacheManager.isMan()) {
            seatList[1].seatRoseNum.toString()
        } else {
            seatList[2].seatRoseNum.toString()
        }
    }

    private var seatStatus = 0
    override fun initListener() {
        mBinding.vgApplyList.setOnSingleClickListener {
            showSeatUserList()
        }
        chatRoomMsgAdapter.addOnItemChildClickListener(
            R.id.userAvatar, object : BaseQuickAdapter.OnItemChildClickListener<ChatRoomMsgInfo> {
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
        chatRoomMsgAdapter.addOnItemChildLongClickListener(
            R.id.userAvatar,
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
//        LiveEventBus.get<Int>(EventBusKeyConfig.UNREAD_COUNT).observe(this) {
//        }
        LiveEventBus.get<String>(LiveDataEventManager.UPDATE_LIVE_ROOM_SELF_INFO).observe(this) {
            selfUserInfo = ImUserManager.getSelfUserInfo()
        }
        LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_CHAT_MSG_TOP).observe(this) {
            if (!it) {
                if (isOwner && roomSourceBean.isShowPkFunc()) {
                    mBinding.btnPk.visibility = View.VISIBLE
                }
                updatePkResult(null, true)
            }
            changeRvChatScroll()
        }
        LiveEventBus.get<String>(LiveDataEventManager.REFRESH_USER_CACHE).observe(this) {
            if (CommonUtils.isPopShow(seatUserOperatePop)) {
                request(
                    { agoraRxApi.getUserInfoByUserId(seatUserOperatePop!!.userInfo.userId) },
                    object : OnRequestResultListener<UserDetailInfo> {
                        override fun onSuccess(data: BaseBean<UserDetailInfo>) {
                            if (CommonUtils.isPopShow(seatUserOperatePop)) {
                                seatUserOperatePop?.refreshUserInfo(data.data!!)
                                return
                            }
                        }
                    })
            }
        }
        mBinding.ivSetting.setOnSingleClickListener {
            showToolDialog()
        }
        mBinding.vgClickRose.setClickScaleListener {
            chatRoomRoseGiftMsg?.apply {
                if (TextUtils.isEmpty(this.giftInfo.svga)) {
                    sendTargetUser?.apply {
                        sendPopGift(this, roseGiftInfo!!)
                    }
                } else {
                    sendPopGift(this.targetUserInfo as UserDetailInfo, this.giftInfo)
                }
            }

        }
        mBinding.btnPk.setOnSingleClickListener {
            showRoomPkSendPop()
        }
        LiveEventBus.get<RoomPkInfo>(EventBusKeyConfig.CONTINUEPK).observe(this) {
            showRoomPkSendPop(it)
        }
        PayManager.registerPayResult(mContext, object : OnPayResultListener {
            override fun onPaySuccess() {
                getRoseGift()
            }
        })
        LiveEventBus.get<Boolean>(EventBusKeyConfig.CHANGEAPPLYPOPSTATUS).observe(this) {
            isCloseApplySeatPop = it
            if (isCloseApplySeatPop) {
                applyQueueTask.clear()
            }
        }
        LiveEventBus.get<Boolean>(EventBusKeyConfig.SWITCH_TO_FOREGROUND).observe(this) {
            //app从后台切换到前台 更新麦位
            refreshSeatInfo()
        }
        registerNetChange()
    }

    private var roomPkSendPop: RoomPkSendPop? = null
    private fun showRoomPkSendPop(roomPkInfo: RoomPkInfo? = null) {
        if (CommonUtils.isPopShow(roomPkSendPop)) {
            return
        }
        if (roomPkInfo != null) {
            pkConfigInfo?.pkTypeDefaultIndex = if (roomPkInfo.pkType == 0) 1 else 0
            if (AppCacheManager.selectTimeIndex >= 0) {
                pkConfigInfo?.pkTimeDefaultIndex = AppCacheManager.selectTimeIndex
            }
        }
        roomPkSendPop = RoomPkSendPop.showDialog(
            mContext,
            pkConfigInfo,
            roomSourceBean,
            object : RoomPkSendPop.OnSelectUserListener {
                override fun onSelectUser(isSelectRedUser: Boolean, isSingMode: Boolean) {
                    showSelectPkUserPop(isSelectRedUser, isSingMode)
                }

                override fun onDeleteUser(userId: String, isSelectRedUser: Boolean) {
                    if (isSelectRedUser) {
                        selectRedUserList.removeIf { it.userId == userId }
                    } else {
                        selectBlueUserList.removeIf { it.userId == userId }
                    }
                }

                override fun onSelectUserResult(
                    userList: MutableList<PkSeatUserInfo>, isSelectRedUser: Boolean
                ) {
                    if (isSelectRedUser) {
                        selectRedUserList = userList
                    } else {
                        selectBlueUserList = userList
                    }
                }
            },
            object : SimpleCallback() {
                override fun onShow(popupView: BasePopupView?) {
                    //点击继续pk 弹框中默认显示上次选中的
                    if (roomPkInfo != null) {
                        val redMemberList = roomPkInfo.redMemberList
                        val blueMemberList = roomPkInfo.blueMemberList
                        val list =
                            if (roomSourceBean.getFragmentType() == RoomListBean.FRG_THREE_ROOM) {
                                seatUserAdapter.items
                            } else {
                                seatList
                            }
                        list.forEach {
                            val roomUserSeatInfo = it.roomUserSeatInfo
                            if (roomUserSeatInfo != null) {
                                if (redMemberList.contains(roomUserSeatInfo.userId)) {
                                    val pkSeatUserInfo = PkSeatUserInfo(it.id - 1, 1)
                                    pkSeatUserInfo.userId = roomUserSeatInfo.userId
                                    pkSeatUserInfo.portrait = roomUserSeatInfo.portrait
                                    selectRedUserList.add(pkSeatUserInfo)
                                } else if (blueMemberList.contains(roomUserSeatInfo.userId)) {
                                    val pkSeatUserInfo = PkSeatUserInfo(it.id - 1, 2)
                                    pkSeatUserInfo.userId = roomUserSeatInfo.userId
                                    pkSeatUserInfo.portrait = roomUserSeatInfo.portrait
                                    selectBlueUserList.add(pkSeatUserInfo)
                                }
                            }
                        }
                        var isSingMode = pkConfigInfo?.pkTypeDefaultIndex == 0
                        val maxCount = getMaxCount(isSingMode)
                        roomPkSendPop?.setSelectUser(selectRedUserList, true, maxCount)
                        roomPkSendPop?.setSelectUser(selectBlueUserList, false, maxCount)
                    }
                }

                override fun onDismiss(popupView: BasePopupView?) {
                    super.onDismiss(popupView)
                    selectRedUserList.clear()
                    selectBlueUserList.clear()
                }
            })

    }

    private var selectPkUserPop: SelectPkUserPop? = null
    fun showSelectPkUserPop(isSelectRedUser: Boolean, isSingMode: Boolean) {
        if (CommonUtils.isPopShow(selectPkUserPop)) {
            return
        }
        val maxCount = getMaxCount(isSingMode)
        selectPkUserPop = SelectPkUserPop.showDialog(
            mContext,
            isSelectRedUser,
            getPkSeatUserList(),
            maxCount,
            object : SelectPkUserPop.OnSelectUserListener {
                override fun onSelectUser(userList: MutableList<PkSeatUserInfo>) {
                    if (isSelectRedUser) {
                        selectRedUserList = userList
                    } else {
                        selectBlueUserList = userList
                    }
                    roomPkSendPop?.setSelectUser(userList, isSelectRedUser, maxCount)
                    selectPkUserPop?.dismiss()

                }
            })
    }

    private fun getMaxCount(isSingMode: Boolean): Int {
        val maxCount = if (isSingMode) {
            1
        } else {
            if (roomSourceBean.getFragmentType() == RoomListBean.FRG_SEVEN_ROOM || roomSourceBean.roomType == RoomListBean.TYPE_SEVEN_SONG) {
                6
            } else {
                8
            }
        }
        return maxCount
    }

    private var selectRedUserList = mutableListOf<PkSeatUserInfo>()
    private var selectBlueUserList = mutableListOf<PkSeatUserInfo>()


    protected open fun getPkSeatUserList(): MutableList<PkSeatUserInfo> {
        val pkUserList = mutableListOf<PkSeatUserInfo>()
        seatList.forEach {
            val roomUserSeatInfo = it.roomUserSeatInfo
            if (roomUserSeatInfo != null) {
                val list = mutableListOf<PkSeatUserInfo>()
                list.addAll(selectRedUserList)
                list.addAll(selectBlueUserList)
                val hasSelectUser = list.find { it.userId == roomUserSeatInfo.userId }
                val pkSeatUserInfo = if (hasSelectUser != null) {
                    hasSelectUser
                } else {
                    val userInfo = PkSeatUserInfo(it.id - 1, 0)
                    userInfo.portrait = roomUserSeatInfo.portrait
                    userInfo.userId = roomUserSeatInfo.userId
                    userInfo
                }
                pkUserList.add(pkSeatUserInfo)
            }
        }
        selectPkUserPop?.refreshSeatList(pkUserList)
        return pkUserList
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
        request(
            { agoraRxApi.getConfigInfo(ServiceConfigKeyManager.KEY_ROOM_TOP_TIMES) },
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
        adminCloseRoomReasonPop = AdminCloseRoomReasonPop.showDialog(
            mContext,
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

    protected fun showSendGiftPop(roomUserSeatInfo: UserDetailInfo, isGetUser: Boolean = true) {
        getGiftComboSwitch()
        if (isGetUser) {
            request(
                { agoraRxApi.getUserInfoByUserId(roomUserSeatInfo.userId) },
                object : OnRequestResultListener<UserDetailInfo> {
                    override fun onSuccess(data: BaseBean<UserDetailInfo>) {
                        if (getCurrentSendGiftPop()?.isVisible == true) {
                            getCurrentSendGiftPop()?.showAddFriendsBtn(data.data!!)
                        }
                    }
                })
        }
        startSendComboGift(chatRoomRoseGiftMsg)
        showGiftPop(roomUserSeatInfo, isGetUser)
    }

    private var sendGiftPopWeakRef: WeakReference<SendGiftPop>? = null
    private fun getCurrentSendGiftPop(): SendGiftPop? {
        return sendGiftPopWeakRef?.get()
    }

    private fun clearSendGiftPopReference() {
        sendGiftPopWeakRef?.clear()
        sendGiftPopWeakRef = null
    }

    private fun showGiftPop(roomUserSeatInfo: UserDetailInfo, isGetUser: Boolean = true) {
        logInfoCom("showGiftPop = click")
        val currentPop = sendGiftPopWeakRef?.get()

        if (currentPop?.isVisible == true) {
            logInfoCom("showGiftPop = return")
            DialogUtils.dismissLoading()
            return
        }
        // 清理之前的引用
        clearSendGiftPopReference()
        roomUserSeatInfo.roomId = roomId.toInt()
        val listener = object : SendGiftPop.OnSendGiftListener {

            override fun onSendGift(item: GiftInfo, isCombo: Boolean) {
                getCurrentSendGiftPop()?.dismiss()
                clearSendGiftPopReference()
                if (isCombo) {
                    sendPopGift(roomUserSeatInfo, item)
                } else {
                    sendGiftSuccess(item, roomUserSeatInfo)
                }
            }

            override fun onShowUserInfo(userId: String) {
                showUserPop(userId)
            }

            override fun onAddFriend() {
                showAddFriendPop(roomUserSeatInfo)
            }

            override fun onShowFriendBtn() {
                if (!isGetUser) {
                    if (getCurrentSendGiftPop()?.isVisible == true) {
                        getCurrentSendGiftPop()?.showAddFriendsBtn(roomUserSeatInfo)
                    }
                }
            }

            override fun onSendAll(item: GiftInfo, balance: String) {
                super.onSendAll(item, balance)
                getCurrentSendGiftPop()?.dismiss()
                clearSendGiftPopReference()
                if (isSendAll) {
                    return
                }
                sendPosition = 0
                isSendAll = true
                val needSendUserInfo = mutableListOf<UserDetailInfo>()
                seatList.forEach {
                    val roomUserSeatInfo = it.roomUserSeatInfo
                    roomUserSeatInfo?.apply {
                        if (this.userId != localStrUserId) {
                            needSendUserInfo.add(this)
                        }
                    }
                }
                val price = item.price * needSendUserInfo.size
                if (CommonUtils.compareString(price.toString(), balanceRose)) {
                    showRechargePop()
                    isSendAll = false
                    return
                }
                balanceRose = CommonUtils.subBigDecimal(balanceRose, BigDecimal(price))
                if (needSendUserInfo.isNotEmpty()) {
                    startSendGift(item, needSendUserInfo[0], needSendUserInfo)
                } else {
                    logComToFile("sendGiftPop", "没有需要赠送的礼物用户")
                    isSendAll = false
                }
            }
        }
        val sendGiftPop = SendGiftPop.showDialog(
            mContext, roomUserSeatInfo, roomType, 0, listener, isShowContinueClick, balanceRose
        )
        sendGiftPopWeakRef = WeakReference(sendGiftPop)
    }


    var clickCount = 0
    private var sendTargetUser: UserDetailInfo? = null
    protected var chatRoomRoseGiftMsg: ChatRoomGiftMsg? = null


    private var sendCmdQueueTask: TaskQueueManagerImpl = TaskQueueManagerImpl()

    fun checkSendGift(targetUserInfo: UserDetailInfo, giftInfo: GiftInfo) {
        if (mBinding.vgClickRose.isInvisible) {
            SendGiftCheckManager.checkSendGift(
                targetUserInfo.userId,
                giftInfo.id,
                object : SendGiftCheckManager.OnCheckGiftListener {
                    override fun onCanSend() {
                        sendPopGift(targetUserInfo, giftInfo)
                    }
                })
        } else {
            sendPopGift(targetUserInfo, giftInfo)
        }

    }

    fun sendPopGift(
        roomUserSeatInfo: UserDetailInfo,
        giftInfo: GiftInfo
    ) {
        if (roomUserSeatInfo.userId == localStrUserId) {
            showToast("不能给自己送礼")
            return
        }
        if (!CommonUtils.compareString(balanceRose.toPlainString(), giftInfo.price.toString())) {
            showToast("余额不足")
            showRechargePop()
            return
        }
        if (countDown != null && (sendTargetUser?.userId != roomUserSeatInfo.userId || chatRoomRoseGiftMsg?.giftInfo?.id != giftInfo.id) && chatRoomRoseGiftMsg != null) {
            startSendComboGift(chatRoomRoseGiftMsg)
            logInfoCom("startSendComboGift", "赠送对象发生改变")
        }
        chatRoomRoseGiftMsg =
            ChatRoomGiftMsg(selfUserInfo!!, roomUserSeatInfo, giftInfo)
        VibrateUtils.vibrate(50)
        balanceRose = CommonUtils.subBigDecimal(balanceRose, BigDecimal(giftInfo.price))
        if (countDown == null || sendTargetUser?.userId != roomUserSeatInfo.userId) {
            clickCount = 0
        }


        mBinding.vgClickRose.visibility = View.VISIBLE
        sendCmdQueueTask.addTask(
            SendCmdTask(
                roomSourceBean.uid,
                GsonUtils.toJson(chatRoomRoseGiftMsg),
                if (giftInfo.type == GiftInfo.TYPE_ROSE) ChatConstant.ACTION_SEND_ROSE else ChatConstant.ACTION_SEND_GIFT
            )
        )
        mBinding.progress.setCountdownTime(3000)
        mBinding.progress.startCountdown()
        startCountTime(chatRoomRoseGiftMsg!!)
        clickCount++
        if (giftInfo.type == GiftInfo.TYPE_ROSE) {
            mBinding.roseAnimView.addRose()
        } else {
            playSvga(chatRoomRoseGiftMsg!!)
        }
        sendCnt(chatRoomRoseGiftMsg!!, clickCount > 1)
        sendTargetUser = roomUserSeatInfo
        mBinding.tvNum.setStrokeText(" x$clickCount ")

    }

    private var sendCount = 0
    private var sendCntId = ""
    private fun sendCnt(chatRoomGiftMsg: ChatRoomGiftMsg, isCombo: Boolean) {
        logInfoCom("sendCnt", "isCombo:$isCombo,sendCntId = $sendCntId")
        if (!isCombo && TextUtils.isEmpty(sendCntId)) {
            sendCount++
            logInfoCom("sendCnt", "sendCount:$sendCount")
            return
        }
        val sendGiftRequest = SendGiftRequest()
        val giftInfo = chatRoomGiftMsg.giftInfo
        val sendUserInfo = chatRoomGiftMsg.targetUserInfo
        sendGiftRequest.roomId = roomId
        sendGiftRequest.toUid = sendUserInfo.userId
        sendGiftRequest.giftId = giftInfo.id
        sendGiftRequest.num = 1
        sendGiftRequest.source = SendGiftRequest.SOURCE_LIVE_ROOM
        sendGiftRequest.sendCntId = sendCntId
        sendGiftRequest.callId = 0
        logInfoCom("sendCnt", "sendGiftRequest:${GsonUtils.toJson(sendGiftRequest)}")
        request2(
            { agoraRxApi.sendCnt(sendGiftRequest) },
            object : OnRequestResultListener<GiftSendCntInfo> {
                override fun onSuccess(data: BaseBean<GiftSendCntInfo>) {
                    chatRoomRoseGiftMsg?.sendCntId = data.data!!.sendCntId
                    sendCntId = data.data!!.sendCntId
                    logInfoCom(
                        "sendCnt",
                        "sendGiftRequest:发送成功 isCombo = ${isCombo},sendCount=${sendCount},sendCntId = ${chatRoomRoseGiftMsg?.sendCntId}"
                    )
                    if (!isCombo && sendCount > 0) {
                        logInfoCom("sendCnt", "sendGiftRequest:首次送成功")
                        for (i in 0 until sendCount) {
                            logInfoCom("sendCnt", "遍历开始")
                            sendCnt(chatRoomGiftMsg, true)
                        }
                        sendCount = 0
                    }
                }
            })
    }


    private var sendPosition = 0
    private var isSendAll = false;
    private fun startSendGift(
        item: GiftInfo,
        userInfo: UserDetailInfo,
        needSendUserInfo: MutableList<UserDetailInfo> = mutableListOf<UserDetailInfo>()
    ) {
        val sendGiftRequest = SendGiftRequest()
        sendGiftRequest.roomId = roomId
        sendGiftRequest.toUid = userInfo.userId
        sendGiftRequest.giftId = item.id
        sendGiftRequest.num = 1
        sendGiftRequest.source = SendGiftRequest.SOURCE_LIVE_ROOM
        sendGiftRequest.callId = 0
        request2(
            { imChatRxApi.sendGift(sendGiftRequest) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    showToast("赠送礼物成功")
                    val map = HashMap<String, Any>()
                    map["giftName"] = item.name
                    map["giftIcon"] = item.giftIcon
                    map["num"] = sendGiftRequest.num
                    map["svga"] = item.svga
                    if (item.type == GiftInfo.TYPE_RANDOM_BOX) {
                        item.randomBoxGiftInfo = data.data
                    }
                    sendGiftSuccess(item, userInfo, isSendAll = true)
                    if (needSendUserInfo.isNotEmpty()) {
                        sendNext(needSendUserInfo, item)
                    }
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    if (code == ErrorCode.CODE_NO_BALANCE) {
                        showToast("余额不足")
                        showRechargePop()
                        isSendAll = false
                    } else {
                        if (needSendUserInfo.isNotEmpty()) {
                            sendNext(needSendUserInfo, item)
                        }
                    }
                }
            })
    }

    private fun sendNext(
        needSendUserInfo: MutableList<UserDetailInfo>, item: GiftInfo
    ) {
        sendPosition++
        if (sendPosition < needSendUserInfo.size) {
            startSendGift(item, needSendUserInfo[sendPosition], needSendUserInfo)
        } else {
            isSendAll = false
            sendPosition = 0
        }
    }

    protected fun sendGiftSuccess(
        item: GiftInfo,
        roomUserSeatInfo: UserDetailInfo,
        isSend: Boolean = true,
        isSendAll: Boolean = false
    ) {
        LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.REFRESH_USER_CACHE)
        item.sendNumber = 1
        val chatRoomGiftMsg =
            ChatRoomGiftMsg(selfUserInfo!!, roomUserSeatInfo, item)
        chatRoomGiftMsg.isAllSeatSend = isSendAll
        showFloatAnim(chatRoomGiftMsg)
        //val giftMsgInfo = GiftMsgInfo(item, roomUserSeatInfo)
        if (isSend) {
            logComToFile("sendGift", "赠送礼物成功，发送礼物消息")
            sendMessage(GsonUtils.toJson(chatRoomGiftMsg), ChatRoomMsgInfo.ITEM_GIFT_TYPE)
        }
        playSvga(chatRoomGiftMsg, isSendAll)
        getRoseGift()
    }


    private var isOnNewIntent = false
    fun onNewIntent(intent: Intent) {
        isOnNewIntent = true
        logInfoCom("重新进入")
        if (intent.getBooleanExtra("isFinish", false)) {
            showOffDialog()
        }
        closeMiniWindow()
        addHostSurfaceView()
    }


    protected open fun addHostSurfaceView() {
        if (roomSourceBean.isThreeRoom()) {
            seatUserAdapter.notifyItemChanged(0)
        }
    }

    private fun closeMiniWindow() {
        if (LiveRoomVideoMiniManager.getInstance().isShowing) {
            LiveRoomVideoMiniManager.getInstance().closeMiniWindow()
        }
    }

    private fun showOffDialog() {
        DialogUtils.showConfirmDialog("关闭直播", {
            logComToFile(LiveRoomActivity.LIVE_ROOM_TAG, "手动点击退出房间")
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

    protected open fun setHasSeatUpStatus() {
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
        mViewModel.autoSeat(
            roomId, (if (autoSeat) 1 else 0).toString(), object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    logInfoCom("开启/关闭自动上麦")
                    roomSourceBean.autoSeat = autoSeat
                    refreshAutoSeat()
                    //需要透传消息提示观众房主已开启自动上麦/关闭自动上麦
                    if (autoSeat) {
                        mBinding.vgApplyList.visibility = View.GONE
                        EmMsgManager.sendCmdMessageToChatRoom(
                            roomSourceBean.uid, "", ChatConstant.ACTION_MSG_OPEN_AUTO_SEAT
                        )
                    } else {
                        if (!roomSourceBean.isThreeRoom()) {
                            mBinding.vgApplyList.visibility = View.VISIBLE
                        }
                        EmMsgManager.sendCmdMessageToChatRoom(
                            roomSourceBean.uid, "", ChatConstant.ACTION_MSG_CLOSE_AUTO_SEAT
                        )
                    }
                }
            })
    }

    //获取悬浮窗权限
    open fun showFloatWindow(type: Int) {
        if (type == 1 && !isInSeatByUserId(localUserId)) {
            //如果是点击关闭或者物理键盘返回且不在麦位上直接关闭房间
            DialogUtils.showConfirmDialog("退出房间", {}, {
                logComToFile(LiveRoomActivity.LIVE_ROOM_TAG, "手动点击退出房间")
                closeRoom()
            }, "是否退出房间？", "确认退出", "再等等")
        } else {
            PermissionXUtils.checkAlertPermission(
                mContext,
                if (isOwner) "关闭房间" else "离开房间",
                object : PermissionXUtils.OnAlertPermissionListener {
                    override fun onSuccess() {
                        doShowFloatWindow(type)
                    }

                    override fun onFail() {}
                    override fun onClose() {
                        logComToFile(LiveRoomActivity.LIVE_ROOM_TAG, "手动点击退出房间")
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
            // leave()
        } else {
            if (CommonUtils.isPopShow(roomAngleResultPop)) {
                angelAnimList.add(angleResultInfo)
                return
            }
            roomAngleResultPop = RoomAngleResultPop.showDialog(
                mContext, angleUser, guardUser, type, object : SimpleCallback() {
                    override fun onDismiss(popupView: BasePopupView?) {
                        super.onDismiss(popupView)
                        if (angelAnimList.isNotEmpty()) {
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
            roomId, object : OnRequestResultListener<MutableList<AngleRankInfo>> {
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

    private fun doShowFloatWindow(type: Int) {
        val surfaceView = if (roomSourceBean.isThreeRoom()) {
            val findViewHolderForAdapterPosition =
                seatUserAdapter.recyclerView.findViewHolderForAdapterPosition(0)
            when (findViewHolderForAdapterPosition) {
                is ThreeRoomSeatAdapter.VH -> {
                    findViewHolderForAdapterPosition.binding.anchorSeatInfo.itemVideoSf.tag as View?
                }

                else -> {
                    null
                }
            }
        } else {
            MoreSeatLiveRoomFrg.getOwnerSurfaceView(owenId = roomSourceBean.ownerInfo!!.userId)
        }

        surfaceView?.apply {
            LiveRoomVideoMiniManager.getInstance()
                .show(
                    mContext.applicationContext,
                    2,
                    roomSourceBean.ownerInfo,
                    object : PermissionListener {
                        override fun onSuccess() {
                            if (type == 1) {
                                MiniWindowManager.switchLiveToMiniFloat(mContext)
                            }
                        }

                        override fun onFail() {}
                    },
                    this
                )
        }
    }

    // 限制聊天消息数量，防止无限增长
    private val MAX_CHAT_MESSAGES = 500

    //更新接受消息数据
    val list: ArrayList<ChatRoomMsgInfo> = ArrayList()
    open fun updateReceivedMsg(messages: List<EMMessage>) {
        list.clear()
        for (message in messages) {
            if (message.conversationId() != roomSourceBean.uid) {
                break
            }
            if (message.body is EMTextMessageBody) {
                val content = (message.body as EMTextMessageBody).message
                val sendType = message.getIntAttribute(ChatConstant.CUSTOM_SEND_TYPE)
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
                    dealReceiveGiftMsg(fromJson)
                } else if (sendType == ChatRoomMsgInfo.ITEM_WELCOME_TYPE) {
                    if (isOwner) {
                        sendMessage(
                            roomSourceBean.welcomeMsg,
                            ChatRoomMsgInfo.ITEM_DEFAULT_TYPE,
                            sendUserInfo
                        )
                    }
                    addUserEnterAnim(sendUserInfo)
                }
            }
        }
        // 限制消息数量
        if (chatRoomMsgAdapter.itemCount > MAX_CHAT_MESSAGES) {
            val overflow = chatRoomMsgAdapter.itemCount - MAX_CHAT_MESSAGES
            val newList = chatRoomMsgAdapter.items.drop(overflow).toMutableList()
            newList.addAll(list)
            chatRoomMsgAdapter.submitList(newList)
        } else {
            chatRoomMsgAdapter.addAll(list)
        }

        scrollChatToBottom(50)
    }

    private fun dealReceiveGiftMsg(fromJson: ChatRoomGiftMsg) {
        val giftInfo = fromJson.giftInfo
        //fromJson.sendUser = sendUserInfo
        runOnUiThread {
            val allSeatSend = fromJson.isAllSeatSend
            if (isShowContinueClick && giftInfo.type != GiftInfo.TYPE_RANDOM_BOX && giftInfo.type != GiftInfo.TYPE_LOVER && giftInfo.type != GiftInfo.TYPE_FRAME && !allSeatSend) {
                showFloatAnim(fromJson)
            } else {
                playSvga(fromJson)
                showFloatAnim(fromJson)
            }
            if (giftInfo.type == GiftInfo.TYPE_FRAME && fromJson.targetUserInfo.userId == localStrUserId) {
                LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.REFRESH_USER_CACHE)
            }
        }
    }


    private fun sendPkNotice(content: String) {
        sendMessage(
            content, ChatRoomMsgInfo.ITEM_ROBOT_TYPE
        )
    }

    private var applyQueueTask: TaskQueueManagerImpl = TaskQueueManagerImpl()
    private var applySeatCount = 0

    @SuppressLint("SetTextI18n")
    fun setApplyNum() {
        applySeatCount--
        if (applySeatCount < 0) {
            applySeatCount = 0
        }
        mBinding.tvApplyNum.text = "${applySeatCount}人"
    }

    private var isCloseApplySeatPop = false

    /**
     * 透传消息处理逻辑
     */
    @SuppressLint("SetTextI18n")
    private fun dealCmdMsg(it: EMMessage) {
        try {
            if (isNotMyRoom(it.to) && !it.to.equals(localStrUserId)) {
                return
            }
            if (mContext.isFinishing) {
                return
            }
            val source = it.getIntAttribute("source", -1)
            logInfoCom(LiveRoomActivity.LIVE_ROOM_TAG, "source=${source}---roomId=${roomId}")
            if (source == ChatConstant.ACTION_MSG_APPLY_SET_UP) { //申请上麦
                logInfoCom("有人申请上麦")
                runOnUiThread {
                    if (roomSourceBean.isThreeRoom()) {
                        //同时只弹出一个申请弹窗，
                        applyQueueTask.addTask(ApplySeatTask(it, roomId, roomType))
                    } else {
                        if (!isCloseApplySeatPop) {
                            applyQueueTask.addTask(ApplySeatTask(it, roomId, roomType))
                        }
                        applySeatCount++
                        mBinding.tvApplyNum.text = "${applySeatCount}人"
                    }
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
                sendMessage(
                    "@" + selfUserInfo?.nickName + "，你的玫瑰余额不足，请及时充值",
                    ChatRoomMsgInfo.ITEM_SYSTEM_TYPE
                )
            } else if (source == ChatConstant.ACTION_MSG_ROSE_LACK_KICK_OUT) { //玫瑰不足，踢出男嘉宾
                //专属房间余额不足 退出房间
                showToast("玫瑰余额用尽")
                LiveRoomEndActivity.lunch(mContext, LiveRoomManager.HOUSE_NOT_FUNDS, "", roomId)
                AgoraManager.getInstance().setDownVideo(localUserId, true)
                logComToFile(
                    LiveRoomActivity.LIVE_ROOM_TAG,
                    "收到透传source=$source，专属房间余额不足 退出房间"
                )
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
                    logComToFile(
                        LiveRoomActivity.LIVE_ROOM_TAG, "收到透传source=$source，服务端发送强制离开"
                    )
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
                roomSourceBean.roomType = 1
                roomType = 1
                getRoomDetail()
            } else if (source == ChatConstant.ACTION_MSG_SWITCH_MIKE) { //开/关麦
                val seatId: Int = it.getIntAttribute("position")
                val mikeUse: Int = it.getIntAttribute("mike_use", 0)
                logInfoCom("position：$seatId  mike_use：$mikeUse")
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
                    roomType = 2
                    if (isOwner || isInSeatByUserId(localUserId)) {
                        showToast("房间已切换至专属房间")
                        getRoseGift()
                        getRoomDetail()
                    } else {
                        LiveRoomEndActivity.lunch(
                            mContext, LiveRoomManager.HOUSE_CUT_EXTRA_KICK, "", roomId
                        )
                        logComToFile(
                            LiveRoomActivity.LIVE_ROOM_TAG,
                            "收到透传source=$source，切换专属被踢出房间"
                        )
                        leaveRoomFinish()
                    }
                }
            } else if (source == ChatConstant.ACTION_MSG_SWITCH_TYPE) { //切换房间为专属房间,通知男嘉宾
                val data: JSONObject = it.getJSONObjectAttribute("data")
                logInfoCom(value = "切换房间为专属房间，通知男嘉宾$data")
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
            } else if (source == ChatConstant.ACTION_SEND_ROSE) {
                mBinding.roseAnimView.addRose()
            } else if (source == ChatConstant.ACTION_SET_ADMIN) {
                //设置管理员
                showToast("你已被房主设为管理员")
                roomSourceBean.roomAdmin = true
                userRoomAdminChanger(true)
                onlineUserListPop?.refreshRoomInfo(roomSourceBean)
            } else if (source == ChatConstant.ACTION_CANCEL_ADMIN) {
                //取消管理员
                showToast("你已被房主移除管理员")
                roomSourceBean.roomAdmin = false
                userRoomAdminChanger(false)
                onlineUserListPop?.refreshRoomInfo(roomSourceBean)
            } else if (source == ChatConstant.ACTION_SKIP_ROOM) {
                //踢出房间
                logComToFile(LiveRoomActivity.LIVE_ROOM_TAG, "收到透传source=$source，被踢出房间")
                val data: JSONObject = it.getJSONObjectAttribute("data")
                val optString = data.optString("tip")
                showToast(optString)
                leaveRoomFinish()
            } else if (source == ChatConstant.ACTION_ROOM_CHECK) {
                showContinueLivePop()
            } else if (source == ChatConstant.ACTION_ROOM_USER_OFFLINE_LEAVE) {
                runOnUiThread {
                    val data: JSONObject = it.getJSONObjectAttribute("data")
                    val desc = data.optString("desc", "")
                    if (!TextUtils.isEmpty(desc)) {
                        val chatRoomMsgInfo =
                            ChatRoomMsgInfo(ChatRoomMsgInfo.ITEM_SYSTEM_TYPE, desc, null)
                        chatRoomMsgAdapter.add(chatRoomMsgInfo)
                        scrollChatToBottom(50)
                        refreshSeatInfo()
                    }
                }
            } else if (source == ChatConstant.REFRESH_SEAT_ROSE) {
                refreshSeatRoseInfo()
            } else if (source == ChatConstant.UPDATE_PK_INFO) {
                val roomPkInfo = GsonUtils.fromJson(
                    it.getStringAttribute(ChatConstant.CUSTOM_DATA), RoomPkInfo::class.java
                )
                bindPkInfo(roomPkInfo)
            } else if (source == ChatConstant.ACTION_SEND_GIFT) {
                val content = it.getStringAttribute(ChatConstant.CUSTOM_DATA)
                val chatRoomGiftMsg =
                    GsonUtils.fromJson<ChatRoomGiftMsg>(content, ChatRoomGiftMsg::class.java)
                playSvga(chatRoomGiftMsg)
            } else {
                onReceiveCmdMsg(it)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected open fun userRoomAdminChanger(isRoomAdmin: Boolean) {}
    private fun bindPkInfo(roomPkInfo: RoomPkInfo?) {
        if (isOwner && roomSourceBean.isShowPkFunc() && (roomPkInfo == null || roomPkInfo.status == RoomPkInfo.STATUS_END)) {
            mBinding.btnPk.visibility = View.VISIBLE
        } else {
            mBinding.btnPk.visibility = View.GONE
        }
        if (roomPkInfo?.status == RoomPkInfo.STATUS_RESULT) {
            updatePkResult(roomPkInfo)
            if (isOwner) {
                val content = if (roomPkInfo.result == RoomPkInfo.RESULT_BLUE_SUCCESS) {
                    "PK已结束，恭喜蓝方获胜！"
                } else if (roomPkInfo.result == RoomPkInfo.RESULT_RED_SUCCESS) {
                    "PK已结束，恭喜红方获胜！"
                } else {
                    "PK已结束，双方平局！"
                }
                sendPkNotice(content)
            }
        } else {
            updatePkResult(roomPkInfo, true)
        }

        if (isOwner && roomPkInfo?.showPkStart == "0") {
            sendPkNotice("PK开始啦！")
        }
        mBinding.roomPkView.setPkInfo(roomPkInfo, roomSourceBean)
        changeRvChatScroll()
    }

    protected open fun updatePkResult(roomPkInfo: RoomPkInfo?, isClear: Boolean = false) {
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
        val content = Spans.builder().text("主持申请转为专属房进行视频交友\n")
            .text("专属房间需消耗${price}玫瑰/分钟，是否同意？").color(
                CommonUtils.getColor(
                    cn.yanhu.baselib.R.color.colorMain
                )
            ).build()

        DialogUtils.showConfirmDialog(
            "专属房申请",
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
        receiveInviteSeatPop = ReceiveInviteSeatPop.showDialog(
            topActivity, it, object : ReceiveInviteSeatPop.OnClickSeatUpListener {
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
                request(
                    {
                        agoraRxApi.userSetSeat(
                            roomId, SEAT_TYPE_AUTO, seatId, localStrUserId
                        )
                    }, object : OnRequestResultListener<String> {
                        override fun onSuccess(data: BaseBean<String>) {
                            LiveRoomManager.toLiveRoomPage(
                                ActivityUtils.getTopActivity() as FragmentActivity, roomId
                            )
                        }

                        override fun onFail(code: Int?, msg: String?) {
                            super.onFail(code, msg)
                            showFailTips(code, msg)
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
        if (roomSourceBean.roomType == RoomListBean.TYPE_PRIVATE) { //专属房间下麦直接离开
            AgoraManager.getInstance().setDownVideo(localUserId, true)
            logInfoCom(value = "专属房间下麦直接离开，调用离开接口")
            roomLeave()
            return
        }
        val mySeatId = getMySeatId()
        if (mySeatId == -1) {
            logComToFile(TAG, "出现鬼麦现象，被踢下麦成功uid=$localUserId")
            AgoraManager.getInstance().setDownVideo(localUserId, true)
            refreshSeatInfo()
            return
        }
        mViewModel.userSetSeat(
            roomId,
            SEAT_TYPE_SIT_DOWN,
            getMySeatId().toString(),
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    setSeatOutSuccess()
                }
            })
    }

    protected open fun setSeatOutSuccess() {
        checkTime = 0L
        foreverFaceEffect = ""
        foreverFacePrice = 0
        setSeatStatus()
        logInfoCom(value = "用户下麦：$localUserId")
        BeautyManager.setStickerItem(null)
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
            mBinding.tvRoseSeatNum.text = "${getSeatInRoseNum()}玫瑰/次"
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
        mViewModel.roomLeave(
            roomId, roomSourceBean.uuid, object : OnRequestResultListener<RoomLeaveResponse> {
                override fun onSuccess(data: BaseBean<RoomLeaveResponse>) {
                    roomLeaveInfo = data.data
                    AgoraManager.getInstance().setDownVideo(localUserId, true)
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
        if (!isFreeSeat()) {
            if (!CommonUtils.compareString(
                    balanceRose.toPlainString(),
                    getSeatInRoseNum().toString()
                )
            ) {
                showRechargePop()
                return
            }
        }

        val seatId = if (TextUtils.isEmpty(seatNum)) getNeedSeatId().toString() else seatNum
        if (seatId == "-1") {
            showToast("没有可用的麦位")
            return
        }
        mViewModel.userSetSeat(roomId, operate, seatId, object : OnRequestResultListener<String> {
            override fun onSuccess(data: BaseBean<String>) {
                if (operate == SEAT_TYPE_APPLY) {
                    //1：申请上麦
                    showToast("您已申请上麦，请等待主持人通过")
                    val map: MutableMap<String, Any> = HashMap()
                    map["fromUid"] = localStrUserId
                    map["seatId"] = seatId
                    map["fromNickName"] = selfUserInfo!!.nickName
                    map["portrait"] = selfUserInfo!!.portrait
                    EmMsgManager.sendCmdMessagePeople(
                        roomSourceBean.ownerInfo!!.userId,
                        ChatConstant.ACTION_MSG_APPLY_SET_UP,
                        map,
                    )
                    setHasApplyStatus()

                } else if (operate == SEAT_TYPE_AUTO) {
                    //自动上麦
                    AgoraManager.getInstance().setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
                    checkTime = 0L
                    refreshSeatInfo(localUserId)
                    setHasSeatUpStatus()
                    balanceRose = CommonUtils.subBigDecimal(
                        balanceRose,
                        BigDecimal(getSeatInRoseNum())
                    )
                }
            }

            override fun onFail(code: Int?, msg: String?) {
                super.onFail(code, msg)
                showFailTips(code, msg)

            }

        })
    }

    private fun showFailTips(code: Int?, msg: String?) {
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
        messageDialog = SendMessagePop.showDialog(
            mContext,
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

    var roseGiftInfo: GiftInfo? = null
    var balanceRose: BigDecimal = BigDecimal.ZERO
    private fun getRoseGift() {
        request(
            { imChatRxApi.getGiftList(GiftInfo.TYPE_ROSE) },
            object : OnRequestResultListener<GiftResponse> {
                override fun onSuccess(data: BaseBean<GiftResponse>) {
                    val giftResponse = data.data ?: return
                    balanceRose = giftResponse.roseNum
                    if (giftResponse.list.isNotEmpty()) {
                        roseGiftInfo = giftResponse.list[0]
                    }
                    if (getCurrentSendGiftPop()?.isVisible == true) {
                        getCurrentSendGiftPop()?.refreshBalanceRose(balanceRose)
                    }
                }
            })
    }


    private var countDown: CoroutineScope? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startCountTime(chatRoomGiftMsg: ChatRoomGiftMsg) {
        countDown?.cancel()
        mContext.countDown(3, start = {
            countDown = it
        }, end = {
            //倒计时结束
            logInfoCom("startSendComboGift", "倒计时结束")
            startSendComboGift(chatRoomGiftMsg)
            countDown = null
        }, next = {}, cancel = {})
    }

    protected fun startSendComboGift(chatRoomGiftMsg: ChatRoomGiftMsg?) {
        if (chatRoomGiftMsg == null) {
            return
        }
        countDown?.cancel()
        logInfoCom("startSendComboGift", "clickCount=${clickCount}")
        if (clickCount > 0) {
            chatRoomGiftMsg.giftInfo.sendNumber = clickCount
            logInfoCom("startSendComboGift", "连击结束，调用送礼接口")
            sendRoseGift(chatRoomGiftMsg)
            mBinding.vgClickRose.visibility = View.INVISIBLE
            clickCount = 0
            chatRoomRoseGiftMsg = null
        }
        mBinding.vgForeverTip.visibility = View.INVISIBLE
        mBinding.tvClickCount.text = ""
        mBinding.tvFaceTip1.text = "继续连击 "
        mBinding.tvFaceTip2.text = " 次升级永久贴脸  "
    }

    private fun sendComboSuccess(chatRoomGiftMsg: ChatRoomGiftMsg) {
        LiveDataEventManager.sendLiveDataMessage(LiveDataEventManager.REFRESH_USER_CACHE)
        showFloatAnim(chatRoomGiftMsg)
        //val giftMsgInfo = GiftMsgInfo(item, roomUserSeatInfo)
        logInfoCom("startSendComboGift", "赠送礼物成功，发送礼物消息")
        sendMessage(GsonUtils.toJson(chatRoomGiftMsg), ChatRoomMsgInfo.ITEM_GIFT_TYPE)
        getRoseGift()
    }

    fun logInfoCom(value: String?) {
        if (BuildConfig.DEBUG) {
            logInfoCom("logInfoCom", value = value)
        }
    }

    fun logInfoCom(key: String = "logInfoCom", value: String?) {
        if (BuildConfig.DEBUG) {
            logcom(key, value)
        }
    }

    private fun sendRoseGift(chatRoomGiftMsg: ChatRoomGiftMsg) {
        val sendGiftRequest = SendGiftRequest()
        val giftInfo = chatRoomGiftMsg.giftInfo
        val sendUserInfo = chatRoomGiftMsg.targetUserInfo
        sendGiftRequest.roomId = roomId
        sendGiftRequest.toUid = sendUserInfo.userId
        sendGiftRequest.giftId = giftInfo.id
        sendGiftRequest.num = chatRoomGiftMsg.giftInfo.sendNumber
        sendGiftRequest.source = SendGiftRequest.SOURCE_LIVE_ROOM
        sendGiftRequest.sendCntId = sendCntId
        sendGiftRequest.callId = 0
        logInfoCom("startSendComboGift", "sendGiftInfo=${GsonUtils.toJson(sendGiftRequest)}")
        sendCntId = ""
        request2(
            { imChatRxApi.sendGift(sendGiftRequest) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    if (TextUtils.isEmpty(chatRoomGiftMsg.giftInfo.svga)) {
                        showGiftFloatAnim(chatRoomGiftMsg)
                        sendMessage(
                            GsonUtils.toJson(chatRoomGiftMsg),
                            ChatRoomMsgInfo.ITEM_GIFT_TYPE
                        )
                        logComToFile("startSendComboGift", "赠送玫瑰成功，发送玫瑰礼物消息")
                        getRoseGift()
                    } else {
                        sendComboSuccess(chatRoomGiftMsg)
                    }
                    refreshSeatRoseInfo()
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
        trim: String,
        sendType: Int,
        altUser: BaseUserInfo? = null
    ) {
        if (ifMute) {
            showToast("你已被禁言")
            return
        }

        // 在IO线程执行消息构建和发送
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val message = EMMessage.createTextSendMessage(trim, roomSourceBean.uid)
                val sendStartTime = System.currentTimeMillis()

                // 设置消息属性
                setMessageAttributes(message, altUser, selfUserInfo!!, sendType)

                // 设置回调
                setupMessageCallback(message, trim, selfUserInfo!!, altUser, sendStartTime)
                logComToFile("startSendComboGift", "开始发送消息")
                // 发送消息
                EMClient.getInstance().chatManager().sendMessage(message)

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    logComToFile("发送消息异常", e.message ?: "unknown error")
                }
            }
        }
    }

    private fun setMessageAttributes(
        message: EMMessage,
        altUser: BaseUserInfo?,
        selfUserInfo: BaseUserInfo,
        sendType: Int
    ) {
        if (altUser != null) {
            message.setAttribute(ChatConstant.ATE_USER_INFO, GsonUtils.toJson(altUser))
        }
        message.setAttribute(ChatConstant.CUSTOM_SEND_USER_INFO, GsonUtils.toJson(selfUserInfo))
        message.setAttribute(ChatConstant.CUSTOM_SEND_TYPE, sendType)
        message.chatType = EMMessage.ChatType.ChatRoom
    }

    private fun setupMessageCallback(
        message: EMMessage,
        content: String,
        selfUserInfo: UserDetailInfo,
        altUser: BaseUserInfo?,
        startTime: Long
    ) {
        message.setMessageStatusCallback(object : EMCallBack {
            override fun onSuccess() {
                val costTime = System.currentTimeMillis() - startTime
                logComToFile("startSendComboGift", "发送成功，耗时: ${costTime}ms, 内容: $content")

                sendGiftMsgSuccess(message, content, selfUserInfo, altUser)
            }

            override fun onError(code: Int, error: String) {
                val costTime = System.currentTimeMillis() - startTime
                logComToFile(TAG, "发送失败，code=$code, error=$error, 耗时: ${costTime}ms")
                runOnUiThread {
                    if (code == EMError.CHATROOM_NOT_JOINED) {
                        joinChatRoom()
                    } else if (code == EMError.MESSAGE_INVALID) {
                        ApplicationProxy.instance.reLoginImSdk(object : OnImLoginListener {
                            override fun onSuccess() {
                                EMClient.getInstance().chatManager().sendMessage(message)
                            }

                            override fun onError(code: Int, error: String?) {
                            }
                        })
                    } else {
                        showToast("发送失败: $error")
                    }
                }
            }
        })
    }

    private fun sendGiftMsgSuccess(
        message: EMMessage,
        content: String,
        selfUserInfo: UserDetailInfo,
        altUser: BaseUserInfo?
    ) {
        runOnUiThread {
            val chatRoomMsgInfo = ChatRoomMsgInfo(
                message.getIntAttribute(ChatConstant.CUSTOM_SEND_TYPE, 0),
                content,
                selfUserInfo,
                altUser
            )
            chatRoomMsgAdapter.add(chatRoomMsgInfo)
            scrollChatToBottom(50)
        }
    }

    private val giftAnimTaskManager: TaskQueueManagerImpl = TaskQueueManagerImpl()
    private fun showGiftSvgAnim(giftInfo: GiftInfo?) {
        if (giftInfo == null || TextUtils.isEmpty(giftInfo.svga) || giftInfo.type == GiftInfo.TYPE_FACE) {
            return
        }
        giftAnimTaskManager.addTask(
            GiftPopAnimTask(
                giftInfo, mBinding.svgGiftAnim, mBinding.videoGiftAnimView, roomId = roomId
            )
        )
    }

    private fun showFloatAnim(giftMsgInfo: ChatRoomGiftMsg) {
        val giftInfo = giftMsgInfo.giftInfo
        refreshSeatRoseInfo()
        showGiftFloatAnim(giftMsgInfo)
        if (giftInfo.type == GiftInfo.TYPE_SONG) {
            getRoomExtraInfo()
        }
        if (roomSourceBean.isPrivateRoom() || roomSourceBean.isPublicRoom()) {
            getRoomDetail()
        }
        calculateForeverFaceEffect(giftMsgInfo)
    }

    //播放动画
    private fun playSvga(giftMsgInfo: ChatRoomGiftMsg, isSendAll: Boolean = false) {
        val giftInfo = giftMsgInfo.giftInfo
        giftInfo.sendNumber =
            if (giftMsgInfo.giftInfo.sendNumber > 0) giftMsgInfo.giftInfo.sendNumber else 1


        if (giftInfo.type == GiftInfo.TYPE_SONG && giftMsgInfo.sendUser.userId == roomSourceBean.ownerInfo?.userId) {
            logInfoCom("点歌的人是房主不显示礼物动画")
        } else {
            showGiftSvgAnim(giftInfo)
        }
        playFaceGiftEffect(giftInfo, giftMsgInfo)
        if (isShowContinueClick && !isSendAll) {
            showForeverFaceEffectTip(giftMsgInfo)
        }
    }


    private fun showForeverFaceEffectTip(giftMsgInfo: ChatRoomGiftMsg) {
        val giftInfo = giftMsgInfo.giftInfo
        if (giftInfo.type == GiftInfo.TYPE_FACE && giftMsgInfo.sendUser.userId == localStrUserId) {
            val userId = giftMsgInfo.targetUserInfo.userId
            val seatUserInfo = getSeatUserInfoUserId(userId.toInt())
            val replacePrice = seatUserInfo?.replacePrice

            mBinding.vgForeverTip.visibility = View.VISIBLE
            if (replacePrice == null || replacePrice <= 0) {
                showNeedClickCount(needComboCnt)
            } else {
                val price = giftInfo.price
                var count = ceil((replacePrice / price).toDouble()).toInt()
                if (count < needComboCnt) {
                    count = needComboCnt
                }
                showNeedClickCount(count)
            }
        }
    }

    private fun showGiftFloatAnim(giftMsgInfo: ChatRoomGiftMsg) {
        giftMsgInfo.theGiftStay = 1000
        starGiftAnimation(
            giftMsgInfo
        )
    }

    /**
     * 播放贴脸礼物特效
     */
    //private var faceEffectQueueTask: TaskQueueManagerImpl = TaskQueueManagerImpl()
    private var faceEffectInfo: String = ""

    private fun playFaceGiftEffect(
        giftInfo: GiftInfo, giftMsgInfo: ChatRoomGiftMsg
    ) {
        if (giftInfo.type == GiftInfo.TYPE_FACE && giftMsgInfo.targetUserInfo.userId == localStrUserId) {
            faceEffectCountDown?.cancel()
            var currentStickerItem: String? = ""
            if (faceEffectInfo != giftInfo.svga) {
                faceEffectInfo = giftInfo.svga
                currentStickerItem = SenseTimeBeautySDK.beautyConfig.sticker?.path
                val stickerItem = SenseTimeBeautySDK.StickerItem(mContext, faceEffectInfo)
                BeautyManager.setStickerItem(stickerItem)
                logInfoCom("startSendComboGift", "设置贴脸setStickerItem=${faceEffectInfo}")
            }
            startFaceEffectTime(currentStickerItem)
        }
    }

    private fun showNeedClickCount(totalCount: Int) {
        val restCount = totalCount - clickCount
        if (restCount > 0) {
            mBinding.tvClickCount.text = restCount.toString()
        } else {
            mBinding.tvClickCount.text = ""
            mBinding.tvFaceTip1.text = ""
            mBinding.tvFaceTip2.text = "已升级成永久贴脸特效  "
        }
    }

    private var foreverFaceEffect: String = "" //永久贴脸礼物特效
    private var foreverFacePrice: Int = 0
    private fun calculateForeverFaceEffect(giftMsgInfo: ChatRoomGiftMsg) {
        val giftInfo = giftMsgInfo.giftInfo
        if (giftInfo.type == GiftInfo.TYPE_FACE && giftMsgInfo.targetUserInfo.userId == localStrUserId) {
            faceEffectInfo = giftInfo.svga
            if (giftInfo.sendNumber >= needComboCnt) {
                var totalPrice = giftInfo.price * giftInfo.sendNumber
                if (TextUtils.isEmpty(foreverFaceEffect)) {
                    foreverFacePrice = totalPrice
                    foreverFaceEffect = faceEffectInfo
                    logInfoCom(
                        "startSendComboGift",
                        "收到永久贴脸,faceEffectInfo = $faceEffectInfo"
                    )
                } else {
                    if (totalPrice > foreverFacePrice) {
                        foreverFacePrice = totalPrice
                        foreverFaceEffect = faceEffectInfo
                        logInfoCom(
                            "startSendComboGift",
                            "替换永久贴脸,faceEffectInfo = $faceEffectInfo"
                        )
                    }
                }
            }
            if (!TextUtils.isEmpty(foreverFaceEffect)) {
                logInfoCom("startSendComboGift", "当前永久贴脸setStickerItem=${foreverFaceEffect}")
            }
        }
    }


    private var faceEffectCountDown: CoroutineScope? = null
    private var faceRestTime: Int = 0

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startFaceEffectTime(currentStickerItem: String?) {
        if (!TextUtils.isEmpty(foreverFaceEffect) && faceEffectInfo != foreverFaceEffect && currentStickerItem == foreverFaceEffect) {
            //如果当前正在播放的贴脸是永久贴脸并且新的贴脸不是永久贴脸则重置倒计时时间为原始的30s
            faceRestTime = 0
        }
        val countTime = faceRestTime + 30
        logInfoCom("startSendComboGift", "startFaceEffectTime = $countTime")
        mContext.countDown(countTime, start = {
            faceEffectCountDown = it
        }, end = {
            //倒计时结束
            logInfoCom("startSendComboGift", "倒计时结束")
            if (!TextUtils.isEmpty(foreverFaceEffect)) {
                logInfoCom("startSendComboGift", "设置永久贴脸setStickerItem=${foreverFaceEffect}")
                val stickerItem = SenseTimeBeautySDK.StickerItem(mContext, foreverFaceEffect)
                BeautyManager.setStickerItem(stickerItem)
            } else {
                BeautyManager.setStickerItem(null)
            }
            faceRestTime = 0
            faceEffectInfo = ""
            faceEffectCountDown = null
        }, next = {
            faceRestTime = it
        }, cancel = {

        })
    }


    private fun starGiftAnimation(model: ChatRoomGiftMsg) {
        mBinding.giftRewardLayout.put(model)
    }

    private fun setGiftNormalAnimAdapter() {
        mBinding.giftRewardLayout.setRewardAnimAdapter(
            GiftAnimAdapter(
                object : GiftAnimAdapter.OnClearListener {
                    override fun onClear() {
                    }

                    override fun onShowUserInfo(userId: String) {
                        showUserPop(userId)
                    }
                },
            )
        )
    }


    override fun requestData() {
        super.requestData()
        logInfoCom(LiveRoomActivity.LIVE_ROOM_TAG, "获取房间详情---roomId${roomId}")
        getRoomDetail()
    }


    private var roomGroupMemberPop: RoomGroupMemberPop? = null
    protected fun showGroupMemberPop() {
        val ownerUserId = roomSourceBean.ownerInfo!!.userId
        request(
            { agoraRxApi.getGroupMemberInfo(ownerUserId) },
            object : OnRequestResultListener<RoomGroupMemberRes> {
                override fun onSuccess(data: BaseBean<RoomGroupMemberRes>) {
                    if (CommonUtils.isPopShow(roomGroupMemberPop)) {
                        return
                    }
                    roomGroupMemberPop = RoomGroupMemberPop.showDialog(
                        mContext, data.data!!, object : RoomGroupMemberPop.OnExitGroupListener {
                            override fun onExitGroup() {
                                exitGroup(ownerUserId)
                            }
                        })
                }
            })
    }

    protected fun showJoinGroupPop() {
        if (AppCacheManager.isMan()) {
            DialogUtils.showConfirmDialog("加入粉丝团", {
                if (CommonUtils.compareString(balanceRose.toPlainString(), "20")) {
                    joinRoomGroup()
                } else {
                    showRechargePop()
                }
            }, {}, "是否同意花费20玫瑰，加入粉丝团", confirm = "加入", cancel = "取消")
        } else {
            joinRoomGroup()
        }
    }

    private fun joinRoomGroup() {
        mViewModel.joinRoomGroup(
            roomSourceBean.ownerInfo!!.userId, object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    showToast("加团成功")
                    joinGroupSuccess()
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    showFailTips(code, msg)
                }
            })
    }

    protected open fun joinGroupSuccess() {
        if (AppCacheManager.isMan()) {
            balanceRose = CommonUtils.subBigDecimal(balanceRose, BigDecimal(20))
        }
    }

    protected open fun exitGroupSuccess() {}


    private var seatUserOperatePop: SeatUserOperatePop? = null
    protected fun showUserPop(userId: String) {
        request(
            { agoraRxApi.getUserInfoByUserId(userId) },
            object : OnRequestResultListener<UserDetailInfo> {
                override fun onSuccess(data: BaseBean<UserDetailInfo>) {
                    showUserBasicInfoPop(data.data!!)
                }
            })
    }

    private fun showUserBasicInfoPop(data: UserDetailInfo) {
        if (CommonUtils.isPopShow(seatUserOperatePop)) {
            return
        }
        startSendComboGift(chatRoomRoseGiftMsg)

        seatUserOperatePop = SeatUserOperatePop.showDialog(
            mContext, data, object : SeatUserOperatePop.OnOperateUserListener {
                override fun onSendGift(userInfo: UserDetailInfo) {
                    showSendGiftPop(userInfo, false)
                }

                override fun onAlt(userInfo: UserDetailInfo) {
                    clearSendGiftPopReference()
                    showInputDialog(userInfo, true)
                }

                override fun onAddFriend(userInfo: UserDetailInfo) {
                    showAddFriendPop(userInfo)
                }
            })
    }

    private fun showAddFriendPop(userInfo: UserDetailInfo) {
        if (userInfo.addFriendWay == 0) {
            applyFriend(userInfo)
        } else {
            DialogUtils.showConfirmDialog(
                "添加好友",
                {
                    addFriendByRose(userInfo)
                },
                {},
                content = "是否同意花费${userInfo.needRoseNum}玫瑰，添加好友？",
                cancel = "取消",
                confirm = "加好友",
                cancelBg = cn.yanhu.baselib.R.drawable.shape_cancel_btn_r30
            )
        }
    }

    private fun addFriendByRose(userInfo: UserDetailInfo) {
        request(
            { agoraRxApi.becomeFriendRose(userInfo.userId) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    userInfo.isFriend = true
                    ChatUserInfoManager.saveUserInfo(userInfo)
                    getCurrentSendGiftPop()?.hideFriendBtn()
                    showToast("添加好友成功")
                    EmMsgManager.sendCmdMessagePeople(
                        userInfo.userId, CmdMsgTypeConfig.ADD_FRIEND, null
                    )
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    showFailTips(code, msg)
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

    fun showRechargePop() {
        ApplicationProxy.instance.showRechargePop(mContext, true, balanceRose)
    }

    protected fun getRoomDetail() {
        mViewModel.getRoomDetail(roomId, roomType)
    }

    protected fun refreshSeatInfo(uid: Int = 0) {
        refreshThreeRoomInfo()
        mViewModel.getSeatList(roomId, object : OnRequestResultListener<MutableList<RoomSeatInfo>> {
            override fun onSuccess(data: BaseBean<MutableList<RoomSeatInfo>>) {
                val it = data.data ?: return
                seatList = it
                roomSourceBean.roomSeatResList = seatList
                logComToFile(TAG, "获取麦位信息成功userId=$uid")
                if (uid == 0) {
                    getRoomSeatSuccess(seatList)
                } else {
                    refreshSeatInfo(seatList, uid)
                }
                updateSeatRoseInfo()

            }

            override fun onFail(code: Int?, msg: String?) {
                super.onFail(code, msg)
                logComToFile(TAG, "getSeatList失败msg=$msg，uid = $uid")
            }
        })
    }

    /**
     * 更新麦位上用户的玫瑰数量
     */
    protected fun refreshSeatRoseInfo() {
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
    }


    protected open fun refreshSeatInfo(it: MutableList<RoomSeatInfo>, uid: Int) {
    }

    fun RecyclerView.Adapter<*>.safeUpdateItem(
        list: MutableList<RoomSeatInfo>, position: Int, newItem: RoomSeatInfo
    ) {
        if (position !in list.indices) return
        // 如果引用相同，克隆一个对象（适用于 data class）
        val finalItem = if (list[position] === newItem) {
            logComToFile(TAG, "更新麦位信息,复制对象，position=$position")
            newItem.deepCopy()
        } else {
            newItem
        }
        list[position] = finalItem as RoomSeatInfo
        this.notifyItemChanged(position, Any())
    }


    override fun registerNecessaryObserver() {
        mViewModel.roomDetailLivedata.observe(this) { it ->
            parseState(it, {
                roomType = it.roomType
                mBinding.roomInfo = it
                needComboCnt = it.needComboCnt
                roomSourceBean = it
                if (seatList.isNotEmpty()) {
                    roomSourceBean.roomSeatResList = seatList
                }
                isOwner = it.ownerInfo?.userId == localStrUserId
                val pkDetail = roomSourceBean.pkDetail
                getRoomInfoSuccess()
                bindPkInfo(pkDetail)
                getGiftComboSwitch()
                userRoomAdminChanger(roomSourceBean.roomAdmin)
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
                    LiveRoomManager.chatRoomId = roomSourceBean.uid
                    logInfoCom("加入聊天室成功")
                    if (!roomSourceBean.isAdmin()) {
                        sendMessage("进入了房间", ChatRoomMsgInfo.ITEM_WELCOME_TYPE)
                    }
                    ThreadUtils.getMainHandler().postDelayed({
                        refreshOnlineUser(getChatRoom().memberCount)
                    }, 1000)
                    AgoraManager.getInstance().isInitSuccess = true
                }

                override fun onError(error: Int, errorMsg: String?) {
                    if (error == EMError.CHATROOM_ALREADY_JOINED) {
                        return
                    }
                    logInfoCom("加入聊天室失败，$error————msg$errorMsg")
                    if (error == 201 && !hasReLogin) {
                        reLoginIm()
                    } else {
                        showToast("直播间异常，请重新尝试进入直播间")
                        logComToFile(
                            LiveRoomActivity.LIVE_ROOM_TAG,
                            "加入聊天室失败$error————$errorMsg---退出直播间"
                        )
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
                logComToFile(
                    LiveRoomActivity.LIVE_ROOM_TAG, "加入聊天室失败$error————msg$error---退出直播间"
                )
                leaveRoomFinish()
            }
        })
    }

    var isFinish = false
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
    private fun addUserEnterAnim(user: BaseUserInfo?) {
        if (user == null) {
            return
        }
        val carUrl = user.carUrl
        if (!TextUtils.isEmpty(carUrl)) {
            if (!isInSeatByUserId(
                    localUserId
                ) || RoomSwitchCacheManager.isOpenEnterAnim(roomId)
            ) {
                val giftInfo = GiftInfo()
                giftInfo.svga = carUrl
                showGiftSvgAnim(giftInfo)
            }
        }
        val enterAnimUrl = user.enterAnimUrl
        val chatRoomGiftMsg = ChatRoomGiftMsg(user)
        if (!TextUtils.isEmpty(enterAnimUrl)) {
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
    fun setRvChatMessageTop(isShow: Boolean) {
        mBinding.rvSeat.post {
            val maxTop: Int = getMaxTopHeight()
            val minTop: Int = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20)
            if (isShow) {
                if (mBinding.viewMask.isInvisible) {
                    mBinding.viewMask.visibility = View.VISIBLE
                    AnimManager.showMarginTopAnimator(mBinding.rvChat, maxTop, minTop, 300)
                }
            } else {
                if (mBinding.viewMask.isVisible) {
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
        return (if (seatHeight > 0) seatHeight else mBinding.rvSeat.height) + titleHeight + mBinding.flTopView.height + mBinding.vgPkView.height
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
        val findLast =
            seatList.indexOfLast { it.roomUserSeatInfo?.userId == localStrUserId }
        AgoraManager.getInstance().init(mContext, if (findLast >= 0) 1 else 0, null)
        logInfoCom(LiveRoomActivity.LIVE_ROOM_TAG, "加载房间---roomId${roomId}")
        //声网初始化
        if (roomSourceBean.getFragmentType() == RoomListBean.FRG_THREE_ROOM) {
            AgoraManager.getInstance().setVideoEncoderConfiguration(500, 560)
        } else {
            if (roomSourceBean.getFragmentType() == RoomListBean.FRG_SEVEN_ROOM || roomSourceBean.roomType == TYPE_SEVEN_SONG) {
                AgoraManager.getInstance().setVideoEncoderConfiguration(330, 360)
            } else {
                AgoraManager.getInstance().setVideoEncoderConfiguration(320, 290)
            }
        }
        return AgoraManager.getInstance()
            .joinChannel(localUserId, roomId, roomSourceBean.agoraToken)

    }


    private fun destroyRoom() {
        chatRoomRoseGiftMsg?.apply {
            startSendComboGift(this)
        }

        // 移除聊天室回调
        if (localUserId > 0) {
            AgoraManager.getInstance().enableLocalVideo(false)
        }
        BeautyManager.setStickerItem(null)
        AgoraManager.getInstance().leaveChannel()
        EMClient.getInstance().chatroomManager()
            .leaveChatRoom(roomSourceBean.uid, object : EMCallBack {
                override fun onSuccess() {
                    LiveRoomManager.chatRoomId = ""
                }

                override fun onError(code: Int, error: String?) {
                }
            })
        pauseAnimView()
        clearAnimView()
        logInfoCom(LiveRoomActivity.LIVE_ROOM_TAG, "销毁房间---roomId${roomId}")
        EMClient.getInstance().chatroomManager().removeChatRoomListener(this@BaseLiveRoomFrg)
        handler.removeCallbacksAndMessages(null)
        closeMiniWindow()
        // clearAgora()
    }

    private fun clearAgora() {
        AgoraManager.getInstance().clearRtcConnection()
        AgoraManager.getInstance().onDestroy()
    }

    private fun pauseAnimView() {
        mBinding.giftRewardLayout.onPause()
        mBinding.userEnterView.onPause()
    }

    private fun destroyAnimView() {
        clearSendGiftPopReference()
        mBinding.userEnterView.onDestroy()
        mBinding.giftRewardLayout.onDestroy()
        mBinding.roseAnimView.clearRoses()
        mBinding.svgGiftAnim.clear()
    }

    private fun clearAnimView() {
        mBinding.svgGiftAnim.clearsAfterDetached = true
        mBinding.svgGiftAnim.stopAnimation(true)
        mBinding.svgGiftAnim.clear()
        mBinding.userEnterView.clear()
        mBinding.giftRewardLayout.clear()

    }


    override fun onResume() {
        super.onResume()
        mBinding.userEnterView.onResume()
        mBinding.giftRewardLayout.onResume()
        if (isOnNewIntent) {
            isOnNewIntent = false
            return
        }
        getRoseGift()
        closeMiniWindow()
        if (mViewModel.roomDetailLivedata.value != null) {

            addHostSurfaceView()
        }
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
        if (!isLeave) {
            if (isFinish) {
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
            } else {
                leaveUserMap.clear()
                offLineUser.clear()
                logComToFile(
                    LiveRoomActivity.LIVE_ROOM_TAG, "触发exactDestroy关闭房间---roomId${roomId}"
                )
            }

        }
        destroyRoom()
        destroyAnimView()
    }


    /**
     * 处理声网相关回调逻辑
     */
    override fun agoraListener(type: Int, uid: Int) {
        runOnUiThread {
            if (type == AgoraManager.TOKEN_EXPIRE || type == AgoraManager.TOKEN_WILL_EXPIRE) { //token已过期、token即将过期
                fetchToken()
            } else if (type == AgoraManager.USER_SET_UP) { //检测到有观众上麦，更新视图
                if (!isInSeatByUserId(uid)) {
                    logComToFile(TAG, "有观众上麦，更新视图userId=$uid")
                    refreshSeatInfo(uid)
                }
                removeUserLeaveRecord(uid)
            } else if (type == AgoraManager.USER_SIT_DOWN) { //检测到有观众下麦，更新视图
                logInfoCom("下麦请求$uid")
                logComToFile(TAG, "USER_SIT_DOWN---有观众下麦，更新视图userId=$uid")
                updateUserLeaveView(uid)
            } else if (type == AgoraManager.USER_QUIT) { //检测到有观众离开
                logComToFile(TAG, "用户离开$uid")
                mViewModel.getSeatList(
                    roomId,
                    object : OnRequestResultListener<MutableList<RoomSeatInfo>> {
                        override fun onSuccess(data: BaseBean<MutableList<RoomSeatInfo>>) {
                            val it = data.data ?: return
                            it.forEach {
                                if (it.roomUserSeatInfo?.userId == uid.toString()) {
                                    return
                                }
                            }
                            logComToFile(TAG, "USER_QUIT---有观众下麦，更新视图userId=$uid")
                            updateUserLeaveView(uid)
                        }

                        override fun onFail(code: Int?, msg: String?) {
                            super.onFail(code, msg)
                            logComToFile(TAG, "getSeatList失败msg=$msg，uid = $uid")
                        }
                    })
            } else if (type == AgoraManager.USER_SHORT_LEAVE) { //远端用户超时掉线
            } else if (type == AgoraManager.USER_ERROR_CAMERA_DISABLED) { //主播相机设备由于设备策略而无法打开
            } else if (type == AgoraManager.USER_REMOTE_VIDEO_INIT_FINISH) { //远端用户视频渲染成功
                removeUserLeaveRecord(uid)
                userVideoStatusChanged(uid, false)
            } else if (type == AgoraManager.REMOTE_USER_VIDEO_UP) { //远端视频恢复正常播放
                removeUserLeaveRecord(uid)
                userVideoStatusChanged(uid, false)
            } else if (type == AgoraManager.REMOTE_USER_VIDEO_DOWN) {
                if (!leaveUserMap.containsKey(uid)) {
                    leaveUserMap.put(uid, System.currentTimeMillis())
                    logComToFile(TAG, "uid：" + uid + "开始离线倒计时")
                }
            } else if (type == AgoraManager.USER_NETWOKR_GOOD) { //网络良好
                if (uid == 0) {
                    networkType = 1
                    userNetChanged(localStrUserId, false)
                } else {
                    //对方网络良好时 判断下离线集合里有没有此用户 如果有且离线时间超过60秒 恢复正常状态，兜底离线变上线时没有恢复状态
                    if (leaveUserMap.containsKey(uid)) {
                        val value = leaveUserMap[uid]!!
                        if (System.currentTimeMillis() - value >= 60000L) {
                            logComToFile(TAG, "uid：" + uid + "恢复网络正常,离线倒计时结束")
                            removeUserLeaveRecord(uid)
                            userVideoStatusChanged(uid, false)
                        }
                    }
                }
            } else if (type == AgoraManager.USER_NETWOKR_BAD || type == AgoraManager.USER_NETWOKR_DOWN) { //网络不佳、网络断开
                if (uid == 0) {
                    networkType = if (type == AgoraManager.USER_NETWOKR_BAD) {
                        0
                    } else {
                        -1
                    }
                    userNetChanged(localStrUserId, true)
                }

            } else if (type == AgoraManager.USER_REMOTE_VIDEO_PLAY_FAIL) {
                userVideoStatusChanged(uid, true)
            }
        }
    }


    protected var networkType = 1

    private fun updateUserLeaveView(uid: Int) {
        removeUserLeaveRecord(uid)
        if (uid.toString() == roomSourceBean.ownerInfo?.userId) {
            userVideoStatusChanged(uid, true)
        } else {
            AgoraManager.getInstance().setDownVideo(uid, localUserId == uid)
            userLeaveChanged(uid)
        }
        if (isOwner) {
            roomPkSendPop?.setUserLeave(uid.toString())
        }
    }


    // private var leaveMap: MutableMap<Int, Thread> = mutableMapOf()

    //溢出用户离开记录
    private fun removeUserLeaveRecord(uid: Int): Boolean {
        logInfoCom("leaveMap是否包含：" + leaveUserMap.containsKey(uid))
        if (offLineUser.contains(uid)) {
            offLineUser.remove(uid)
        }
        if (leaveUserMap.containsKey(uid)) {
            leaveUserMap.remove(uid)
            logComToFile(TAG, "uid：" + uid + "离线倒计时结束")
            return true
        }
        return false
    }

    //嘉宾离线超时2分钟，强制踢出房间
    protected fun operateLeave(operatedUserId: Int) {
        logInfoCom("强制下麦：$operatedUserId")
        mViewModel.operateLeave(
            roomId, operatedUserId.toString(), object : OnRequestResultListener<String> {
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

    private class SafeHandler(activity: BaseLiveRoomFrg) : Handler(Looper.getMainLooper()) {
        private val weakActivity = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            weakActivity.get()?.handleMessageSafe(msg)
        }
    }

    private fun handleMessageSafe(msg: Message) {
        // 处理消息逻辑
        if (msg.what == 2) {
            logInfoCom("handler：强制下麦")
            val downUserId = msg.obj as Int
            if (isOwner && removeUserLeaveRecord(downUserId)) {
                //判断是否在离开倒计时集合，为true直接强制踢出房间
                logComToFile(
                    LiveRoomActivity.LIVE_ROOM_TAG,
                    "离线2分钟，强制下麦,userId=$downUserId"
                )
                logInfoCom("downWheet：强制下麦成功")
                operateLeave(downUserId)
            }
        } else if (msg.what == 3) {
            //5后显示暂时离线
            val downUserId = msg.obj as Int
            if (leaveUserMap.containsKey(downUserId)) {
                userVideoStatusChanged(downUserId, true)
            }
        }
    }

    var handler: Handler = SafeHandler(this)
    val CHECK_INTERVAL = 1000L
    var leaveUserMap = mutableMapOf<Int, Long>()
    var offLineUser = mutableListOf<Int>()

    private fun startStatusCheck() {
        handler.removeCallbacks(checkRunnable)
        handler.postDelayed(checkRunnable, CHECK_INTERVAL)
    }

    private val checkRunnable = object : Runnable {
        override fun run() {
            checkOfflineStatus()
            handler.postDelayed(this, CHECK_INTERVAL)
        }
    }

    private fun checkOfflineStatus() {
        try {
            leaveUserMap.forEach {
                val key = it.key
                val differTime = System.currentTimeMillis() - it.value
                if (differTime >= 5000) {
                    if (!offLineUser.contains(key)) {
                        offLineUser.add(key)
                        logComToFile(TAG, "uid：" + key + "显示暂时离线")
                        val obtain = Message.obtain()
                        obtain.what = 3
                        obtain.obj = key
                        handler.sendMessage(obtain)
                    }
                }
                if (differTime >= 60000 * 2) {
                    val obtain = Message.obtain()
                    obtain.what = 2
                    obtain.obj = key
                    handler.sendMessage(obtain)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
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
//        if (isOwner) {
//            for (speaker in speakers!!) {
//                val uid = speaker.uid
//                if (!isInSeatByUserId(uid)) {
//                    //不在座位上 还有声音 出现了鬼麦现象 踢下麦
//                    logComToFile(TAG,"出现鬼麦现象，踢下麦uid=$uid")
//                    EmMsgManager.sendCmdMessagePeople(
//                        uid.toString(), "", ChatConstant.ACTION_MSG_SIT_DOWN
//                    )
//                }
//            }
//        }
    }

    var checkTime = 0L
    override fun onLocalVideoStats(
        source: Constants.VideoSourceType?,
        stats: IRtcEngineEventHandler.LocalVideoStats?
    ) {
        if (isOwner) {
            return
        }
        if (checkTime == 0L) {
            checkTime = System.currentTimeMillis()
            return
        }
        if (System.currentTimeMillis() - checkTime > 60000) {
            //1分钟检测一次
            checkTime = System.currentTimeMillis()
            val uid = stats?.uid ?: return
            logComToFile(TAG, "检测鬼麦现象uid=$uid")
            if (!isInSeatByUserId(uid)) {
                //不在座位上 还有声音 出现了鬼麦现象 下麦
                checkTime = 0L
                logComToFile(TAG, "出现鬼麦现象，触发下麦uid=$uid")
                userDownSeat()
//            EmMsgManager.sendCmdMessagePeople(
//                uid.toString(), "", ChatConstant.ACTION_MSG_SIT_DOWN
//            )
            }
        }

    }

    private fun getSeatUserInfoUserId(uid: Int): SeatUserInfo? {
        for (j in seatList.indices) {
            val roomSeatResListDTO = seatList[j]
            val roomUserSeatInfo = roomSeatResListDTO.roomUserSeatInfo
            if (roomUserSeatInfo != null) {
                val userId: Int = roomUserSeatInfo.userId.toInt()
                if (userId == uid) {
                    return roomUserSeatInfo
                }
            }
        }
        return null
    }

    private fun isInSeatByUserId(uid: Int): Boolean {
        for (j in seatList.indices) {
            val roomSeatResListDTO = seatList[j]
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
                if (userId == localUserId) {
                    return roomSeatResListDTO.id
                }
            }
        }
        return -1
    }

    private fun getSeatIdByUserId(userId: Int): Int {
        for (j in seatList.indices) {
            val roomSeatResListDTO = seatList[j]
            val roomUserSeatInfo = roomSeatResListDTO.roomUserSeatInfo
            if (roomUserSeatInfo != null) {
                val searUserId: Int = roomUserSeatInfo.userId.toInt()
                if (searUserId == userId) {
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
        getRoseRankList()
    }

    private fun refreshThreeRoomInfo() {
        if (isOwner && roomSourceBean.getFragmentType() == RoomListBean.FRG_THREE_ROOM) {
            getRoomDetail()
        }
    }

    /**
     * 设置是否显示 预览页面
     */
    protected open fun userVideoStatusChanged(uid: Int, isShowPreload: Boolean) {
    }

    /**
     * 当前用户网络状态发生改变
     */
    protected open fun userNetChanged(uid: String, ifNetDisConnect: Boolean) {
    }

    protected open fun getRoomInfoSuccess() {
        if (roomSourceBean.isThreeRoom()) {
            (seatUserAdapter as ThreeRoomSeatAdapter).roomDetailInfo = roomSourceBean
            if ((seatUserAdapter as ThreeRoomSeatAdapter).roomDetailInfo == null) {
                seatUserAdapter.notifyItemChanged(0)
            } else {
                seatUserAdapter.notifyItemChanged(0, true)
            }
        }
    }

    protected open fun getRoomSeatSuccess(seatList: MutableList<RoomSeatInfo>) {
    }


    companion object {
        var TAG = "liveRoom"
        const val SEAT_TYPE_AUTO = "2" //自动上麦
        const val SEAT_TYPE_APPLY = "1"//申请上麦
        const val SEAT_TYPE_SIT_DOWN = "3"//下麦
    }

    private fun isNotMyRoom(roomId: String?): Boolean {
        return roomSourceBean.uid != roomId
    }

    override fun onChatRoomDestroyed(roomId: String?, roomName: String?) {
        logInfoCom("房主解散聊天室：$roomId")
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
        logComToFile(LiveRoomActivity.LIVE_ROOM_TAG, "解散聊天室roomId=${roomId}---退出直播间")
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
        mViewModel.switchMike(
            Integer.valueOf(roomId),
            operate,
            seatNum,
            operatedUserId,
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    logInfoCom("打开/关闭麦克风")
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
    }

    private var emChatRoom: EMChatRoom? = null
    private fun getChatRoom(): EMChatRoom {
        if (emChatRoom == null) {
            emChatRoom = EMClient.getInstance().chatroomManager().getChatRoom(roomSourceBean.uid)
        }
        return emChatRoom!!
    }

    override fun onMemberJoined(roomId: String?, participant: String?) {
        logInfoCom("新朋友加入聊天室：$participant")
        if (isNotMyRoom(roomId)) {
            return
        }
        runOnUiThread {
            refreshOnlineUser(getChatRoom().memberCount)
            refreshThreeRoomInfo()
        }

    }

    override fun onMemberExited(roomId: String?, roomName: String?, participant: String?) {
        logInfoCom("有成员退出聊天室：$roomName——————$participant")
        if (isNotMyRoom(roomId)) {
            return
        }
        runOnUiThread {
            refreshOnlineUser(getChatRoom().memberCount)
        }

//        val userId = participant!!.toInt()
//        updateUserLeaveView(userId)
    }

    override fun onRemovedFromChatRoom(
        reason: Int, roomId: String?, roomName: String?, participant: String?
    ) {
    }

    override fun onMuteListAdded(
        chatRoomId: String?, mutes: MutableList<String>?, expireTime: Long
    ) {
        logInfoCom("成员被禁言：$mutes——————")
        if (isNotMyRoom(chatRoomId)) {
            return
        }
        runOnUiThread {
            for (mute in mutes!!) {
                if (mute == localStrUserId) {
                    ifMute = true
                    showToast("你已被禁言")
                }
            }
        }

    }

    override fun onMuteListRemoved(chatRoomId: String?, mutes: MutableList<String>?) {
        logInfoCom("成员取消禁言：$mutes——————")
        if (isNotMyRoom(chatRoomId)) {
            return
        }
        runOnUiThread {
            for (mute in mutes!!) {
                if (mute == localStrUserId) {
                    ifMute = false
                    showToast("你已被取消禁言")
                }
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
        request(
            { agoraRxApi.getInviteList(roomId, "0", "0", 1) },
            object : OnRequestResultListener<MutableList<UserDetailInfo>> {
                override fun onSuccess(data: BaseBean<MutableList<UserDetailInfo>>) {
                    val userList = data.data ?: return
                    if (CommonUtils.isPopShow(liveRoomUserListPop)) {
                        return
                    }
                    refreshApplySeatNum(userList)
                    liveRoomUserListPop = LiveRoomSeatManagerPop.showDialog(
                        this@BaseLiveRoomFrg,
                        userList,
                        roomSourceBean,
                        onSendSeatInviteListener = inviteSeatListener
                    )
                }
            })
    }


    /**
     * 直播见检测提示弹框
     * 1. 当房间只有主持1人时，开播后每隔15-30分弹窗让主持人在60s内点“我在”，没点则关闭房间），
     * 关房原因“长时间未点击确认弹窗”。如果进入观众，则等观众离开房间后重新开始计时
     * 2.专属直播间10分钟房间内没有男嘉宾上麦，弹窗提示“请尽快邀请男嘉宾上麦，若5分钟内还没有男嘉宾加入，
     * 系统将自动关闭当前专属房”。关房原因“专属房15分钟没有男嘉宾上麦”
     * 3.主播点击“我在”3次，当前房间不再弹出
     * 以上逻辑全部服务端处理 服务端发送透传后前端展示一个提示弹框
     */
    private var commonTipDialog: CommonTipDialog? = null
    private var roomCheckCountDown: CoroutineScope? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun showContinueLivePop() {
        if (CommonUtils.isPopShow(commonTipDialog)) {
            return
        }
        val build: Spans = if (!roomSourceBean.isPrivateRoom()) {
            getSpans(60)
        } else {
            Spans.builder()
                .text("请尽快邀请男嘉宾上麦，若5分钟内还没有男嘉宾加入，系统将自动关闭当前专属房")
                .build()
        }
        val commonTipsInfo = CommonTipsInfo(
            "温馨提示",
            build,
            if (!roomSourceBean.isPrivateRoom()) "继续直播" else "我知道了",
            true,
            0,
            false
        )
        commonTipDialog = CommonTipDialog.showDialog(
            mContext, commonTipsInfo, object : CommonTipDialog.OnClickBtnListener {
                override fun onClickBtn() {
                    if (!roomSourceBean.isPrivateRoom()) {
                        mViewModel.clickIam(roomId)
                        roomCheckCountDown?.cancel()
                    }
                }
            }, null
        )
        if (!roomSourceBean.isPrivateRoom()) {
            mContext.countDown(60, start = {
                roomCheckCountDown = it
            }, end = {
                //倒计时结束
                commonTipDialog?.dismiss()
            }, next = {
                commonTipDialog?.setDesc(getSpans(it))
            }, cancel = {})
        }
    }

    private fun getSpans(second: Int): Spans {
        return Spans.builder().text("系统检测到你长时间未操作\n")
            .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.color6))
            .text("请点击“继续直播”，\n否则" + second + "秒后将关闭房间")
            .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.colorMain)).build()
    }


    protected fun changeGiftAudioStatus(ivAudio: ImageView, isSave: Boolean = false) {
        val roomSwitchInfo = RoomSwitchCacheManager.getRoomSwitchInfo(roomId)
        if (isSave) {
            roomSwitchInfo.giftVoiceOpen = !roomSwitchInfo.giftVoiceOpen
            RoomSwitchCacheManager.saveRoomSwitchInfo(roomSwitchInfo)
        }
        if (roomSwitchInfo.giftVoiceOpen) {
            ivAudio.setImageResource(R.drawable.svg_voice_on)
        } else {
            ivAudio.setImageResource(R.drawable.svg_voice_off)
        }

    }


    protected fun changeEnterAnimStatus(ivEnterAnim: AppCompatImageView, isSave: Boolean = false) {
        val roomSwitchInfo = RoomSwitchCacheManager.getRoomSwitchInfo(roomId)
        if (isSave) {
            roomSwitchInfo.enterAnimOpen = !roomSwitchInfo.enterAnimOpen
            RoomSwitchCacheManager.saveRoomSwitchInfo(roomSwitchInfo)
        }
        if (roomSwitchInfo.enterAnimOpen) {
            ivEnterAnim.setImageResource(R.drawable.svg_voice_on)
        } else {
            ivEnterAnim.setImageResource(R.drawable.svg_voice_off)
        }

    }

    private fun exitGroup(ownerUserId: String?) {
        DialogUtils.showConfirmDialog("退出粉丝团", {
            request(
                { agoraRxApi.exitGroup(ownerUserId) },
                object : OnRequestResultListener<String> {
                    override fun onSuccess(data: BaseBean<String>) {
                        roomGroupMemberPop?.dismiss()
                        showToast("已退出")
                        exitGroupSuccess()
                    }
                })
        }, {}, "确认立即退出粉丝团吗？", confirm = "确认", cancel = "取消")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        VideoCanvasPool.clear()
        countDown?.cancel()
        roomCheckCountDown?.cancel()
        faceEffectCountDown?.cancel()
        mBinding.roomPkView.onDestroy()
        sendCmdQueueTask.clear()
        applyQueueTask.clear()
        giftAnimTaskManager.clear()
    }

}

