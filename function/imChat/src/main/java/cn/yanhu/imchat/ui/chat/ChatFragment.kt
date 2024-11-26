package cn.yanhu.imchat.ui.chat

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import cn.yanhu.baselib.anim.AnimManager.removeAnimSet
import cn.yanhu.baselib.utils.CommonUtils.isPopShow
import cn.yanhu.baselib.utils.DialogUtils.showConfirmDialog
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.SendGiftRequest
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.ImMessageParamsConfig
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager.isWoman
import cn.yanhu.commonres.manager.AppCacheManager.userId
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.utils.PermissionXUtils.PermissionListener
import cn.yanhu.commonres.utils.PermissionXUtils.checkPermission
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.bean.ChatCheckInfo
import cn.yanhu.imchat.bean.ChatFuncInfo
import cn.yanhu.imchat.custom.chat.CustomEaseChatLayout.SendMsgListener
import cn.yanhu.imchat.custom.chat.CustomEaseChatPrimaryMenu
import cn.yanhu.imchat.custom.chat.CustomEaseChatPrimaryMenu.OnChatTypeClickListener
import cn.yanhu.imchat.custom.chat.CustomEaseHandleMessagePresenterImpl
import cn.yanhu.imchat.listener.CallBackListener
import cn.yanhu.imchat.manager.EmMsgManager
import cn.yanhu.imchat.manager.EmMsgManager.isFateMessage
import cn.yanhu.imchat.manager.ImCallManager
import cn.yanhu.imchat.manager.ImUserManager.getSelfUserInfo
import cn.yanhu.imchat.manager.SmSdkUtils
import cn.yanhu.imchat.pop.ChatSelectImagePop
import cn.yanhu.imchat.pop.ChatSelectImagePop.Companion.showDialog
import cn.yanhu.imchat.pop.SendGiftPop
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ErrorCode
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.UriUtils
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.hjq.toast.ToastUtils
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMTextMessageBody
import com.hyphenate.chat.EMVoiceMessageBody
import com.hyphenate.easeui.domain.EaseEmojicon
import com.hyphenate.easeui.utils.EaseSmileUtils
import com.hyphenate.util.ImageUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.lxj.xpopup.core.BasePopupView
import com.qiyukf.unicorn.mediaselect.MimeType
import java.io.File
import java.util.Timer

class ChatFragment : CustomEaseChatFragment(), SendMsgListener, OnChatTypeClickListener {
    private val ITEM_GIFT = 1
    private val CHAT_TYPE_TXT = "0"
    private val CHAT_TYPE_IMAGE = "1"
    private val CHAT_TYPE_GIF = "2"
    private val CHAT_TYPE_VOICE = "3"
    private val CHAT_TYPE_VIDEO = "4"
    private var clipboard: ClipboardManager? = null
    var customEaseChatPrimaryMenu: CustomEaseChatPrimaryMenu? = null
    private var typingListener: TypingListener? = null
    private var rewardTimer: Timer? = Timer()
    private var userDetailInfo: UserDetailInfo? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setUserInfo(userInfo: UserDetailInfo?) {
        userDetailInfo = userInfo
        customEaseChatPrimaryMenu?.setUserInfo(userInfo)
    }

    override fun initView() {
        super.initView()
        clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        initAvatar()
        initLayout()
        val value = requireArguments().getString(IntentKeyConfig.USERINFO)
        if (!TextUtils.isEmpty(value)) {
            userDetailInfo = GsonUtils.fromJson(value, UserDetailInfo::class.java)
            if (userDetailInfo != null) {
                setUserInfo(userDetailInfo)
            }
        }
        listener()
        onAgreeInviteEvent()
    }

    private val hasShowFirstRechargePop = false
    private val lastChatCardCount = 0
    private fun onAgreeInviteEvent() {
        LiveEventBus.get<Any>(EventBusKeyConfig.AGREE_GROUP_INVITE)
            .observe(this, object : Observer<Any> {
                override fun onChanged(message: Any) {
                    agreeInvite(message as EMMessage)
                }
            })
    }

