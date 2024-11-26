package cn.yanhu.agora.ui.imphone

import android.media.MediaPlayer
import android.text.TextUtils
import android.view.View
import cn.yanhu.agora.databinding.ActivityToWaitPhoneBinding
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.agora.R
import cn.yanhu.agora.manager.dbCache.AgoraSdkCacheManager
import cn.yanhu.agora.manager.ImPhoneMsgManager
import cn.yanhu.agora.miniwindow.LiveRoomVideoMiniManager
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.StatusBarUtil
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.router.RouterPath
import cn.yanhu.commonres.bean.ChatCallResponseInfo
import cn.yanhu.imchat.manager.ChatCallStatusConfig
import cn.yanhu.imchat.manager.CutLiveRoomUtils
import cn.yanhu.imchat.manager.EmMsgManager
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.status.ErrorCode
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.gson.Gson
import com.hyphenate.chat.EMMessage
import com.jeremyliao.liveeventbus.LiveEventBus

/**
 * @author: zhengjun
 * created: 2024/10/14
 * desc:
 */
@Route(path = RouterPath.ROUTER_TO_WAITPHONE)
class ToWaitPhoneActivity : BaseActivity<ActivityToWaitPhoneBinding, ImPhoneViewModel>(
    R.layout.activity_to_wait_phone,
    ImPhoneViewModel::class.java
) {
    private lateinit var callInfo: ChatCallResponseInfo
    private var chatType = 0 //0语音
    override fun initData() {
        setFullScreenStatusBar(true)
        val statusBarHeight = StatusBarUtil.getStatusBarHeight(mContext)
        ViewUtils.setMarginTop(mBinding.titleBar, statusBarHeight)
        playRing()
        val content = intent.extras?.getString(IntentKeyConfig.DATA)
        if (TextUtils.isEmpty(content)){
            finish()
            return
        }
        callInfo = Gson().fromJson(
            content, ChatCallResponseInfo::class.java
        )
        chatType = callInfo.chatType
        mBinding.callInfo = callInfo
        bgBlur()
    }

    /*
 * 背景模糊
 * */
    private fun bgBlur() {
        if (callInfo.chatType == 0) {
            return
        } else {
            mBinding.callWaitBg.visibility = View.VISIBLE
        }
        val videoSelfIntroduction = callInfo.videoSelfIntroduction
        if (TextUtils.isEmpty(videoSelfIntroduction)) {
            GlideUtils.loadBlurTransPic(mContext,callInfo.user.portrait, imageView = mBinding.callWaitBg)
        } else {
            //playVideoView(videoSelfIntroduction)
        }
    }


    override fun initListener() {
        super.initListener()
        mBinding.waitPhoneAnswer.setOnSingleClickListener {
            if (AgoraSdkCacheManager.hasLoadAgoraSdk()) {
                if (LiveRoomVideoMiniManager.getInstance().isShowing) {
                    CutLiveRoomUtils.showChangeAlert(
                        object : CutLiveRoomUtils.ChangeListener {
                            override fun sure() {
                                callUp()
                            }
                        },
                        if (chatType == 0) "你确定要离开当前房间并进入语音通话吗？" else "你确定要离开当前房间并进入视频通话吗？"
                    )
                } else {
                    callUp()
                }
            } else {
                LiveEventBus.get<Boolean>(EventBusKeyConfig.SHOW_AGORA_SDK_DOWNLOAD_PROGRESS)
                    .post(true)
            }
        }
        mBinding.titleBar.setTitleButtonOnClickListener(object : TitleBar.TitleButtonOnClickListener{
            override fun leftButtonOnClick(v: View?) {
                refuseCall()
            }

            override fun rightButtonOnClick(v: View?) {
            }

        })
        mBinding.waitPhoneRefuse.setOnSingleClickListener {
            refuseCall()
        }
        LiveEventBus.get<EMMessage>(EventBusKeyConfig.RECEIVE_CMD_MSG).observe(this){
            val source = it.getIntAttribute("source", -1)
            if (source == ChatConstant.ACTION_PHONE_CALL_REFUSE) { //一对一通话取消
                finish()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        refuseCall()
    }

    /*
   * 接通一对一通话
   * */
    private var isClickAgree = false
    fun callUp() {
        if (isClickAgree) {
            return
        }
        isClickAgree = true
        mViewModel.startCall(
            callInfo.user.id.toString(),
            callInfo.chatType.toString(),
            ChatCallStatusConfig.STATUS_COMMIT,
            callInfo.uid
        )

    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.chatCallObserver.observe(this) { it ->
            parseState(it, {
                EmMsgManager.sendCmdMessagePeople(
                    callInfo.user.id.toString(),
                    ChatConstant.ACTION_PHONE_CALL_ANSWER,
                    null,
                )
                RouteIntent.toVideoPhonePage(it)
                isClickAgree = false
                finish()
            }, {
                isClickAgree = false
                if (ErrorCode.CODE_NO_BALANCE == it.code) {
                    ApplicationProxy.instance.showRechargePop(mContext, true)
                }else if (it.code == ErrorCode.CODE_NEED_REAL_NAME){
                    RouteIntent.lunchToRealNamPage()
                } else {
                    refuseCall()
                }
            })
        }
    }

     fun refuseCall() {
        requestCall()
        EmMsgManager.sendCmdMessagePeople(
            callInfo.user.id.toString(),
            ChatConstant.ACTION_PHONE_CALL_REFUSE,
            null,
        )
        ImPhoneMsgManager.sendRefuseMsg(callInfo)
        finish()
    }

    /*
      * 拒绝接通一对一通话
      * */
    private fun requestCall() {
        callInfo.apply {
            mViewModel.call(
                this.user.id.toString(),
                this.chatType.toString(),
                ChatCallStatusConfig.STATUS_REFUSE,
                this.uid
            )
        }

    }

    private var mediaPlayerRing: MediaPlayer? = null

    private fun playRing() {
        mediaPlayerRing = MediaPlayer.create(mContext, cn.yanhu.commonres.R.raw.papa_rings)
        mediaPlayerRing!!.isLooping = true // 设置循环播放
        mediaPlayerRing!!.start() // 开始播放
    }

    override fun exactDestroy() {
        super.exactDestroy()
        if (mediaPlayerRing != null) {
            mediaPlayerRing!!.stop()
            mediaPlayerRing!!.release()
            mediaPlayerRing = null
        }
    }
}