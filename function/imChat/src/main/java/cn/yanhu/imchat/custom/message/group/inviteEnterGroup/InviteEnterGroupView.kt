package cn.yanhu.imchat.custom.message.group.inviteEnterGroup

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.yanhu.imchat.R
import cn.yanhu.imchat.custom.message.BaseEaseChatRow
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.imchat.bean.GroupInviteRecord
import com.blankj.utilcode.util.GsonUtils
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.utils.EaseUserUtils
import com.jeremyliao.liveeventbus.LiveEventBus

@SuppressLint("ViewConstructor")
class InviteEnterGroupView(context: Context, isSender: Boolean) : BaseEaseChatRow(context, isSender) {
    private var applyFriendLl: LinearLayout? = null
    private var icon: ImageView? = null
    private var easeAgree: TextView? = null
    private var hintView: TextView? = null
    private var vgContent: ViewGroup? = null
    override fun onInflateView() {
        inflater.inflate(R.layout.ease_invite_enter_group_layout, this)
    }

    override fun onFindViewById() {
        icon = findViewById(R.id.alert_icon)
        hintView = findViewById(R.id.tv_alert)
        easeAgree = findViewById(R.id.ease_agree)
        applyFriendLl = findViewById(R.id.apply_friend_ll)
        vgContent = findViewById(R.id.vg_content)
    }

    @SuppressLint("SetTextI18n")
    override fun onSetUpView() {
        super.onSetUpView()
        try {
            val msg = message.getStringAttribute(ChatConstant.CUSTOM_MSG, "")
            val recordInfo = GsonUtils.fromJson(msg, GroupInviteRecord::class.java)

            if (recordInfo.isJoinSuccess) {
                if (message.direct() == EMMessage.Direct.SEND){
                    hintView?.text = "您已加入群聊:${recordInfo.groupName}"
                    easeAgree?.text = "进入"
                    easeAgree?.setOnClickListener {
                       // GroupCommonManager.saveUserJoinGroupChatRecord(recordInfo.groupId,GroupCommonManager.SOURCE_GROUP_USER)
                    }
                }else{
                    val user = EaseUserUtils.getUserInfo(message.from)
                    val nickName = if (user!=null){
                        user.nickname
                    }else{
                        message.from
                    }
                    easeAgree?.visibility = View.GONE
                    hintView?.text = "${nickName}已加入群聊:${recordInfo.groupName}"
                    vgContent?.setBackgroundResource(cn.yanhu.commonres.R.drawable.shape_transparent)
                }
            } else {
                if (message.direct() == EMMessage.Direct.SEND) {
                    easeAgree?.visibility = View.GONE
                    hintView?.text = "您已邀请对方加入群聊，请等待对方同意"
                    vgContent?.setBackgroundResource(cn.yanhu.commonres.R.drawable.shape_transparent)
                } else {
                    easeAgree?.visibility = View.VISIBLE
                    hintView?.text = recordInfo.content
                    vgContent?.setBackgroundResource(R.drawable.shape_item_un_select)
                    easeAgree?.text = "同意"
                    easeAgree?.setOnClickListener {
                        LiveEventBus.get<EMMessage>(EventBusKeyConfig.AGREE_GROUP_INVITE).post(message)
                    }
                }
            }
        } catch (e: Exception) {
        }
    }
}