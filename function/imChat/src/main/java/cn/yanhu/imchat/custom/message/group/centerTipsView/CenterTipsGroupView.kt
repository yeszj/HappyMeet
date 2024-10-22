package cn.yanhu.imchat.custom.message.group.centerTipsView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.imchat.R
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.imchat.bean.GroupCenterTipMsgInfo
import com.blankj.utilcode.util.GsonUtils
import com.bumptech.glide.Glide
import com.hyphenate.easeui.widget.chatrow.EaseChatRow

@Suppress("DEPRECATION")
@SuppressLint("ViewConstructor")
class CenterTipsGroupView(context: Context, isSender: Boolean) :
    EaseChatRow(context, isSender) {
    private var ivTipIcon: ImageView? = null
    private var tvCenterTip:TextView? = null
    override fun onInflateView() {
        inflater.inflate(
            R.layout.ease_group_center_tips_msg_view,
            this
        )
    }

    override fun onFindViewById() {
        ivTipIcon = findViewById(R.id.iv_tipIcon)
        tvCenterTip = findViewById(R.id.tv_centetTip)
    }

    @SuppressLint("SetTextI18n", "CheckResult")
    override fun onSetUpView() {
        try {
            val msg = message.getStringAttribute(ChatConstant.CUSTOM_MSG, "")
            val recordInfo = GsonUtils.fromJson(msg, GroupCenterTipMsgInfo::class.java)

            val stringAttribute: String =
                message.getStringAttribute(ChatConstant.CUSTOM_SEND_USER_INFO, "")
            val userInfo = GsonUtils.fromJson(
                stringAttribute,
                BaseUserInfo::class.java
            )
            val content = recordInfo.content
            when(recordInfo.type){
                GroupCenterTipMsgInfo.TYPE_RECEIVE_RED_PACKET->{
                    if (isSender()){
                        val build = Spans.builder().text("你").color(Color.parseColor("#EA4528"))
                            .text("领取了${content}的红包").build()
                        tvCenterTip?.text = build
                    }else{
                        val build = Spans.builder().text(userInfo.nickName).color(Color.parseColor("#EA4528"))
                            .text("领取了你的红包").build()
                        tvCenterTip?.text = build
                    }
                }
                else->{
                    val charSequence: CharSequence =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY)
                    } else {
                        Html.fromHtml(content)
                    }
                    tvCenterTip?.text = charSequence
                }
            }

            val iconUrl = recordInfo.iconUrl
            if (TextUtils.isEmpty(iconUrl)){
                ivTipIcon?.visibility = View.GONE
            }else{
                ivTipIcon?.apply {
                    this.visibility = View.VISIBLE
                    Glide.with(context).load(recordInfo.iconUrl).dontAnimate()
                        .into(this)
                }
            }

        } catch (e: Exception) {
        }
    }
}