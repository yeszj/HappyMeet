package cn.yanhu.imchat.custom.message.chatSystemMsg

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.widget.TextView
import cn.yanhu.baselib.utils.HtmlClickProcessor
import cn.yanhu.commonres.bean.CommonSystemMsgInfo
import cn.yanhu.commonres.bean.CommonSystemMsgInfo.SystemMsgInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.router.PageIntentUtil.url2Page
import cn.yanhu.imchat.custom.message.BaseEaseChatRow
import com.blankj.utilcode.util.GsonUtils
import java.util.Objects
import kotlin.jvm.java
import cn.yanhu.imchat.R

@SuppressLint("ViewConstructor")
class ChatSystemMsgView(context: Context?, isSender: Boolean) : BaseEaseChatRow(context, isSender) {
    private var tvTitle: TextView? = null
    private var hintView: TextView? = null

    override fun onInflateView() {
        inflater.inflate(R.layout.ease_common_system_msg_layout, this)
    }

    override fun onFindViewById() {
        hintView = findViewById<TextView>(R.id.tv_alert)
        tvTitle = findViewById<TextView>(R.id.tv_title)
    }

    @SuppressLint("SetTextI18n")
    override fun onSetUpView() {
        try {
            val stringAttribute = message.getStringAttribute(ChatConstant.CUSTOM_DATA, "")
            val commonSystemMsgInfo = GsonUtils.fromJson<CommonSystemMsgInfo>(
                stringAttribute,
                CommonSystemMsgInfo::class.java
            )
            val showInfo = getSystemMsgInfo(commonSystemMsgInfo)
            val title = showInfo.title
            if (TextUtils.isEmpty(title)) {
                tvTitle!!.visibility = GONE
            } else {
                tvTitle!!.visibility = VISIBLE
                tvTitle!!.text = title
            }

            var url = showInfo.url

            HtmlClickProcessor.setHtmlTextWithClick (hintView!!, showInfo.content ,(if (TextUtils.isEmpty(showInfo.clickContent)) "举报" else showInfo.clickContent)) {
                url2Page(context, url)
            }
        } catch (e: Exception) {
        }
    }

    private fun getSystemMsgInfo(commonSystemMsgInfo: CommonSystemMsgInfo): SystemMsgInfo {
        val sendShowInfo = commonSystemMsgInfo.sendShowInfo
        val receiveShowInfo = commonSystemMsgInfo.receiveShowInfo
        val showInfo: SystemMsgInfo
        if (isSender()) {
            showInfo = sendShowInfo
        } else {
            showInfo = Objects.requireNonNullElse<SystemMsgInfo>(receiveShowInfo, sendShowInfo)
        }
        return showInfo
    }
}
