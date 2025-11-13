package cn.yanhu.agora.manager

import android.graphics.Color
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.EnterCheckResponse
import cn.yanhu.agora.manager.dbCache.AgoraSdkCacheManager
import cn.yanhu.agora.manager.dbCache.BeautyCacheManager
import cn.yanhu.agora.miniwindow.MiniWindowManager
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.imchat.manager.CutLiveRoomUtils
import cn.yanhu.imchat.manager.EmMsgManager
import cn.yanhu.imchat.manager.ImUserManager
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ErrorCode
import com.hyphenate.chat.EMClient
import com.pcl.sdklib.listener.OnPayResultListener
import com.pcl.sdklib.manager.PayManager


object LiveRoomManager {
    const val HOUSE_OWNER_OFF = 1001 //房主关闭直播间
    const val HOUSE_CUT_EXTRA_KICK = 1002 //房主切换直播间类型为：专属，其余观众被踢出直播间
    const val HOUSE_NOT_FUNDS = 1003 //余额不足，专属房间结束
    const val HOUSE_OFF = 1004 //直播间结束，观众收到通知
    const val HOUSE_ADMINISTRATOR_OFF = 1005 //管理员强制关闭直播间
    const val HOUSE_CALL_OFF = 1006 //通话结束
    const val HOUSE_CALL_PRICE_OFF = 1007 //通话余额不足结束
    var chatRoomId = ""
    @JvmStatic
    fun toLiveRoomPage(context: FragmentActivity, applyRoomId: String) {
        toLiveRoomPage(context, applyRoomId, null)
    }

    @JvmStatic
    fun toLiveRoomPage(
        context: FragmentActivity,
        applyRoomId: String,
        onRoomNoExitListener: OnRoomNoExitListener? = null
    ) {
        if (!BeautyCacheManager.hasLoadBeautySdk()) {
            //showToast("请等待美颜插件加载完成")
            DialogUtils.dismissLoading()
            LiveDataEventManager.sendLiveDataMessage(
                EventBusKeyConfig.SHOW_BEAUTY_SDK_DOWNLOAD_PROGRESS,
                true
            )
        } else if (!AgoraSdkCacheManager.hasLoadAgoraSdk()) {
            //showToast("请等待插件加载完成")
            DialogUtils.dismissLoading()
            LiveDataEventManager.sendLiveDataMessage(
                EventBusKeyConfig.SHOW_AGORA_SDK_DOWNLOAD_PROGRESS,
                true
            )
        } else {
            if (AgoraManager.isLiveRoom && AgoraManager.getInstance().currentRoomID == applyRoomId) {
                val liveRoomActivity = ApplicationProxy.instance.getLiveRoomActivity()
                if (liveRoomActivity != null) {
                    MiniWindowManager.switchLiveToFront(
                        context,
                        liveRoomActivity
                    )
                } else {
                    //liveRoomActivity 被系统销毁了 重新进入房间
                    AgoraManager.isLiveRoom = false
                    AgoraManager.getInstance().leaveChannel()
                    enterCheck(context, applyRoomId, onRoomNoExitListener)
                }
                DialogUtils.dismissLoading()
            } else {
                enterCheck(context, applyRoomId, onRoomNoExitListener)
            }
        }
    }


