package cn.yanhu.imchat.custom.message

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.FragmentActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.countDown
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.ImMessageParamsConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.utils.SVGAUtils
import cn.yanhu.imchat.R
import cn.yanhu.imchat.bean.GroupUserInfo
import cn.yanhu.imchat.db.ChatUserInfoManager
import cn.yanhu.imchat.db.GroupUserInfoCacheManager
import cn.yanhu.imchat.manager.ImUserManager
import cn.yanhu.imchat.view.ImChatUserInfoView
import com.blankj.utilcode.util.GsonUtils
import com.bumptech.glide.Glide
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.chat.EMGroup
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.utils.EaseUserUtils
import com.hyphenate.easeui.widget.chatrow.EaseChatRow
import com.jeremyliao.liveeventbus.LiveEventBus
import com.opensource.svgaplayer.SVGAImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

/**
 * @author: zhengjun
 * created: 2024/1/19
 * desc:
 */
@SuppressLint("ViewConstructor")
open class BaseEaseChatRow : EaseChatRow {
    constructor(context: Context?, isSender: Boolean) : super(context, isSender)
    constructor(context: Context?, message: EMMessage?, position: Int, adapter: Any?) : super(
        context, message, position, adapter
    )

    private var ivAvatarFrame: SVGAImageView? = null
    private var vgAvatar: ViewGroup? = null
    private var userInfoView: ImChatUserInfoView? = null
    private var tvIdentity: TextView? = null
    override fun onInflateView() {}
    override fun onFindViewById() {
        ivAvatarFrame = findViewById(R.id.iv_avatarFrame)
        vgAvatar = findViewById(R.id.vg_avatar)
        userInfoView = findViewById(R.id.userInfoView)
        tvIdentity = findViewById(R.id.tv_identity)
    }

    override fun onMessageSuccess() {
        super.onMessageSuccess()
        //LiveEventBus.get<Boolean>(EventBusKeyConfig.SEND_MESSAGE_SUCCESS).post(true)
    }

