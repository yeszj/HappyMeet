package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import cn.yanhu.agora.R
import cn.yanhu.agora.manager.AppAudioManager
import cn.gxgre.forlove.manager.ImageLoadManager
import cn.yanhu.agora.databinding.PopReceiveImCallBgBinding
import cn.yanhu.agora.manager.ImPhoneMsgManager
import cn.yanhu.agora.miniwindow.EaseCallFloatWindow
import cn.yanhu.agora.ui.imphone.FromWaitPhoneActivity
import cn.yanhu.agora.ui.imphone.ImPhoneViewModel
import cn.yanhu.agora.ui.imphone.ToWaitPhoneActivity
import cn.yanhu.agora.ui.imphone.VideoPhoneActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.ChatCallResponseInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.imchat.manager.EmMsgManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ThreadUtils
import com.google.gson.Gson
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.animator.PopupAnimator
import com.lxj.xpopup.animator.TranslateAnimator
import com.lxj.xpopup.core.PositionPopupView
import com.lxj.xpopup.enums.PopupAnimation

/**
 * @author: zhengjun
 * created: 2024/8/14
 * desc:
 */
@SuppressLint("ViewConstructor")
class ReceiveImCallInBgPop(
    context: Context,
    val callInfo: String
) : PositionPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_receive_im_call_bg
    }

    private lateinit var imPhoneViewModel: ImPhoneViewModel

    lateinit var chatCallInfo: ChatCallResponseInfo
    private lateinit var mBinding: PopReceiveImCallBgBinding
    override fun onCreate() {
        super.onCreate()
        imPhoneViewModel = ImPhoneViewModel()
        mBinding = PopReceiveImCallBgBinding.bind(popupImplView)
        chatCallInfo = Gson().fromJson(
            callInfo, ChatCallResponseInfo::class.java
        )
        cn.yanhu.baselib.utils.ViewUtils.setMarginTop(
            mBinding.vgParent,
            CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_40)
        )
        mBinding.callInfo = chatCallInfo
        ImageLoadManager.loadImage(mBinding.ivAvatar, chatCallInfo.user.portrait)
        mBinding.tvRefuse.setOnSingleClickListener {
            refuseCall()
        }
        mBinding.vgAgree.setOnSingleClickListener {
            agreeCall()
        }
        AppAudioManager.playRing(context)
    }

    override fun beforeDismiss() {
        super.beforeDismiss()
        AppAudioManager.clearMediaPlay()
    }

    @SuppressLint("CheckResult")
    private fun agreeCall() {
        if (isClickAgree) {
            return
        }
        isClickAgree = true
        val hashMap = HashMap<String, Any>()
        hashMap["chatUserId"] = chatCallInfo.user.id.toString()
        hashMap["chatType"] = chatCallInfo.chatType.toString()
        hashMap["status"] = "3"
        hashMap["uid"] = chatCallInfo.uid

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
                    refuseCall()
                }

            }
        )
    }

    override fun getPopupAnimator(): PopupAnimator {
        return TranslateAnimator(
            popupContentView,
            animationDuration,
            PopupAnimation.TranslateFromTop
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
                else -> {
                    toNewCallPage()
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
            context: Context,
            callInfo: String
        ): ReceiveImCallInBgPop {
            val remarkNameTipsDialog = ReceiveImCallInBgPop(context, callInfo)
            val builder = XPopup.Builder(context)
            builder
                .maxWidth(ScreenUtils.getScreenWidth())
                .enableShowWhenAppBackground(true)
                .enableDrag(false)
                .dismissOnBackPressed(false)
                .dismissOnTouchOutside(false)
                .isDestroyOnDismiss(true).asCustom(remarkNameTipsDialog).show()
            return remarkNameTipsDialog
        }
    }

}