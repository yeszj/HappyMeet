package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.PopReceiveImCallBinding
import cn.yanhu.agora.manager.ImPhoneMsgManager
import cn.yanhu.agora.miniwindow.EaseCallFloatWindow
import cn.yanhu.agora.ui.imphone.FromWaitPhoneActivity
import cn.yanhu.agora.ui.imphone.ImPhoneViewModel
import cn.yanhu.agora.ui.imphone.ToWaitPhoneActivity
import cn.yanhu.agora.ui.imphone.VideoPhoneActivity
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.ChatCallResponseInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.imchat.config.RechargeSourceConfig
import cn.yanhu.imchat.manager.EmMsgManager
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ErrorCode
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ThreadUtils
import com.google.gson.Gson
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView

/**
 * @author: zhengjun
 * created: 2024/8/14
 * desc:
 */
@SuppressLint("ViewConstructor")
class ReceiveImCallPop(val context: FragmentActivity, val callInfo: String) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_receive_im_call
    }
    private lateinit var imPhoneViewModel: ImPhoneViewModel

    lateinit var chatCallInfo: ChatCallResponseInfo
    private lateinit var mBinding: PopReceiveImCallBinding
    override fun onCreate() {
        super.onCreate()
        imPhoneViewModel = ImPhoneViewModel()
        mBinding = PopReceiveImCallBinding.bind(popupImplView)
        chatCallInfo = Gson().fromJson(
            callInfo, ChatCallResponseInfo::class.java
        )
        mBinding.callInfo = chatCallInfo
        mBinding.tvRefuse.setOnSingleClickListener {
            refuseCall()
        }
        mBinding.vgAgree.setOnSingleClickListener {
            agreeCall()
        }
        LiveEventBus.get<Boolean>(LiveDataEventManager.PAY_RESULT).observe(context) {
            if (it) {
                ThreadUtils.getMainHandler().postDelayed({ toNewCallPage() }, 500)
            }
        }
    }

    private fun agreeCall() {
        if (isClickAgree) {
            return
        }
        DialogUtils.showLoading()
        isClickAgree = true
        imPhoneViewModel.call(
            chatCallInfo.user.id.toString(),
            chatCallInfo.chatType.toString(),
            "3",
            chatCallInfo.uid, object : OnRequestResultListener<ChatCallResponseInfo> {

                override fun onSuccess(data: BaseBean<ChatCallResponseInfo>) {
                    finishOldCall()
                    isClickAgree = false
                    dismiss()
                }
                override fun onFail(code: Int?, msg: String?) {
                    isClickAgree = false
                    showToast(msg)
                    if (ErrorCode.CODE_NO_BALANCE ==code) {
                        showRechargeListDialog(if (chatCallInfo.chatType == 0) RechargeSourceConfig.SOURCE_CALL_VIDEO_AGREE_BALANCE else RechargeSourceConfig.SOURCE_CALL_VOICE_AGREE_BALANCE)
                    } else {
                        refuseCall()
                    }
                }

            }
        )
    }


    private fun finishOldCall() {
        val topActivity = ActivityUtils.getTopActivity()
        if (EaseCallFloatWindow.getInstance().isShowing) {
            val activityList = ActivityUtils.getActivityList()
            activityList.forEach {
                if (it is VideoPhoneActivity) {
                    finishVideoCall(it)
                }
            }
        } else {
            when (topActivity) {
                is VideoPhoneActivity -> {
                    finishVideoCall(topActivity)
                }

                is FromWaitPhoneActivity -> {
                    cancelCall(topActivity)
                }

                is ToWaitPhoneActivity -> {
                    refuseCall(topActivity)
                }
            }
        }
    }

    private fun refuseCall(topActivity: ToWaitPhoneActivity) {
        topActivity.refuseCall()
        toNewCallPage()
    }

    private fun cancelCall(topActivity: FromWaitPhoneActivity) {
        topActivity.cancelCall()
        toNewCallPage()
    }


    private fun finishVideoCall(topActivity: VideoPhoneActivity) {
        topActivity.leave(object : VideoPhoneActivity.OnLeaveListener {
            override fun onLeave() {
                toNewCallPage()
            }
        })
    }


    /*
   * 接通一对一通话
   * */
    private var isClickAgree = false
    fun toNewCallPage() {
        ThreadUtils.getMainHandler().postDelayed({
            DialogUtils.dismissLoading()
            EmMsgManager.sendCmdMessagePeople(
                chatCallInfo.user.id.toString(),
                ChatConstant.ACTION_PHONE_CALL_ANSWER,
                null,
            )
            RouteIntent.toVideoPhonePage(chatCallInfo)
        }, 500)


    }

    private fun showRechargeListDialog(rechargeSource: Int) {
        ApplicationProxy.instance.showRechargePop(context,true)
    }


    private fun requestCall() {
        chatCallInfo.apply {
            imPhoneViewModel.call(
                this.user.id.toString(),
                this.chatType.toString(),
                "2",
                this.uid
            )
        }

    }

    private fun refuseCall() {
        requestCall()
        EmMsgManager.sendCmdMessagePeople(
            chatCallInfo.user.id.toString(),
            ChatConstant.ACTION_PHONE_CALL_REFUSE,
            null,
        )
        ImPhoneMsgManager.sendRefuseMsg(chatCallInfo)
        dismiss()
    }

    companion object {
        @JvmStatic
        fun showDialog(
            context: FragmentActivity,
            callInfo: String
        ): ReceiveImCallPop {
            val remarkNameTipsDialog = ReceiveImCallPop(context, callInfo)
            val builder = XPopup.Builder(context)
            builder
                .maxWidth(ScreenUtils.getScreenWidth())
                .isDestroyOnDismiss(true).asCustom(remarkNameTipsDialog).show()
            return remarkNameTipsDialog
        }
    }

}