    private fun agreeInvite(message: EMMessage) {
//        String msg = message.getStringAttribute(ChatConstant.CUSTOM_MSG, "");
//        GroupInviteRecord recordInfo = GsonUtils.fromJson(msg, GroupInviteRecord.class);
//        String id = recordInfo.getId();
//        viewModel.agreeGroupInvite(id);
//        viewModel.groupCheckData.observe(this, statusResponse -> {
//            ToastUtils.show("加入成功");
//            joinGroupSuccess(message);
//        });
//        viewModel.failed.observe(this, ToastUtils::show);
    }

    private val clickResendRecord: MutableMap<String, Boolean> = HashMap()
    override fun onUserAvatarClick(username: String) {
//        Bundle bundle = new Bundle();
//        bundle.putString("userId", username);
//        Intent intent = new Intent(getContext(), PersonalPageActivity.class);
//        intent.putExtras(bundle);
//        startActivity(intent);
    }

    override fun onOtherTyping(action: String) {
        if (typingListener != null) typingListener!!.typingStateChange(action)
    }

    override fun onResendClick(message: EMMessage) {
        if (clickResendRecord.containsKey(message.msgId)) {
            return
        }
        clickResendRecord[message.msgId] = true
        val content = message.getStringAttribute(ImMessageParamsConfig.SM_CHECK_CONTENT, "")
        var source = message.getIntAttribute(ImMessageParamsConfig.SM_CHECK_SOURCE, -1)
        val messageType = message.type
        if (messageType == EMMessage.Type.CUSTOM) {
            val messageBody = message.body as EMCustomMessageBody
            val event = messageBody.event()
            if (ChatConstant.MSG_CUSTOM_GIF_EMOJI == event) {
                //动图
                sendGif(message)
            } else if (ChatConstant.MSG_CUSTOM_EMOJI == event) {
                //自定义表情消息
                val customMsg = message.getStringAttribute(ChatConstant.CUSTOM_MSG, "")
                val split = customMsg.split("/forlove".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val textValue = StringBuilder()
                for (value in split) {
                    if (!(value.contains("bigIconPath") && value.contains("iconPath"))) {
                        textValue.append(value)
                    }
                }
                startSendCheck(message, SmSdkUtils.SOURCE_TXT, textValue.toString())
            } else {
                if (source != -1) {
                    startSendCheck(message, source, content)
                } else {
                    removeClickRecordMsg(message)
                    showFailTips("当前可能包含违规内容，无法发送")
                }
            }
        } else {
            if (source != -1) {
                startSendCheck(message, source, content)
            } else {
                if (messageType == EMMessage.Type.TXT) {
                    val txtBody = message.body as EMTextMessageBody
                    val span = EaseSmileUtils.getSmiledText(mContext, txtBody.message)
                    source = SmSdkUtils.SOURCE_TXT
                    startSendCheck(message, source, span.toString())
                } else {
                    removeClickRecordMsg(message)
                    showFailTips("当前可能包含违规内容，无法发送")
                }
            }
        }
    }

    private fun listener() {
        chatLayout.setSendMsgListener(this)
    }

    /*
     * 设置布局样式
     * */
    private fun initLayout() {
        customEaseChatPrimaryMenu = CustomEaseChatPrimaryMenu(context)
        customEaseChatPrimaryMenu!!.registerMenuClickListener(this)
        chatLayout.chatInputMenu.setCustomPrimaryMenu(customEaseChatPrimaryMenu)
        // 获取到扩展功能控件
        val chatExtendMenu = chatLayout.chatInputMenu.chatExtendMenu ?: return
        // 清除所有的扩展项
        chatExtendMenu.clear()
        LiveEventBus.get<Any>("call_phone").observe(this, object : Observer<Any> {
            override fun onChanged(o: Any) {
                sendCheck("1", "1", object : CallBackListener {
                    override fun onSuccess() {
                        //ImCallManager.checkCall(requireActivity(), ImCallManager.CALL_VIDEO, get getConversationId()());
                    }

                    override fun onError(code: Int, errorMsg: String) {

                    }
                })
            }
        })
        LiveEventBus.get<Any>("slide_introduce").observe(this, object : Observer<Any> {
            override fun onChanged(integer: Any) {
                // SlideVideoIntroduceActivity.lunch(mContext, Integer.parseInt( getConversationId()), 2, 1, null, "");
            }
        })
    }


    override fun onQuoteClick(message: EMMessage): Boolean {
        return false
    }

    override fun onQuoteLongClick(v: View, message: EMMessage): Boolean {
        return false
    }


    private fun sendChatMessage(message: EMMessage) {
        removeClickRecordMsg(message)
        sendChatMessage(message, true)
    }

    private fun removeClickRecordMsg(message: EMMessage) {
        clickResendRecord.remove(message.msgId)
    }

    private fun sendChatMessage(message: EMMessage, isNeedRose: Boolean) {
        addMessageRewardNum(message)
        if (chatLayout == null) {
            EMClient.getInstance().chatManager().sendMessage(message)
        } else {
            chatLayout.sendMessage(message)
        }
    }

    /*
     * 设置头像样式
     * */
    private fun initAvatar() {
        //获取到聊天列表控件
        val messageListLayout = chatLayout.chatMessageListLayout
        //设置头像形状：0 为默认，1 为圆形，2 为方形
        messageListLayout.setAvatarShapeType(1)
        GlideUtils.loadAsDrawable(
            mContext,
            getSelfUserInfo().portrait,
            object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    chatLayout.chatMessageListLayout.setAvatarDefaultSrc(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })
    }

    /*
     * 监听消息发送
     * */
    override fun sendAlert(map: Map<String, Any>, type: Int) {
        if (type == 0) { //发送语音
            val path = map["path"] as String?
            val length = map["length"] as Int
            sendVoiceMessage(path, length)
        } else { //发送文本
            val content = map["content"] as String?
            if (!TextUtils.isEmpty(content)) {
//                val conversation =
//                    EMClient.getInstance().chatManager().getConversation(conversationId)
//                val lastMessage = conversation.lastMessage
//                val startTime = getYestodyStr(-7, "yyyy-MM-dd")
 //               val timeMillis =
  //                  TimeUtils.date2Millis(TimeUtils.string2Date(startTime, "yyyy-MM-dd"))
 //               val firstSend = isFirstSend
//                if (isWoman() && content!!.length < 5 && (firstSend || lastMessage == null || lastMessage.msgTime < timeMillis || ImConversationMsgFilterManager.isLoveQianXianMsg(
//                        lastMessage
//                    ))
//                ) {
//                    //女性用户 首次发送/或者7天内没有聊天/或者回复的消息是缘分牵线不能少于5个字
//                    ToastUtils.show("不能少于5个字")
//                    customEaseChatPrimaryMenu!!.editText.setText(content)
//                    customEaseChatPrimaryMenu!!.editText.setSelection(content.length)
//                    return
//                }
                sendTxtMessage(content)
            }
        }
    }

    /**
     * 发送gif动图消息
     */
    override fun sendGifEmoji(emjicon: Any) {
        if (emjicon is EaseEmojicon) {
            sendEmoji(emjicon)
        }
    }

    private fun sendEmoji(emjicon: EaseEmojicon) {
        if (isWoman()) {
            if (waitReplyTips()) return
        }
        val emMessage = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
        val body = EMCustomMessageBody(ChatConstant.MSG_CUSTOM_GIF_EMOJI)
        emMessage.body = body
        emMessage.to = conversationId
        emMessage.setAttribute(ChatConstant.CUSTOM_MSG, emjicon.bigIconPath)
        saveMsgToLocal(emMessage)
        sendGif(emMessage)
    }

    private fun sendGif(emMessage: EMMessage) {
        sendCheck("0", CHAT_TYPE_GIF, object : CallBackListener {
            override fun onSuccess() {
                sendChatMessage(emMessage)
            }

            override fun onError(code: Int, errorMsg: String) {
                updateMsgFail(emMessage, errorMsg)
            }
        })
    }

    private var failMsgDialog: BasePopupView? = null
    private fun showFailTips(errorMsg: String?, code: Int = -1) {
        if (isPopShow(failMsgDialog)) {
            return
        }
        val topActivity = ActivityUtils.getTopActivity()
        if (topActivity == null || topActivity.isDestroyed || topActivity !is ImChatActivity) {
            return
        }
        failMsgDialog = showConfirmDialog("温馨提示", {
            if (code == ErrorCode.CODE_NEED_REAL_NAME) {
                RouteIntent.lunchToRealNamPage()
            }
        }, errorMsg!!, if (code == ErrorCode.CODE_NEED_REAL_NAME) "去认证" else "我知道了")
    }

    /**
     * 发送消息前 先将消息保存到本地
     * 等调用判断发送条件接口的逻辑响应后
     * 再去更新消息的发送成功还是失败的状态
     */
    private fun saveMsgToLocal(emMessage: EMMessage) {
        EMClient.getInstance().chatManager().saveMessage(emMessage)
        chatLayout.chatMessageListLayout.refreshToLatest()
    }

    private val isFirstSend: Boolean
        /**
         * 是否首次发送
         *
         * @return
         */
        private get() {
            val conversation = EMClient.getInstance().chatManager().getConversation(conversationId)
            val allMessages = conversation.allMessages
            for (i in allMessages.indices) {
                val emMessage = allMessages[i]
                val from = emMessage.from
                val type = emMessage.type
                if (type == EMMessage.Type.CUSTOM) {
                    val body = emMessage.body as EMCustomMessageBody
                    val event = body.event()
                    if (event == ChatConstant.MSG_CUSTOM_EMOJI || event == ChatConstant.MSG_CUSTOM_GIF_EMOJI) {
                        if (userId == from) {
                            return false
                        }
                    }
                } else {
                    if (userId == from) {
                        return false
                    }
                }
            }
            return true
        }

    private fun sendVoiceMessage(path: String?, length: Int, chatSource: Int = -1) {
        val message: EMMessage
        if (isNetUrl(path)) {
            message = EMMessage.createSendMessage(EMMessage.Type.VOICE)
            val body = EMVoiceMessageBody(Uri.parse(path), length)
            body.remoteUrl = path
            message.to = conversationId
            message.addBody(body)
        } else {
            message = EMMessage.createVoiceSendMessage(Uri.parse(path), length, conversationId)
        }
        if (chatSource != -1) {
            message.setAttribute(ImMessageParamsConfig.KEY_CHAT_SOURCE, chatSource)
        }
        message.chatType = EMMessage.ChatType.Chat
        smCheckBeforeSend(message, SmSdkUtils.SOURCE_VOICE, path)
    }

    /*
     * 底部扩展点击监听 start
     * */
    private val isClickPhoto = false

    @SuppressLint("CheckResult")
    override fun clickPhoto() { //相册
        if (isClickPhoto || userDetailInfo == null) {
            return
        }
        showCameraDialog()
    }

    override fun clickHotChatTxt() {}
    private var selectImagePop: ChatSelectImagePop? = null

    @SuppressLint("CheckResult")
    private fun showCameraDialog() {
        val data: MutableList<ChatFuncInfo> = ArrayList()
        data.add(ChatFuncInfo(1, false, "发照片", ""))
        data.add(ChatFuncInfo(1, false, "拍照", ""))
        data.add(ChatFuncInfo(1, false, "发视频", ""))
        if (selectImagePop != null && selectImagePop!!.isShow) {
            return
        }
        selectImagePop = showDialog(mContext, data, object : OnResultCallbackListener<LocalMedia> {
            override fun onResult(result: ArrayList<LocalMedia>) {
                sendImageOrVideoMessage(result[0])
            }

            override fun onCancel() {}
        })
    }

    override fun onShowEmojiInput(show: Boolean) { //表情
        chatLayout.chatInputMenu.showEmojiconMenu(show)
    }

    override fun onSendCustomEmoji(url: String) {
        //发送
    }

    override fun onSendGift() { //礼物
        showSendGiftPop()
    }

    private var sendGiftPop: SendGiftPop? = null
    private fun showSendGiftPop() {
        if (isPopShow(sendGiftPop)) {
            return
        }
        val sendUserInfo = UserDetailInfo()
        sendUserInfo.userId = conversationId
        sendUserInfo.roomId = conversationId.toInt()
        sendGiftPop = SendGiftPop.showDialog(mContext as FragmentActivity,
            sendUserInfo, SendGiftRequest.SOURCE_CHAT, 0,
            object : SendGiftPop.OnSendGiftListener {
                override fun onSendGift(item: GiftInfo) {
                    EmMsgManager.sendGiftMessage(item, conversationId, chatLayout)
                }
            })
    }

    override fun onAddFriend() {
        onAddFriendTipsListener?.onAddFriend()
    }

    override fun onRecharge() {
        showRechargeListDialog()
    }

    private var onAddFriendTipsListener:OnAddFriendTipsListener?=null
     fun setAddFriendListener(onAddFriendTipsListener: OnAddFriendTipsListener){
        this.onAddFriendTipsListener = onAddFriendTipsListener
    }
     interface OnAddFriendTipsListener{
        fun onAddFriend()
    }

    override fun clickVoice() { //语音
        if (userDetailInfo == null) {
            return
        }
        checkVoicePermission()
    }

    private fun checkVoicePermission() {
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.RECORD_AUDIO)
        checkPermission(this,
            permissions,
            "对爱交友想访问您的麦克风权限，用于提供语音相关的功能或服务",
            "您拒绝授权麦克风权限，无法使用语音相关的功能或服务",
            object : PermissionListener {
                override fun onSuccess() {
                    customEaseChatPrimaryMenu!!.showVoiceStatus()
                }

                override fun onFail() {}
            })
    }

