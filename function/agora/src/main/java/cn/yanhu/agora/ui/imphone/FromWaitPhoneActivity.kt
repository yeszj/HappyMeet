package cn.yanhu.agora.ui.imphone

import android.media.MediaPlayer
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.View
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.router.RouterPath
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.ActivityFromWaitPhoneBinding
import cn.yanhu.agora.manager.AgoraPhoneManager
import cn.yanhu.agora.manager.ImPhoneMsgManager
import cn.yanhu.commonres.bean.ChatCallResponseInfo
import cn.yanhu.imchat.manager.ChatCallStatusConfig
import cn.yanhu.imchat.manager.EmMsgManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.GsonUtils
import com.hyphenate.chat.EMMessage
import com.jeremyliao.liveeventbus.LiveEventBus

@Route(path = RouterPath.ROUTER_FROM_WAITPHONE)
class FromWaitPhoneActivity : BaseActivity<ActivityFromWaitPhoneBinding, ImPhoneViewModel>(
    R.layout.activity_from_wait_phone,
    ImPhoneViewModel::class.java
) {
    private lateinit var callInfo: ChatCallResponseInfo
    override fun initData() {
        setFullScreenStatusBar(false)
        playRing()
        val content = intent.extras?.getString(IntentKeyConfig.DATA)
        if (TextUtils.isEmpty(content)) {
            finish()
            return
        }
        callInfo = GsonUtils.fromJson(
            content,
            ChatCallResponseInfo::class.java
        )
        mBinding.callInfo = callInfo
        mBinding.executePendingBindings()
        startConnectCountDownTime()
        AgoraPhoneManager.getInstance()
            .init(mContext, true)
        AgoraPhoneManager.getInstance().setupLocalVideo(mBinding.surfaceView)
    }

    private var timer: CountDownTimer? = null
    private fun startConnectCountDownTime() {
        timer = object : CountDownTimer(60000, 60000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                showToast("对方无响应")
                cancelCall(true)
            }
        }
        timer?.start()
    }


    /*
     * 取消一对一通话
     * */
    fun cancelCall(isTimeOut: Boolean = false) {
        val userId: String = java.lang.String.valueOf(callInfo.chatUser.id)
        mViewModel.call(
            userId,
            java.lang.String.valueOf(callInfo.chatType),
            ChatCallStatusConfig.STATUS_CANCEL,
            callInfo.uid
        )
        EmMsgManager.sendCmdMessagePeople(
            userId,
            AppCacheManager.userId,
            ChatConstant.ACTION_PHONE_CALL_REFUSE
        )
        if (isTimeOut) {
            ImPhoneMsgManager.sendTimeOutMsg(callInfo.chatType, java.lang.String.valueOf(userId))
        } else {
            ImPhoneMsgManager.sendCancelMsg(
                callInfo.chatType,
                java.lang.String.valueOf(userId)
            )
        }
        finish()
    }

    override fun initListener() {
        super.initListener()
        mBinding.titleBar.setTitleButtonOnClickListener(object :
            TitleBar.TitleButtonOnClickListener {
            override fun leftButtonOnClick(v: View?) {
                cancelCall()
            }

            override fun rightButtonOnClick(v: View?) {
            }

        })
        mBinding.ivHangup.setOnSingleClickListener {
            cancelCall()
        }
        LiveEventBus.get<EMMessage>(EventBusKeyConfig.RECEIVE_CMD_MSG).observe(this) {
            val source = it.getIntAttribute("source", -1)
            if (source == ChatConstant.ACTION_PHONE_CALL_REFUSE) { //一对一通话取消
                finish()
            } else if (source == ChatConstant.ACTION_PHONE_CALL_ANSWER) { //一对一通话同意
                isAgree = true
                RouteIntent.toVideoPhonePage(callInfo)
                finish()
            }
        }
    }

    private var isAgree = false;

    override fun onBackPressed() {
        super.onBackPressed()
        cancelCall()
    }

    /*
     * 播放铃声
     * */
    private var mediaPlayerRing: MediaPlayer? = null

    private fun playRing() {
        mediaPlayerRing = MediaPlayer.create(mContext, cn.yanhu.commonres.R.raw.papa_rings)
        mediaPlayerRing!!.isLooping = true // 设置循环播放
        mediaPlayerRing!!.start() // 开始播放
    }

    override fun exactDestroy() {
        super.exactDestroy()
        if (!isAgree){
            AgoraPhoneManager.getInstance().onDestory()
        }
        mediaPlayerRing?.stop()
        mediaPlayerRing?.release()
        mediaPlayerRing = null
        timer?.apply {
            this.cancel()
            timer = null
        }
    }
}