    override fun onSetUpView() {

        var sendUserInfo = GroupUserInfoCacheManager.getGroupUserInfoByUserId(message.from)
        if (sendUserInfo==null){
            val stringAttribute: String =
                message.getStringAttribute(ChatConstant.CUSTOM_SEND_USER_INFO, "")
             sendUserInfo = GsonUtils.fromJson(
                stringAttribute, GroupUserInfo::class.java
            )
        }
        if (message.chatType == EMMessage.ChatType.GroupChat) {
            ackedView?.visibility = INVISIBLE
            bindIdentityView(message)
            if (message.direct() == EMMessage.Direct.SEND) {
                val userInfo = ImUserManager.getSelfUserInfo()
                if (userInfo != null) {
                    bindMyUserInfo(userInfo)
                } else {
                    bindSendUserInfo(sendUserInfo)
                }
            } else {
                bindSendUserInfo(sendUserInfo)
            }

            ViewUtils.setViewSize(
                vgAvatar,
                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_50),
                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_50)
            )
        } else {
            if (message.direct() == EMMessage.Direct.SEND) {
                val userIcon = ImUserManager.getSelfUserInfo()?.portrait
                if (TextUtils.isEmpty(userIcon)) {
                    if (sendUserInfo != null && !TextUtils.isEmpty(sendUserInfo.portrait)) {
                        if (userAvatarView != null){
                            GlideUtils.loadImage(context,sendUserInfo.portrait,userAvatarView)
                        }
                    }
                } else {
                    if (userAvatarView != null) {
                        GlideUtils.loadImage(context,userIcon,userAvatarView)
                    }
                }

            } else {
                val containsKey =  ChatUserInfoManager.getUserInfo(message.from)
                if (containsKey == null && !TextUtils.isEmpty(sendUserInfo?.portrait)) {
                    if (userAvatarView != null){
                        GlideUtils.loadImage(context,sendUserInfo!!.portrait,userAvatarView)
                    }
                }
            }
            bindAckStatus()
        }
        if (!showSenderType || (isLoveMsg(message) && isSender)) {
            setCoinReward()
        } else {
            val coinLl: ViewGroup? = findViewById(R.id.coin_ll)
            coinLl?.visibility = View.GONE
        }
        setShowChatCardTip()
    }

    private fun setShowChatCardTip() {
        val vgChatCard: ViewGroup? = findViewById(R.id.vg_chatCard)
        val ivCard: AppCompatImageView? = findViewById(R.id.iv_card)
        val tvCardTips: TextView? = findViewById(R.id.tv_cardTips)

        if (vgChatCard != null) {
            if (message.direct() == EMMessage.Direct.SEND){
                val isUserChatCard =
                    message.getBooleanAttribute(ImMessageParamsConfig.KEY_IS_USER_CHAT_CARD, false)
                if (isUserChatCard) {
                    vgChatCard.visibility = VISIBLE
                    tvCardTips?.text = "使用畅聊卡"
                    ivCard?.setImageResource(cn.yanhu.commonres.R.drawable.icon_chat_card_im)
                } else {
                    bindDarlingTips(vgChatCard, ivCard, tvCardTips)
                }
            }else{
                bindDarlingTips(vgChatCard, ivCard, tvCardTips)
            }

        }
    }

    private fun bindDarlingTips(
        vgChatCard: ViewGroup,
        ivCard: AppCompatImageView?,
        tvCardTips: TextView?
    ) {
        val darlingFreeCount =
            message.getIntAttribute(ImMessageParamsConfig.KEY_DARLING_FREE_COUNT, 0)
        if (darlingFreeCount > 0) {
            vgChatCard.visibility = VISIBLE
            ivCard?.setImageResource(cn.yanhu.commonres.R.drawable.icon_darling)
            tvCardTips?.text = "心肝免费消息"
            val coinLl: ViewGroup? = findViewById(R.id.coin_ll)
            coinLl?.visibility = View.GONE
        } else {
            vgChatCard.visibility = GONE
        }
    }

    private fun bindMyUserInfo(userInfo: UserDetailInfo) {
        GlideUtils.loadImage(context, userInfo.portrait,userAvatarView)
        if (ivAvatarFrame != null) {
            val nobleAvatarFrame = userInfo.avatarFrame
            val dimension: Int
            if (!TextUtils.isEmpty(nobleAvatarFrame)) {
                dimension = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_35)
                if (nobleAvatarFrame.endsWith(".svga")) {
                    SVGAUtils.loadSVGAAnim(ivAvatarFrame, nobleAvatarFrame)
                } else {
                    Glide.with(context).load(nobleAvatarFrame)
                        .into(ivAvatarFrame!!)
                }
                ivAvatarFrame?.visibility = VISIBLE
            } else {
                dimension = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_40)
                ivAvatarFrame?.visibility = INVISIBLE
            }
            ViewUtils.setViewSize(userAvatarView, dimension, dimension)
        }
    }

    private fun bindSendUserInfo(sendUserInfo: GroupUserInfo?) {
        if (sendUserInfo != null) {
            userInfoView?.visibility = VISIBLE
            userInfoView?.setUserInfo(sendUserInfo)
            GlideUtils.loadImage(context, sendUserInfo.portrait,userAvatarView)
            if (ivAvatarFrame != null) {
                val nobleAvatarFrame = sendUserInfo.avatarFrame
                val dimension: Int
                if (!TextUtils.isEmpty(nobleAvatarFrame)) {
                    dimension = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_35)
                    if (nobleAvatarFrame.endsWith(".svga")) {
                        SVGAUtils.loadSVGAAnim(ivAvatarFrame, nobleAvatarFrame)
                    } else {
                        Glide.with(context).load(nobleAvatarFrame)
                            .into(ivAvatarFrame!!)
                    }
                    ivAvatarFrame?.visibility = VISIBLE
                } else {
                    dimension = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_40)
                    ivAvatarFrame?.visibility = INVISIBLE
                }
                ViewUtils.setViewSize(userAvatarView, dimension, dimension)
            }
        } else {
            val user = EaseUserUtils.getGroupUserInfo(message.conversationId(), message.from)
            if (user != null && !TextUtils.isEmpty(user.nickname)) {
                userInfoView?.setNickName(user.nickname)
            }
            EaseUserUtils.setUserAvatar(
                context, message.conversationId(), message.from, userAvatarView
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setCoinReward() {
        try {
            val coinLl: ViewGroup? = findViewById(R.id.coin_ll)
            val coinTxt: TextView? = findViewById(R.id.coin_txt)
            val ivCoin: ImageView? = findViewById(R.id.iv_coin)
            val tvRewardDesc: TextView? = findViewById(R.id.tv_rewardDesc)
            val ivQuestion: ImageView? = findViewById(R.id.iv_question)
            if (coinLl == null || coinTxt == null) {
                return
            }
            var rewardGoldSum =
                message.getFloatAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_SUM, 0f)
            if (rewardGoldSum <= 0) {
                rewardGoldSum =
                    message.getIntAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_SUM, 0).toFloat()
            }
            val rewardGoldStatus =
                message.getIntAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_STATUS, 0)
            val rewardDesc =
                message.getStringAttribute(ImMessageParamsConfig.KEY_REPLY_OUT_TIME_DESC, "")
            tvRewardDesc?.text = ""
            if (rewardGoldSum > 0) {
                coinLl.visibility = VISIBLE
                ivQuestion?.tag = message
                val chatSource = message.getIntAttribute(ImMessageParamsConfig.KEY_CHAT_SOURCE, -1)
                val accostDesc = if (chatSource == 0) "搭讪 " else ""
                when (rewardGoldStatus) {
                    0 -> {
                        val isFirstMessage = message.getBooleanAttribute(
                            ImMessageParamsConfig.KEY_IS_FIRST_MESSAGE, false
                        )
                        //是否是男生回复的为爱牵线的消息
                        val isReplyLoveMsg = message.getBooleanAttribute(
                            ImMessageParamsConfig.KEY_IS_REPLY_LOVE_MSG, false
                        )
                        val msgTime = message.msgTime
                        val differTime = System.currentTimeMillis() - msgTime

                        if (isLoveStringMessage(message)) {
                            val restSecond: Int = (120 - differTime / 1000).toInt()
                            if (restSecond > 0) {
                                startCountDown(
                                    restSecond,
                                    coinTxt,
                                    rewardGoldSum,
                                    "200%",
                                    accostDesc
                                )
                            } else {
                                refreshChatMsg()
                            }
                        } else {
                            if (isFirstMessage || isReplyLoveMsg) {
                                if (chatSource == 0) {
                                    val userSecond: Int = (differTime / 1000).toInt()
                                    if (userSecond < 120) {
                                        //2分钟内
                                        startCountDown(
                                            120 - userSecond,
                                            coinTxt,
                                            rewardGoldSum,
                                            "120%",
                                            accostDesc
                                        )
                                    } else if (userSecond < 1800) {
                                        //半小时内
                                        startCountDown(
                                            1800 - userSecond,
                                            coinTxt,
                                            rewardGoldSum,
                                            "100%",
                                            accostDesc
                                        )
                                    } else {
                                        refreshChatMsg()
                                    }
                                } else {
                                    val userSecond: Int = (differTime / 1000).toInt()
                                    if (userSecond < 120) {
                                        //2分钟内
                                        startCountDown(
                                            120 - userSecond,
                                            coinTxt,
                                            rewardGoldSum,
                                            "120%",
                                            accostDesc
                                        )
                                    } else if (userSecond < 3600) {
                                        //1小时内
                                        startCountDown(
                                            3600 - userSecond,
                                            coinTxt,
                                            rewardGoldSum,
                                            "100%",
                                            accostDesc
                                        )
                                    } else if (userSecond < 3600 * 2) {
                                        // 2小时内
                                        startCountDown(
                                            3600 * 2 - userSecond,
                                            coinTxt,
                                            rewardGoldSum,
                                            "80%",
                                            accostDesc
                                        )
                                    } else if (userSecond < 3600 * 4) {
                                        // 4小时内
                                        startCountDown(
                                            3600 * 4 - userSecond,
                                            coinTxt,
                                            rewardGoldSum,
                                            "50%",
                                            accostDesc
                                        )
                                    } else {
                                        refreshChatMsg()
                                    }
                                }
                            } else {
                                coinTxt.text =
                                    "$accostDesc+${CommonUtils.subZeroAndDot(rewardGoldSum.toString())}金币"
                            }
                        }
                        ivCoin?.visibility = View.INVISIBLE
                        ivQuestion?.visibility = View.GONE
                    }

                    1 -> {
                        coinTxt.text =
                            "$accostDesc+${CommonUtils.subZeroAndDot(rewardGoldSum.toString())}金币 已获得"
                        ivCoin?.visibility = View.VISIBLE
                        if (TextUtils.isEmpty(rewardDesc)) {
                            ivQuestion?.visibility = View.GONE
                        } else {
                            ivQuestion?.visibility = View.VISIBLE
                            tvRewardDesc?.text = "($rewardDesc)"
                            ivQuestion?.setOnSingleClickListener {
                                val tag = ivQuestion.tag
                                if (tag is EMMessage) {
                                    //RewardCoinRulePop.showPop(context, isLoveStringMessage(tag))
                                }
                            }
                        }
                    }

                    -1 -> {
                        coinTxt.text =
                            "$accostDesc+${CommonUtils.subZeroAndDot(rewardGoldSum.toString())}金币 未获得"
                        ivCoin?.visibility = View.GONE
                        ivQuestion?.visibility = View.VISIBLE
                        ivQuestion?.setOnSingleClickListener {
                            val tag = ivQuestion.tag
                            if (tag is EMMessage) {
                                //RewardCoinRulePop.showPop(context, isLoveStringMessage(tag))
                            }
                        }
                    }
                }
            } else {
                coinLl.visibility = GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun startCountDown(
        restSecond: Int,
        coinTxt: TextView,
        rewardGoldSum: Float,
        rewardPercent: String,
        accostDesc: String
    ) {
        setCountDownValue(restSecond, accostDesc, rewardGoldSum, rewardPercent, coinTxt)

        val listener: OnAttachStateChangeListener = object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                val tag = coinTxt.getTag(cn.yanhu.commonres.R.id.tag_msg_id)
                if (tag is EMMessage) {
                    startCountDown(restSecond, coinTxt, rewardGoldSum, rewardPercent, accostDesc)
                }
            }

            override fun onViewDetachedFromWindow(v: View) {
                val tag = coinTxt.getTag(cn.yanhu.commonres.R.id.tag_msg_time)
                if (tag is CoroutineScope) {
                    tag.cancel()
                    coinTxt.setTag(cn.yanhu.commonres.R.id.tag_msg_time, null)
                }
                v.removeOnAttachStateChangeListener(this)
            }
        }
        coinTxt.addOnAttachStateChangeListener(listener)
        val tag = coinTxt.getTag(cn.yanhu.commonres.R.id.tag_msg_time)
        if (tag != null) {
            return
        }
        (context as FragmentActivity).countDown(time = restSecond, start = {
            coinTxt.setTag(cn.yanhu.commonres.R.id.tag_msg_id, message)
            coinTxt.setTag(cn.yanhu.commonres.R.id.tag_msg_time, it)
        }, end = {
            //倒计时结束
            refreshChatMsg()
        }, next = {
            setCountDownValue(it, accostDesc, rewardGoldSum, rewardPercent, coinTxt)
        }, cancel = {})
    }

    private fun setCountDownValue(
        it: Int,
        accostDesc: String,
        rewardGoldSum: Float,
        rewardPercent: String,
        coinTxt: TextView
    ) {
        val minute = it / 60
        val second = it % 60
        val minuteDesc = if (minute >= 10) {
            minute.toString()
        } else {
            "0$minute"
        }
        val secondDesc = if (second >= 10) {
            second.toString()
        } else {
            "0$second"
        }
        val build = Spans.builder().text(
            "$accostDesc+${CommonUtils.subZeroAndDot(rewardGoldSum.toString())}金币 ($minuteDesc:${secondDesc}内回复收益x"
        ).text(rewardPercent).color(CommonUtils.getColor(cn.yanhu.commonres.R.color.colorTextRed)).text(")")
            .build()
        coinTxt.text = build
    }

    private fun refreshChatMsg() {
        LiveEventBus.get<Boolean>(EventBusKeyConfig.REFRESH_CHAT_LIST_DATA).post(true)
    }

    private fun isLoveMsg(lastMessage: EMMessage): Boolean {
        if (lastMessage.type == EMMessage.Type.CUSTOM) {
            val messageBody2 = lastMessage.body as EMCustomMessageBody
            val event2 = messageBody2.event()
            if (ChatConstant.MSG_QIANXIAN == event2) {
                val params = messageBody2.params
                if (params.containsKey("content")) {
                    val content = params["content"]
                    return !TextUtils.isEmpty(content) && content!!.startsWith("为爱牵线")
                }
            }
        }
        return false
    }
    override fun onMessageError() {
        super.onMessageError()
        setStatus(GONE, VISIBLE)
    }

    private  fun setStatus(progressVisible: Int, statusVisible: Int) {
        if (progressBar != null) {
            progressBar.visibility = progressVisible
        }
        if (statusView != null) {
            statusView.visibility = statusVisible
        }
    }
    private fun isLoveStringMessage(lastMessage: EMMessage): Boolean {
        var loveString = lastMessage.getIntAttribute("loveString", -1)
        if (lastMessage.type == EMMessage.Type.CUSTOM) {
            val messageBody2 = lastMessage.body as EMCustomMessageBody
            val event2 = messageBody2.event()
            if (ChatConstant.MSG_QIANXIAN == event2) {
                loveString = 1
            }
        }
        return loveString == 1
    }

    private fun bindAckStatus() {
        if (ackedView != null) {
            if (isSender() && AppCacheManager.isWoman()) {
                ackedView.visibility = VISIBLE
                if (message.isAcked) {
                    ackedView.text = "已读"
                } else {
                    ackedView.text = "未读"
                }
            } else {
                ackedView.visibility = INVISIBLE
            }
        }
    }

    private var group: EMGroup? = null
    private fun bindIdentityView(message: EMMessage) {
//        tvIdentity?.apply {
//            if (group == null) {
//                group = EMClient.getInstance().groupManager().getGroup(message.conversationId())
//            }
//            if (group != null) {
//                val adminList = group!!.adminList
//                val owner = group!!.owner
//                if (message.from == owner) {
//                    this.text = "群主"
//                    this.visibility = View.VISIBLE
//                    this.setBackgroundResource(R.drawable.bg_gradient_red_r4)
//                } else if (adminList.contains(message.from)) {
//                    this.text = "管理员"
//                    this.setBackgroundResource(R.drawable.bg_gradient_purple_r4)
//                    this.visibility = View.VISIBLE
//                } else {
//                    this.visibility = View.INVISIBLE
//                }
//            }
//
//        }

    }
}