    override fun clickPhone() { //语音、视频通话
        if (ApplicationProxy.instance.isShowFloatCalling()) {
            ToastUtils.show("正在通话中，请结束当前通话后再试")
            return
        }
        sendCheck("1", "1", object : CallBackListener {
            override fun onError(code: Int, errorMsg: String) {
                when (code) {
                    ErrorCode.CODE_NO_BALANCE -> {
                        showRechargeListDialog()
                    }

                    ErrorCode.CODE_NEED_REAL_NAME -> {
                        showToast(errorMsg)
                        RouteIntent.lunchToRealNamPage()
                    }

                    else -> {
                        showToast(errorMsg)
                    }
                }
            }

            override fun onSuccess() {
                ImCallManager.checkCall(
                    mContext as FragmentActivity, ImCallManager.CALL_VIDEO, conversationId
                )
            }
        })
    }

    override fun onAddVoiceMsg() {}

    /**
     * 发送消息前的判断
     *
     * @param chatType         0发送消息  1请求1v1通话
     * @param type             消息类型
     * @param callBackListener
     */
    @SuppressLint("CheckResult")
    private fun sendCheck(chatType: String, type: String, callBackListener: CallBackListener) {
        request(
            { imChatRxApi.sendCheck(conversationId, chatType, type) },
            object : OnRequestResultListener<ChatCheckInfo> {
                override fun onSuccess(data: BaseBean<ChatCheckInfo>) {
                    val checkInfo = data.data
                    if (checkInfo?.pass == true) {
                        rewardGoldSum = checkInfo.rewardGold.toInt()
                        callBackListener.onSuccess()
                        if (mContext.isDestroyed) {
                            return
                        }
                    } else {
                        callBackListener.onError(data.code, data.msg)
                    }
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    callBackListener.onError(code!!, msg!!)
                }
            },
            false
        )
    }

