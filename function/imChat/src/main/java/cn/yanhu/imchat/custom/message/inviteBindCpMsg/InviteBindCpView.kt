package cn.yanhu.imchat.custom.message.inviteBindCpMsg

import android.annotation.SuppressLint
import android.content.Context
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.ImMessageParamsConfig
import cn.yanhu.commonres.manager.AppCacheManager.userId
import cn.yanhu.imchat.R
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.bean.request.BindCpRequest
import cn.yanhu.imchat.custom.message.BaseEaseChatRow
import cn.yanhu.imchat.databinding.EaseInviteBindCpLayoutBinding
import cn.yanhu.imchat.manager.EmMsgManager
import cn.yanhu.imchat.manager.ImUserManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ThreadUtils
import com.hyphenate.chat.EMCustomMessageBody
import com.jeremyliao.liveeventbus.LiveEventBus

@SuppressLint("ViewConstructor")
class InviteBindCpView(context: Context?, isSender: Boolean) : BaseEaseChatRow(context, isSender) {
    private var binding: EaseInviteBindCpLayoutBinding? = null


    override fun onInflateView() {
        val inflate = inflater.inflate(R.layout.ease_invite_bind_cp_layout, this)
        inflate.tag = "layout/ease_invite_bind_cp_layout_0"
        binding = EaseInviteBindCpLayoutBinding.bind(getRootView())
    }

    override fun onFindViewById() {
    }

    override fun onSetUpView() {
        super.onSetUpView()
        try {
            val messageBody = message.getBody() as EMCustomMessageBody
            val params = messageBody.params
            binding?.apply {
                //0 未绑定 1已同意 2已拒绝
                val bindStatus = message.getIntAttribute(ImMessageParamsConfig.KEY_STATUS, 0)

                val content: String?
                if (message.from == userId) {
                    //发送方
                    vgOperate.visibility = GONE
                    ImMessageParamsConfig
                    content = "已发送心动CP绑定邀请，请等待对方同意～"
                } else {
                    val name = params.getOrDefault(ImMessageParamsConfig.USERNAME, "")

                    content = "心动预警！${name}向您发送了心动CP邀请！\n请在24小时内处理，过期自动失效"

                    if (bindStatus == 0) {
                        tvCommit.visibility = VISIBLE
                        tvRefuse.text = "拒绝"
                        tvRefuse.isEnabled = true
                        tvCommit.setOnSingleClickListener {
                            val activityId =
                                params.getOrDefault(ImMessageParamsConfig.ACTIVITY_ID, "")
                            val bindRequest = BindCpRequest(activityId, message.from)
                            request({ imChatRxApi.confirmBindCp(bindRequest) },
                                object : OnRequestResultListener<BaseUserInfo> {
                                    override fun onSuccess(data: BaseBean<BaseUserInfo>) {
                                        showToast("绑定成功")
                                        message.setAttribute(ImMessageParamsConfig.KEY_STATUS, 1)
                                        EmMsgManager.updateMsg(message)

                                        showBindSuccess()
                                        sendBindSuccessMsg(name)

                                    }
                                })
                        }
                        tvRefuse.setOnSingleClickListener {
                            val activityId =
                                params.getOrDefault(ImMessageParamsConfig.ACTIVITY_ID, "")
                            val bindRequest = BindCpRequest(activityId, message.from)
                            request({ imChatRxApi.rejectBindCp(bindRequest) },
                                object : OnRequestResultListener<String> {
                                    override fun onSuccess(data: BaseBean<String>) {
                                        showToast("已拒绝")
                                        message.setAttribute(ImMessageParamsConfig.KEY_STATUS, 2)
                                        EmMsgManager.updateMsg(message)
                                        showRefuseBind()
                                    }
                                })
                        }
                    } else if (bindStatus == 1) {
                        showBindSuccess()
                    } else {
                        showRefuseBind()
                    }
                    vgOperate.visibility = VISIBLE

                }

                tvAlert.text = content
                executePendingBindings()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendBindSuccessMsg(name: String?) {
        val params = HashMap<String, String>()
        params[ImMessageParamsConfig.SENDSHOWCONTENT] =
            "您和${name}已绑定心动CP～"
        params[ImMessageParamsConfig.RECEIVESHOWCONTENT] =
            "您和${ImUserManager.getSelfUserInfo().nickName}已绑定心动CP～"
        EmMsgManager.sendCommonTipMsg(message.from, params)
        ThreadUtils.getMainHandler().postDelayed({
            LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_CHAT_LIST).post(true)
        },300)

    }

    private fun EaseInviteBindCpLayoutBinding.showRefuseBind() {
        tvRefuse.isEnabled = false
        tvRefuse.text = "已拒绝"
        tvCommit.visibility = GONE
    }

    private fun EaseInviteBindCpLayoutBinding.showBindSuccess() {
        tvRefuse.isEnabled = false
        tvRefuse.text = "已同意"
        tvCommit.visibility = GONE
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }
}