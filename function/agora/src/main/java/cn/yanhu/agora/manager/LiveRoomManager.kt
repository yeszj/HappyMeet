package cn.yanhu.agora.manager

import androidx.fragment.app.FragmentActivity
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.EnterCheckResponse
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
//        request({agoraRxApi.getAgoraToken(applyRoomId)},object : OnRequestResultListener<String>{
//            override fun onSuccess(data: BaseBean<String>) {
//                AgoraManager.getInstence().preloadChannel(context,applyRoomId,data.data)
//            }
//        })
        if (AgoraSdkCacheManager.hasLoadAgoraSdk()) {
            if (AgoraManager.isLiveRoom && AgoraManager.getInstence().currentRoomID == applyRoomId) {
                val liveRoomActivity = ApplicationProxy.instance.getLiveRoomActivity()
                if (liveRoomActivity != null) {
                    MiniWindowManager.switchLiveToFront(
                        context,
                        liveRoomActivity
                    )
                } else {
                    showToast("房间数据异常")
                }
            } else {
                enterCheck(context, applyRoomId, onRoomNoExitListener)
            }
        } else {
            LiveDataEventManager.sendLiveDataMessage(
                EventBusKeyConfig.SHOW_AGORA_SDK_DOWNLOAD_PROGRESS,
                true
            )
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
        //DialogUtils.showLoading()
        if (isRequestCheck){
            return
        }
        isRequestCheck = true
        request(
            { agoraRxApi.enterCheck(applyRoomId) },
            object : OnRequestResultListener<EnterCheckResponse> {
                override fun onSuccess(data: BaseBean<EnterCheckResponse>) {
                    //DialogUtils.dismissLoading()
                    isRequestCheck = false
                    val enterCheckResponse = data.data ?: return
                    if (enterCheckResponse.pass) {
                        if (AgoraManager.isLiveRoom && ApplicationProxy.instance.getLiveRoomActivity() != null) {
                            CutLiveRoomUtils.showChangeAlert(object : CutLiveRoomUtils.ChangeListener {
                                override fun sure() {
                                    RouteIntent.lunchToLiveRoom(context, enterCheckResponse.roomInfo)
                                }
                            })
                        } else {
                            val roomInfo = enterCheckResponse.roomInfo
                            roomInfo.roomType = RoomListBean.TYPE_THREE_ROOM
                            RouteIntent.lunchToLiveRoom(context, roomInfo)
                        }
                    } else {
                        if (AgoraManager.isLiveRoom) {
                            CutLiveRoomUtils.showChangeAlert(object :
                                CutLiveRoomUtils.ChangeListener {
                                override fun sure() {
                                    showEnterRoomDialog(
                                        context, enterCheckResponse, applyRoomId, onRoomNoExitListener
                                    )
                                }
                            })
                        } else {
                            showEnterRoomDialog(context, enterCheckResponse, applyRoomId, onRoomNoExitListener)
                        }
                    }
                }

                override fun onFail(code: Int?, msg: String?) {
                   // DialogUtils.dismissLoading()
                    isRequestCheck = false
                    if ("房间不存在" == msg) {
                        onRoomNoExitListener?.onNoExit()
                        LiveDataEventManager.sendLiveDataMessage(
                            EventBusKeyConfig.CLOSELIVEROOM,
                            applyRoomId
                        )
                    }
                }

            },
            activity = context
        )

    }

    private fun showEnterRoomDialog(
        context: FragmentActivity,
        data: EnterCheckResponse,
        applyRoomId: String,
        onRoomNoExitListener: OnRoomNoExitListener?
    ) {

        val content = Spans.builder().text("申请后可进入专属交友房间免费交友\n\n")
            .text(if (data.info.seatNum == 2) "专属房间需消耗" + data.info.price + "🌹/分钟，是否申请?" else "")
            .color(
                CommonUtils.getColor(
                    cn.yanhu.baselib.R.color.colorMain
                )
            ).build()
        DialogUtils.showConfirmDialog(
            "申请专属私密约会",
            {
                if (!data.info.roseEnough) {
                    showToast("玫瑰不足，需要充值")
                    showRechargeListDialog(context, applyRoomId, onRoomNoExitListener)
                    return@showConfirmDialog
                }
                val selfUserInfo = ImUserManager.getSelfUserInfo()
                val map: MutableMap<String, Any> = HashMap()
                map["fromUid"] = AppCacheManager.userId
                map["seatId"] = data.info.seatNum
                map["fromNickName"] = data.info.nickName
                map["portrait"] = selfUserInfo.portrait
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
        ApplicationProxy.instance.showRechargePop(context,true)
        PayManager.registerPayResult(context, object : OnPayResultListener {
            override fun onPaySuccess() {
                enterCheck(context,applyRoomId,onRoomNoExitListener)
            }
        })
    }

    interface OnRoomNoExitListener {
        fun onNoExit();
    }
}