    /*
     * 发送文本消息
     * */
    private fun sendTxtMessage(content: String?, chatSource: Int = -1) {
        if (content!!.contains("http") && content.contains("/forlove")) {
            //发送自定义表情文字混发
            val split =
                content.split("/forlove".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val textValue = StringBuilder()
            for (value in split) {
                if (!(value.contains("bigIconPath") && value.contains("iconPath"))) {
                    textValue.append(value)
                }
            }
            val emMessage = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
            val body = EMCustomMessageBody(ChatConstant.MSG_CUSTOM_EMOJI)
            emMessage.body = body
            emMessage.to = conversationId
            emMessage.setAttribute(ChatConstant.CUSTOM_MSG, content)
            if (chatSource != -1) {
                emMessage.setAttribute(ImMessageParamsConfig.KEY_CHAT_SOURCE, chatSource)
            }
            smCheckBeforeSend(emMessage, SmSdkUtils.SOURCE_TXT, textValue.toString())
        } else {
            val message = EMMessage.createTextSendMessage(content, conversationId)
            message.chatType = EMMessage.ChatType.Chat
            if (chatSource != -1) {
                message.setAttribute(ImMessageParamsConfig.KEY_CHAT_SOURCE, chatSource)
            }
            smCheckBeforeSend(message, SmSdkUtils.SOURCE_TXT, content)
        }
    }

    private var rewardGoldSum = -1 //获取金币数
    private fun addMessageRewardNum(message: EMMessage) {
        if (rewardGoldSum > 0) {
            message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_SUM, rewardGoldSum)
            rewardGoldSum = -1
        }
        message.setAttribute(ImMessageParamsConfig.KEY_REWARD_GOLD_STATUS, 0)
    }

