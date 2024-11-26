package cn.yanhu.agora.ui.imphone

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import cn.yanhu.agora.R
import cn.yanhu.agora.bean.CheckCallBalanceRes
import cn.yanhu.agora.databinding.ActivityVideoPhoneBinding
import cn.yanhu.agora.listener.IRtcEngineEventHandlerListener
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.manager.AgoraPhoneManager
import cn.yanhu.agora.manager.LiveRoomManager
import cn.yanhu.agora.miniwindow.MiniWindowManager
import cn.yanhu.agora.manager.PermissionManager
import cn.yanhu.agora.miniwindow.EaseCallFloatWindow
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.DateUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.ChatCallResponseInfo
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.GiftSendModel
import cn.yanhu.commonres.bean.SendGiftRequest
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.router.RouterPath
import cn.yanhu.commonres.utils.PermissionXUtils
import cn.yanhu.commonres.view.GiftFrameLayout
import cn.yanhu.imchat.manager.ChatCallStatusConfig
import cn.yanhu.imchat.manager.EmMsgManager
import cn.yanhu.imchat.pop.SendGiftPop
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.parseState
import com.alibaba.android.arouter.facade.annotation.Route
import com.hyphenate.chat.EMMessage
import com.hyphenate.exceptions.HyphenateException
import com.jeremyliao.liveeventbus.LiveEventBus
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAParser.PlayCallback
import com.opensource.svgaplayer.SVGAVideoEntity
import com.pcl.sdklib.listener.OnPayResultListener
import com.pcl.sdklib.manager.PayManager
import io.agora.rtc2.IRtcEngineEventHandler
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.util.Timer
import java.util.TimerTask

/**
 * @author: zhengjun
 * created: 2024/10/14
 * desc:
 */