    /**
     * 判断是否可以直接进入房间
     */
    var isRequestCheck = false
    private fun enterCheck(
        context: FragmentActivity,
        applyRoomId: String,
        onRoomNoExitListener: OnRoomNoExitListener?
    ) {
        if (isRequestCheck) {
            return
        }
        if (!TextUtils.isEmpty(chatRoomId)){
            EMClient.getInstance().chatroomManager().leaveChatRoom(chatRoomId)
        }
        DialogUtils.showLoading(hasShadow = false)
        isRequestCheck = true
        request(
            { agoraRxApi.enterCheck(applyRoomId) },
            object : OnRequestResultListener<EnterCheckResponse> {
                override fun onSuccess(data: BaseBean<EnterCheckResponse>) {
                    isRequestCheck = false
                    DialogUtils.dismissLoading()
                    val enterCheckResponse = data.data ?: return
                    if (enterCheckResponse.pass) {
                        if (AgoraManager.isLiveRoom && ApplicationProxy.instance.getLiveRoomActivity() != null) {
                            CutLiveRoomUtils.showChangeAlert(object :
                                CutLiveRoomUtils.ChangeListener {
                                override fun sure() {
                                    RouteIntent.lunchToLiveRoom(
                                        context,
                                        enterCheckResponse.roomInfo
                                    )
                                }
                            })
                        } else {
                            val roomInfo = enterCheckResponse.roomInfo
                            RouteIntent.lunchToLiveRoom(context, roomInfo)
                        }
                    } else {
                        if (AgoraManager.isLiveRoom) {
                            CutLiveRoomUtils.showChangeAlert(object :
                                CutLiveRoomUtils.ChangeListener {
                                override fun sure() {
                                    showEnterRoomDialog(
                                        context,
                                        enterCheckResponse,
                                        applyRoomId,
                                        onRoomNoExitListener
                                    )
                                }
                            })
                        } else {
                            showEnterRoomDialog(
                                context,
                                enterCheckResponse,
                                applyRoomId,
                                onRoomNoExitListener
                            )
                        }
                        DialogUtils.dismissLoading()
                    }
                }

                override fun onFail(code: Int?, msg: String?) {
                    DialogUtils.dismissLoading()
                    isRequestCheck = false
                    if ("房间不存在" == msg) {
                        onRoomNoExitListener?.onNoExit()
                        LiveDataEventManager.sendLiveDataMessage(
                            EventBusKeyConfig.CLOSELIVEROOM,
                            applyRoomId
                        )
                    } else if (code == ErrorCode.COMMON_TIP_SKIP) {
                        DialogUtils.showConfirmDialog(msg!!, {
                        }, {

                        }, cancel = "", confirm = "好吧", isHideCancel = true)
                    } else {
                        showToast(msg)
                    }
                }

            }, isShowToast = false,
            activity = context
        )

    }

    private fun showEnterRoomDialog(
        context: FragmentActivity,
        data: EnterCheckResponse,
        applyRoomId: String,
        onRoomNoExitListener: OnRoomNoExitListener?
    ) {
        if (!data.info.roseEnough) {
            showToast("玫瑰不足，需要充值")
            showRechargeListDialog(context, applyRoomId, onRoomNoExitListener)
            return
        }
        val content =
            Spans.builder().text("申请通过后可进入专属房间交友").color(Color.parseColor("#666666"))
                .text(if (data.info.seatNum == 2) "\n\n专属房间需消耗" + data.info.price + "玫瑰/分钟，是否申请?" else "")
                .color(
                    CommonUtils.getColor(
                        cn.yanhu.baselib.R.color.colorMain
                    )
                ).build()
        DialogUtils.showConfirmDialog(
            "申请专属房",
            {

                if (data.roomInfo.roomType == RoomListBean.TYPE_ROBOT_ROOM) {
                    showToast("已发送申请")
                    return@showConfirmDialog
                }
                val selfUserInfo = ImUserManager.getSelfUserInfo()
                val map: MutableMap<String, Any> = HashMap()
                map["fromUid"] = AppCacheManager.userId
                map["seatId"] = data.info.seatNum
                map["fromNickName"] = data.info.nickName
                map["portrait"] = selfUserInfo.portrait
                showToast("已发送申请")
                EmMsgManager.sendCmdMessagePeople(
                    data.info.roomUserId,
                    ChatConstant.ACTION_MSG_APPLY_SET_UP,
                    map
                )
            },
            {
            },
            content = content,
            cancel = "放弃机会",
            confirm = "申请",
            cancelBg = cn.yanhu.baselib.R.drawable.shape_cancel_btn_r30
        )

    }


    //显示充值列表
    private fun showRechargeListDialog(
        context: FragmentActivity,
        applyRoomId: String,
        onRoomNoExitListener: OnRoomNoExitListener?
    ) {
        ApplicationProxy.instance.showRechargePop(context, true)
        PayManager.registerPayResult(context, object : OnPayResultListener {
            override fun onPaySuccess() {
                enterCheck(context, applyRoomId, onRoomNoExitListener)
            }
        })
    }

    interface OnRoomNoExitListener {
        fun onNoExit();
    }
}