    override fun addMsgAttrsBeforeSend(message: EMMessage) {
        super.addMsgAttrsBeforeSend(message)
        val myUserInfo = getSelfUserInfo()
        message.setAttribute(ChatConstant.CUSTOM_SEND_USER_INFO, GsonUtils.toJson(myUserInfo))
    }

    /*
     * 发送图片/视频消息
     * */
    private fun sendImageOrVideoMessage(localMedia: LocalMedia) {
        val type: String
        val mimeType = localMedia.mimeType
        type = if (MimeType.isVideo(mimeType)) {
            CHAT_TYPE_VIDEO
        } else {
            CHAT_TYPE_IMAGE
        }
        if (selectImagePop != null) {
            selectImagePop!!.dismiss()
        }
        sendVideoOrImageMsg(type, localMedia)
    }

    private fun sendVideoOrImageMsg(finalType: String, localMedia: LocalMedia) {
        if (finalType == "4") {
            val availablePath = localMedia.availablePath
            val uri = UriUtils.file2Uri(File(availablePath))
            val thumbPath =
                (chatLayout.presenter as CustomEaseHandleMessagePresenterImpl).getThumbPath(uri)
            val message = EMMessage.createVideoSendMessage(
                uri, thumbPath, localMedia.duration.toInt(), conversationId
            )
            message.chatType = EMMessage.ChatType.Chat
            smCheckBeforeSend(message, SmSdkUtils.SOURCE_VIDEO, availablePath)
        } else {
            val availablePath = localMedia.availablePath
            var restoreImageUri =
                ImageUtils.checkDegreeAndRestoreImage(mContext, Uri.parse(availablePath))
            restoreImageUri =
                (chatLayout.presenter as CustomEaseHandleMessagePresenterImpl).handleImageHeifToJpeg(
                    restoreImageUri
                )
            val message = EMMessage.createImageSendMessage(restoreImageUri, true, conversationId)
            message.chatType = EMMessage.ChatType.Chat
            smCheckBeforeSend(message, SmSdkUtils.SOURCE_IMAGE, availablePath)
        }
    }