@Route(path = RouterPath.ROUTER_VIDEO_PHONE)
class VideoPhoneActivity : BaseActivity<ActivityVideoPhoneBinding, ImPhoneViewModel>(
    R.layout.activity_video_phone,
    ImPhoneViewModel::class.java
), IRtcEngineEventHandlerListener {
    private var floatView: VideoPhoneFloatView? = null

    private var callTimer: Timer? = null

    private var timer: Long = 0
    private var isCallUserConnect = false //对方是否连接


    private var isFinish = false

    private var handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == 1) {
                timer += 1000
                val timerStr: String? = DateUtils.stringForTime(timer)
                mBinding.timer = timerStr
                if (EaseCallFloatWindow.getInstance().isShowing) {
                    EaseCallFloatWindow.getInstance().setTime(timerStr)
                }
                if (timer == 30000L) {
                    mBinding.notice.visibility = View.GONE
                }

                //检测余额是否足够支付下一分钟
                checkUserBalance()

                //每分钟扣款
                deductionData()

                //免费时间计时
                setFreeTimeValue()

                //余额不足，剩余时间计时
                setBalanceTimeValue()
            }
        }
    }
    private var callInfo: ChatCallResponseInfo? = null
     var chatUserId: Int = 0
    override fun initData() {
        try {
            AgoraManager.isLiveRoom = true
            setFullScreenStatusBar(false)
            setTitleMarginTop(mBinding.topCl)
            callInfo = intent.getSerializableExtra(IntentKeyConfig.DATA) as ChatCallResponseInfo
            val chatUser = if (AppCacheManager.userId == callInfo!!.user.id.toString()) {
                callInfo!!.chatUser
            } else {
                callInfo!!.user
            }
            chatUserId = chatUser.id.toInt()
            mBinding.chatUserInfo = chatUser
            AgoraPhoneManager.getInstance().userInfo = callInfo
            getCallInfo()

        } catch (e: Exception) {
            showToast("视频通话初始化异常，请重试")
            leave()
        }
        //设置圆角
        ViewUtils.setViewCorner(mBinding.callToVideoSf, 40)

    }

    override fun initListener() {
        super.initListener()
        mBinding.vgHangUp.setOnClickListener { hangUpConnectTimeOut() }
        onRechargeResultListener()
        LiveEventBus.get<EMMessage>(EventBusKeyConfig.RECEIVE_CMD_MSG).observe(this) {
            solveCmdMsg(it)
        }
        mBinding.topBarEye.setOnSingleClickListener {
            //开、关对方摄像头
            if (!mBinding.chatVideoClose) {
                val isCloseVideo: Boolean = !mBinding.callInfo!!.chatUser.colseVideo
                mBinding.callInfo!!.chatUser.colseVideo = isCloseVideo
                isShowVideo(false, isCloseVideo)
            }
        }
        mBinding.topBarWmode.setOnSingleClickListener {
            showFloatWindow()
        }
        mBinding.callGift.setOnSingleClickListener {
            val sendUserInfo = UserDetailInfo()
            sendUserInfo.userId = chatUserId.toString()
            SendGiftPop.showDialog(
                mContext,
                sendUserInfo,
                SendGiftRequest.SOURCE_CALL,
                callInfo!!.id,
                object : SendGiftPop.OnSendGiftListener {
                    override fun onSendGift(item: GiftInfo) {
                        //赠送礼物提示
                        starGiftAnimation(
                            createGiftSendModel(
                                callInfo!!.user.nickName,
                                callInfo!!.user.portrait,
                                "送出 " + item.name,
                                item.giftIcon,
                                item.sendNumber
                            )!!
                        )
                        playSvga(item.svga)
                    }

                })
        }
        mBinding.callVideo.setOnSingleClickListener {
            //切换前后摄像头
            AgoraPhoneManager.getInstance().switchCamera()
        }
        mBinding.callRecharge.setOnSingleClickListener {
            //充值
            mBinding.callRecharge.visibility = View.GONE
            showRechargePop()
        }
        mBinding.callFinish.setOnSingleClickListener {
            //结束通话
            callFinish(
                true,
                object : OnLeaveListener {
                    override fun onLeave() {
                    }
                })
        }
    }

    private fun showRechargePop() {
        ApplicationProxy.instance.showRechargePop(
            mContext,
            hasShadow = false,
            isDismissWhenPaySuccess = true
        )
    }

    private fun solveCmdMsg(it: EMMessage) {
        val source: Int
        try {
            source = it.getIntAttribute("source")
            logcom("透传消息类型：$source")
            if (source == ChatConstant.ACTION_PHONE_CALL_FINISH) { //结束通话
                if (!isFinish) {
                    isFinish = true
                    closeFloatWidow()
                    callFinish(
                        false,
                        object : OnLeaveListener {
                            override fun onLeave() {
                                toCallEndActivity(
                                    LiveRoomManager.HOUSE_CALL_OFF
                                )
                            }
                        })
                }
            } else if (source == ChatConstant.ACTION_PHONE_SEND_GIFT) { //赠送礼物
                val giftName: String = it.getStringAttribute("giftName")
                val giftIcon: String = it.getStringAttribute("giftIcon")
                val num: String = it.getStringAttribute("num")
                val svga: String = it.getStringAttribute("svga")
                //赠送礼物提示
                starGiftAnimation(
                    createGiftSendModel(
                        callInfo!!.chatUser.nickName,
                        callInfo!!.chatUser.portrait,
                        "送出 $giftName",
                        giftIcon,
                        num.toInt()
                    )!!
                )
                playSvga(svga)
            } else if (source == ChatConstant.ACTION_MSG_CALL_ROSE_LACK_ALERT) { //一对一通话余额不足，提示男嘉宾
                showBalanceNoEnoughTips(true)
            } else if (source == ChatConstant.ACTION_MSG_CALL_ROSE_LACK_FINISH) { //一对一通话余额不足，结束通话
                finishCallWhenBalanceNoEnough()
            } else if (source == ChatConstant.ACTION_PHONE_CALL_BALANCE_NOT_ENOUGH) {
                //男方余额不足 30s后挂断 提示女方
                showBalanceNoEnoughTips(false)
            }
        } catch (e: HyphenateException) {
            logcom(e.message)
        }
    }

    private fun setBalanceTimeValue() {
        val remainTime = mBinding.remainTime!!
        if (remainTime > 0) {
            mBinding.remainTime = remainTime - 1
        } else if ( mBinding.remainTimer.visibility == View.VISIBLE) {
            showUserInfo()
        }
    }

    private fun setFreeTimeValue() {
        val freeTime = mBinding.freeTime!!
        if (freeTime > 0) {
            mBinding.freeTime = freeTime - 1
        } else if ( mBinding.freeTimer.visibility == View.VISIBLE) {
            mBinding.freeTimer.visibility = View.GONE
        }
    }

    private fun deductionData() {
        if (timer * 0.001 % 60 == 0.0) {
            logcom("时长：$timer——————请求扣款")
            val spendUserId: Int = callInfo!!.spendUserId//扣费用户id
            if (spendUserId == callInfo!!.user.id.toInt()) {
                if (checkCallBalanceRes == null || checkCallBalanceRes!!.ifBalanceEnough) {
                    //余额不足时挂断不调扣费接口(服务端判断扣费)  如果扣费用户id是我自己 则扣费
                    mViewModel.deduction(callInfo!!.id.toString())
                }
            }
            if (checkCallBalanceRes != null && !checkCallBalanceRes!!.ifBalanceEnough) {
                finishCallWhenBalanceNoEnough()
            }
        }
    }

    private var isRemainLack = false
    private fun finishCallWhenBalanceNoEnough() {
        logcom("钱不足，结束通话")
        closeFloatWidow()
        callTimer?.cancel()
        isRemainLack = true
        callFinish(
            false,
            object : OnLeaveListener {
                override fun onLeave() {
                    toCallEndActivity(LiveRoomManager.HOUSE_CALL_PRICE_OFF)
                }
            })
    }

    private fun toCallEndActivity(type: Int) {
        CallPhoneEndActivity.lunch(mContext, type)
    }

    private fun getCallInfo() {
        mViewModel.startCall(
            chatUserId.toString(),
            "1",
            ChatCallStatusConfig.STATUS_COMMIT,
            callInfo!!.uid
        )
        mViewModel.chatCallObserver.observe(this) { it ->
            parseState(it, {
                callInfo = it
                it.chatType = 1
                mBinding.callInfo = it
                mBinding.chatUserInfo = it.chatUser
                mBinding.notice.visibility = View.VISIBLE
                AgoraPhoneManager.getInstance().userInfo = it
                bgBlur(it.chatUser.portrait)
                isRequestPermission()
            })
        }
    }

    //判断是否授权必要权限
    private fun isRequestPermission() {
        PermissionManager.checkVideoPermission(mContext, object :
            PermissionXUtils.PermissionListener {
            override fun onSuccess() {
                sdkInit()
            }

            override fun onFail() {
                sdkInit()
            }
        })
    }


    /*
     * 结束通话
     * */
    private fun callFinish(isSelfFinish: Boolean, onLeaveListener: OnLeaveListener) {
        logcom("结束通话：$isSelfFinish")
        if (mBinding.callInfo == null) {
            leave(onLeaveListener)
        }
        if (isSelfFinish) {
            DialogUtils.showConfirmDialog("结束视频通话", {
                logcom("通话时长：" + mBinding.timer)
                EmMsgManager.sendCmdMessagePeople(
                    callInfo!!.chatUser.id.toString(),
                    ChatConstant.ACTION_PHONE_CALL_FINISH,
                    null,
                )
                leave(onLeaveListener)
            }, {

            }, "确定结束一对一视频通话吗？", context = mContext)
        } else {
            closeFloatWidow()
            leave(onLeaveListener)
        }
    }

    private fun hangUpConnectTimeOut() {
        if (!isCallUserConnect) {
            leave()
        }
    }

    private var waitTimer: CountDownTimer? = null

    private fun startCountTime() {
        Log.e("agoraListener", "开启连接倒计时")
        waitTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                showToast("对方长时间未连接，通话结束")
                hangUpConnectTimeOut()
            }
        }
        waitTimer?.start()
    }

    private fun sdkInit() {
        try {
            AgoraPhoneManager.getInstance().setRtcEngineEventHandlerListener(this)
            //声网初始化
            AgoraPhoneManager.getInstance().init(this, true)
            //是否进入频道  0：进入
            val joined = AgoraPhoneManager.getInstance().joinChannel(
                callInfo!!.user.id.toInt(),
                callInfo!!.uid,
                callInfo!!.agoraToken
            )
            //判断通话加入是否成功  0：success
            if (joined == 0) {
                mBinding.remainTime = 0
                mBinding.freeTime = 0
                //判断是否有免费通话时长，且是否为付费方，顶部弹出提示
//                if ( mBinding.callInfo.user
//                        .getId().toInt() == mBinding.callInfo.spendUserId
//                ) {
//                    mBinding.freeTimer.visibility = View.VISIBLE
//                    mBinding.freeTime = 60
//                } else {
//                    mBinding.freeTime = 0
//                }
                startCountTime()
            } else {
                showToast("视频通话连接失败")
                leave()
            }
        } catch (e: java.lang.Exception) {
            showToast("通话异常终止")
            leave()
        }
    }


    private var checkCallBalanceRes: CheckCallBalanceRes? = null
    private fun checkUserBalance() {
        //每分钟的第三十秒 检测用户余额是否充足 不够提醒用户充值
        val spendUserId: Int = callInfo!!.spendUserId//扣费用户id
        if (spendUserId == callInfo!!.user.id.toInt()) {
            if (timer * 0.001 % 60 == 30.0) {
                mViewModel.checkCallBalance(java.lang.String.valueOf(callInfo!!.id))
                mViewModel.checkCallBalanceObserver.observe(this) { it ->
                    parseState(it, {
                        checkCallBalanceRes = it
                        if (!it.ifBalanceEnough) {
                            EmMsgManager.sendCmdMessagePeople(
                                chatUserId.toString(),
                                ChatConstant.ACTION_PHONE_CALL_BALANCE_NOT_ENOUGH,
                                null
                            )
                            showBalanceNoEnoughTips(true)
                        }
                    })


                }
            }
        }
    }


    private fun showBalanceNoEnoughTips(isShowRecharge: Boolean) {
        logcom("余额不足")
        val downTime = (60 - timer * 0.001 % 60).toInt()
        mBinding.remainTime = downTime
        mBinding.remainTimer.visibility = View.VISIBLE
        mBinding.callToUserNickname.visibility = View.INVISIBLE
        mBinding.callToUserDetail.visibility = View.INVISIBLE
        mBinding.callRecharge.visibility = View.GONE
        if (isShowRecharge) {
            mBinding.tvHangUpTips.text = "玫瑰余额不足，通话即将结束("
            showRechargePop()
        } else {
            mBinding.tvHangUpTips.text = "对方玫瑰不足，通话即将结束("
        }
    }

    private fun onRechargeResultListener() {
        PayManager.registerPayResult(mContext, object : OnPayResultListener {
            override fun onPaySuccess() {
                checkCallBalanceRes?.ifBalanceEnough = true
                if (mBinding.remainTimer.visibility == View.VISIBLE) {
                    mBinding.remainTime = 0
                    showUserInfo()
                }
            }
        })
    }

    private fun showUserInfo() {
        mBinding.remainTimer.visibility = View.GONE
        mBinding.callToUserNickname.visibility = View.VISIBLE
        mBinding.callToUserDetail.visibility = View.VISIBLE
    }

    //是否打开摄像头
    private fun isShowVideo(isLocal: Boolean, isClose: Boolean) {
        if (isLocal == floatView!!.isLocal) {
            floatView?.isShowVideo(isClose)
        } else {
            mBinding.callToVideoSf.visibility = if (isClose) View.INVISIBLE else View.VISIBLE
            mBinding.toBgPreload.visibility = if (isClose) View.VISIBLE else View.GONE
            mBinding.callToUserPortrait.visibility = if (isClose) View.VISIBLE else View.GONE
        }
    }

    private val playUrlList: MutableList<String> = ArrayList()
    private var isPlay = false

    //礼物动效
    private fun playSvga(url: String) {
        if (isPlay) {
            playUrlList.add(url)
            return
        }
        isPlay = true
        val svgaParser = SVGAParser(this)
        try {
            svgaParser.decodeFromURL(URL(url), object : SVGAParser.ParseCompletion {
                override fun onComplete(svgaVideoEntity: SVGAVideoEntity) {
                    val svgaDrawable = SVGADrawable(svgaVideoEntity)
                    // 设置drawable 资源
                    mBinding.callSvgaView.setImageDrawable(svgaDrawable)
                    // 暂停动画，停在第一个页面
                    //_SVGAImageView.pauseAnimation();

                    // 设置为填充模式
                    svgaDrawable.scaleType = ImageView.ScaleType.FIT_XY
                    mBinding.callSvgaView.loops = 1
                    // 开始播放动画
                    mBinding.callSvgaView.startAnimation()
                    // 设置回调
                    mBinding.callSvgaView.callback = (object : SVGACallback {
                        override fun onPause() {
                            // 暂停
                        }

                        override fun onFinished() {
                            // 完成
                            isPlay = false
                            if (!playUrlList.isEmpty()) {
                                val s = playUrlList[0]
                                playUrlList.removeAt(0)
                                playSvga(s)
                            }
                        }

                        override fun onRepeat() {}
                        override fun onStep(i: Int, v: Double) {}
                    })
                }

                override fun onError() {
                }
            }, object : PlayCallback {
                override fun onPlay(file: List<File>) {

                }
            })
        } catch (e: MalformedURLException) {
            throw RuntimeException(e)
        }
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

    private fun leave() {
        leave(object : OnLeaveListener {
            override fun onLeave() {

            }
        })
    }

     fun leave(onLeaveListener: OnLeaveListener) {
        if (callInfo == null) {
            return
        }
        val second = (timer * 0.001).toInt()
        mViewModel.finishCall(
            java.lang.String.valueOf(callInfo!!.chatUser.id),
            "0",
            if (isCallUserConnect) "4" else "5",
            callInfo!!.uid,
            second, object : cn.yanhu.agora.listener.OnLeaveListener {
                override fun onLeave(response: ChatCallResponseInfo?) {

                }
            }
        )
        onLeaveListener.onLeave()
        isFinish = true
        finish()
    }

    interface OnLeaveListener {
        fun onLeave()
    }

    override fun agoraListener(type: Int, uid: Int) {
        runOnUiThread {
            if (type == AgoraPhoneManager.TOKEN_EXPIRE || type == AgoraPhoneManager.TOKEN_WILL_EXPIRE) { //token已过期、token即将过期
                fetchToken()
            } else if (type == AgoraPhoneManager.TO_USER_LINE) { //远端用户连接通话
                Log.e("agoraListener", "agoraListener: 远端用户连接通话")
                if (waitTimer != null) {
                    waitTimer!!.cancel()
                }
                showSelfVideoView()
                isCallUserConnect = true
                mBinding.vgWaiting.visibility = View.INVISIBLE
                startCallTime()
                AgoraPhoneManager.getInstance().setupRemoteVideo(
                    chatUserId,
                    mBinding.callToVideoSf
                )
            } else if (type == AgoraPhoneManager.TO_USER_OFFLINE_QUIT) { //用户结束了通话
                if (!isFinish) {
                    isFinish = true
                    closeFloatWidow()
                    callFinish(
                        false,
                        object : OnLeaveListener {
                            override fun onLeave() {
                                toCallEndActivity(
                                    LiveRoomManager.HOUSE_CALL_OFF
                                )
                            }
                        })
                }
            } else if (type == AgoraPhoneManager.TO_USER_MIC_OPEN) { //远端用户麦克风状态
                mBinding.callInfo!!.chatUser.colseMic = false
            } else if (type == AgoraPhoneManager.TO_USER_MIC_CLOSE) { //远端用户麦克风状态
                mBinding.callInfo!!.chatUser.colseMic = true
            } else if (type == AgoraPhoneManager.TO_USER_VIDEO_OPEN) { //远端用户摄像头状态
                mBinding.callInfo!!.chatUser.colseVideo = false
                mBinding.chatVideoClose = false
                isShowVideo(false, false)
            } else if (type == AgoraPhoneManager.TO_USER_VIDEO_CLOSE) { //远端用户摄像头状态
                mBinding.callInfo!!.chatUser.colseVideo = true
                mBinding.chatVideoClose = true
                isShowVideo(false, true)
            } else if (type == AgoraPhoneManager.USER_LOCAL_VIDEO_INIT_FINISH) { //本地摄像头初始化完毕
                isShowVideo(true, false)
            } else if (type == AgoraPhoneManager.USER_REMOTE_VIDEO_INIT_FINISH) { //远端摄像头初始化完毕
                isShowVideo(isLocal = false, isClose = false)
            }
        }
    }

    private fun startCallTime() {
        mBinding.tvTime.visibility = View.VISIBLE
        //注册通话计时器，开始计时通话时长
        mBinding.timer = "00:00:00"
        callTimer = Timer()
        callTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (!isFinishing) {
                    val obtain = Message.obtain()
                    obtain.what = 1
                    handler.sendMessage(obtain)
                }
            }
        }, 1000, 1000)
    }


    private fun showSelfVideoView() {
        floatView = VideoPhoneFloatView(this)
        floatView?.updataView(this, callInfo!!.user, true)
        floatView?.videoView?.setOnClickListener { v ->
            if (floatView?.isLocal == true) {
                AgoraPhoneManager.getInstance().setupLocalVideo(mBinding.callToVideoSf)
                floatView?.updataView(
                    this@VideoPhoneActivity,
                    callInfo!!.chatUser,
                    false
                )
                isShowVideo(true, mBinding.callInfo!!.user.colseVideo)
                bgBlur(callInfo!!.user.portrait)
            } else {
                AgoraPhoneManager.getInstance().setupRemoteVideo(
                    chatUserId,
                    mBinding.callToVideoSf
                )
                floatView?.updataView(this@VideoPhoneActivity, callInfo!!.user, true)
                isShowVideo(false, mBinding.callInfo!!.chatUser.colseVideo)
                bgBlur(callInfo!!.chatUser.portrait)
            }
        }

    }

    /*
     * 背景模糊
     * */
    private fun bgBlur(portrait: String) {

        GlideUtils.loadBlurTransPic(mContext, portrait, imageView = mBinding.callToUserPortrait)
    }

    /*
     * 获取声网token
     * */
    private var isRequestToken = false
    private fun fetchToken() {
        runOnUiThread(Runnable {
            if (isRequestToken) {
                return@Runnable
            }
            isRequestToken = true
            mViewModel.getAgoraToken(callInfo!!.uid)
            mViewModel.getTokenObserver.observe(this) {
                parseState(it, {
                    isRequestToken = false
                    callInfo!!.agoraToken = it
                    AgoraPhoneManager.getInstance().renewToken(it)
                }, {
                    isRequestToken = false
                })
            }
        })
    }

    override fun onAudioVolumeIndication(
        speakers: Array<out IRtcEngineEventHandler.AudioVolumeInfo>?,
        totalVolume: Int
    ) {
    }


    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        showFloatWindow()
    }


    //获取悬浮窗权限
    private fun showFloatWindow() {
        PermissionXUtils.checkAlertPermission(
            this,
            "关闭房间",
            object : PermissionXUtils.OnAlertPermissionListener {
                override fun onSuccess() {
                    doShowFloatWindow()
                }

                override fun onFail() {}
                override fun onClose() {
                }
            })
    }

    private fun doShowFloatWindow() {
        if (isDestroyed || callInfo == null) {
            return
        }
        val chatUser = callInfo!!.chatUser
        EaseCallFloatWindow.getInstance().initFloatWindow(applicationContext, 1)
        EaseCallFloatWindow.getInstance().show(chatUser.id.toInt())
        MiniWindowManager.switchLiveToMiniFloat(mContext)
    }

    private fun closeFloatWidow() {
        if (EaseCallFloatWindow.getInstance().isShowing) {
            EaseCallFloatWindow.getInstance().dismiss()
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent!!.getBooleanExtra("isFinish", false)) {
            callFinish(
                true,
                object : OnLeaveListener {
                    override fun onLeave() {
                    }
                })
        }
        if (floatView == null) {
            return
        }
        if (floatView!!.isLocal) {
            AgoraPhoneManager.getInstance().setupRemoteVideo(
                callInfo!!.chatUser.id.toInt(),
                mBinding.callToVideoSf
            )
        } else {
            floatView!!.updataView(this, callInfo!!.chatUser, false)
        }
    }

    override fun onStart() {
        super.onStart()
        if (EaseCallFloatWindow.getInstance().isShowing) {
            EaseCallFloatWindow.getInstance().dismiss()
            if (AgoraPhoneManager.getInstance().isInit) {
                if (floatView!!.isLocal) {
                    floatView?.updataView(this, callInfo!!.user, true)
                } else {
                    AgoraPhoneManager.getInstance().setupLocalVideo(mBinding.callToVideoSf)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        var isFloatPermission = true
        isFloatPermission = Settings.canDrawOverlays(this)
        if (!isFinish && isFloatPermission) {
            showFloatWindow()
        } else {
            AgoraManager.isLiveRoom = false
            AgoraManager.isVideoCall = false
        }
    }

    override fun exactDestroy() {
        super.exactDestroy()
        LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESHCHATUSER).post(true)
        logcom("onDestroy：通话结束")
        AgoraManager.isVideoCall = false
        AgoraManager.isLiveRoom = false
        if (AgoraPhoneManager.getInstance().isInit) {
            AgoraPhoneManager.getInstance().onDestory()
        }
        if (floatView != null) {
            floatView!!.destory()
        }
        closeFloatWidow()
        if (callTimer != null) {
            callTimer!!.cancel()
            callTimer = null
        }
        handler.removeCallbacksAndMessages(null)
    }
}