    /**
     * 发送消息前 进行数美合规检测
     *
     * @param message
     * @param source
     * @param content
     */
    private fun smCheckBeforeSend(message: EMMessage, source: Int, content: String?) {
        //消息先保存到本地 聊天列表中则可暂时显示发送中的样式
//        if (isWoman()) {
//            val replyQianXianCount = chatLayout.chatMessageListLayout.replyQianXianCount
//            if (replyQianXianCount == 0 && source == SmSdkUtils.SOURCE_TXT && content!!.length < 5 && message.type == EMMessage.Type.TXT) {
//                ToastUtils.show("不能少于5个字")
//                return
//            }
//            if (waitReplyTips()) return
//        }
        saveMsgToLocal(message)
        startSendCheck(message, source, content!!)
    }

    private fun waitReplyTips(): Boolean {
        val conversation = EMClient.getInstance().chatManager().getConversation(conversationId)
        val lastMessage = conversation.lastMessage
        if (conversation.lastMessage != null && conversation.lastMessage.from == userId && !isFateMessage(
                lastMessage
            )
        ) {
            if (chatLayout.chatMessageListLayout.replyQianXianCount >= 3) {
                ToastUtils.show("请等待对方回复")
                return true
            }
        }
        return false
    }

    private fun startSendCheck(message: EMMessage, source: Int, content: String) {
        message.setAttribute(ImMessageParamsConfig.SM_CHECK_CONTENT, content)
        message.setAttribute(ImMessageParamsConfig.SM_CHECK_SOURCE, source)
        var type = CHAT_TYPE_TXT
        if (source == SmSdkUtils.SOURCE_TXT) {
            type = CHAT_TYPE_TXT
        } else if (source == SmSdkUtils.SOURCE_VOICE) {
            type = CHAT_TYPE_VOICE
        } else if (source == SmSdkUtils.SOURCE_VIDEO) {
            type = CHAT_TYPE_VIDEO
        } else if (source == SmSdkUtils.SOURCE_IMAGE) {
            type = CHAT_TYPE_IMAGE
        }
        sendCheck("0", type, object : CallBackListener {
            override fun onSuccess() {
                //满足发送消息条件
                SmSdkUtils.smCheckBeforeSend(content,
                    conversationId,
                    SmSdkUtils.TYPE_MESSAGE,
                    source,
                    object : SmSdkUtils.OnSmCheckResultListener {
                        override fun onCheckSuccess(smCheckId: String?) {
                            message.setAttribute(ImMessageParamsConfig.SM_CHECK_ID, smCheckId)
                            sendChatMessage(message)
                        }

                        override fun onCheckFail(code: Int?, msg: String?) {
                            updateMsgFail(message, msg)
                        }

                    });
            }

            override fun onError(code: Int, errorMsg: String) {
                //不满足发送消息条件 更新本地消息为发送失败
                if (code == ErrorCode.CODE_NO_BALANCE) {
                    updateMsgFail(message, "")
                    showRechargeListDialog()
                } else {
                    updateMsgFail(message, errorMsg, code)
                }
            }
        })
    }

    private fun updateMsgFail(message: EMMessage, errorMsg: String?, code: Int = -1) {
        removeClickRecordMsg(message)
        if (!TextUtils.isEmpty(errorMsg)) {
            showFailTips(errorMsg, code)
        }
        message.setStatus(EMMessage.Status.FAIL)
        EMClient.getInstance().chatManager().updateMessage(message)
        notifyChatMsgItem()
    }

    private fun notifyChatMsgItem() {
        ThreadUtils.getMainHandler().postDelayed(
            { chatLayout.chatMessageListLayout.messageAdapter.notifyItemChanged(if (chatLayout.chatMessageListLayout.layoutManager.reverseLayout) 0 else chatLayout.chatMessageListLayout.messageAdapter.itemCount - 1) },
            500
        )
    }

    //显示充值
    private fun showRechargeListDialog() {
        ApplicationProxy.instance.showRechargePop(mContext as FragmentActivity, true)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (customEaseChatPrimaryMenu != null) {
            removeAnimSet(customEaseChatPrimaryMenu!!.animatorSet)
        }
        if (rewardTimer != null) {
            rewardTimer!!.purge()
            rewardTimer!!.cancel()
            rewardTimer = null
        }
    }

    fun setTypingListener(typingListener: TypingListener?) {
        this.typingListener = typingListener
    }

    interface TypingListener {
        fun typingStateChange(action: String?)
    }

    companion object {
        private fun isNetUrl(path: String?): Boolean {
            return path!!.startsWith("http") || path.startsWith("https")
        }
    }
}