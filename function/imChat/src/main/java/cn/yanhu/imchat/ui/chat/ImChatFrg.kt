package cn.yanhu.imchat.ui.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.text.TextUtils
import android.view.View
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.queue.TaskQueueManagerImpl
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.CmdMsgTypeConfig
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.task.GiftPopAnimTask
import cn.yanhu.imchat.ImChatViewModel
import cn.yanhu.imchat.R
import cn.yanhu.imchat.config.ChatUiConfig
import cn.yanhu.imchat.databinding.FrgImChatBinding
import cn.yanhu.imchat.db.ChatUserInfoManager
import cn.yanhu.imchat.manager.EmMsgManager
import cn.yanhu.imchat.ui.chatSetting.UserChatSettingActivity
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.status.ErrorCode
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.constants.EaseConstant
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.core.BasePopupView

/**
 * @author: zhengjun
 * created: 2024/2/5
 * desc:
 */
@Suppress("DEPRECATION")
class ImChatFrg : BaseFragment<FrgImChatBinding, ImChatViewModel>(
    R.layout.frg_im_chat,
    ImChatViewModel::class.java
) {
    lateinit var chatFragment: ChatFragment
    private var userId: String = ""
    private var isPop: Boolean = false
    override fun initData() {
        userId = requireArguments().getString(EaseConstant.EXTRA_CONVERSATION_ID).toString()
        if (TextUtils.isEmpty(userId)) {
            finishPage()
            return
        }
        isPop = requireArguments().getBoolean("isPop", false)
        chatFragment = ChatFragment()
        ChatUiConfig.initConfig()
        chatFragment.arguments = arguments
        addFragment(chatFragment)
        userInfo = ChatUserInfoManager.getUserInfo(userId)
        userInfo?.apply {
            bindUserInfo(this)
        }
        chatFragment.setAddFriendListener(object : ChatFragment.OnAddFriendTipsListener{
            override fun onAddFriend() {
                showAddFriendPop()
            }
        })
        playUnShowGiftAnim()
    }

    private fun finishPage() {
        chatFragment?.customEaseChatPrimaryMenu?.hideSoftKeyboard()
        if (isPop) {
            LiveDataEventManager.sendLiveDataMessage(EventBusKeyConfig.CLOSECHATDIALOG, "-1")
        } else {
            mContext.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearAnimView()
    }

    private fun clearAnimView() {
        mBinding.svgGiftAnim.clearsAfterDetached = true
        mBinding.svgGiftAnim.stopAnimation(true)
        mBinding.svgGiftAnim.clear()
    }

    /**
     * 不在聊天页面时收到的礼物消息
     * 再次进入聊天页面时播放礼物特效动画
     */
    private fun playUnShowGiftAnim() {
        val conversation =
            EMClient.getInstance().chatManager().getConversation(userId) ?: return
        val unreadMsgCount = conversation.unreadMsgCount
        if (unreadMsgCount > 0) {
            val emMessages = conversation.searchMsgFromDB(
                System.currentTimeMillis(),
                unreadMsgCount,
                EMConversation.EMSearchDirection.UP
            )
            if (emMessages.size > 0) {
                for (i in emMessages.indices) {
                    val emMessage = emMessages[i]
                    if (emMessage.type == EMMessage.Type.CUSTOM) {
                        val body = emMessage.body as EMCustomMessageBody
                        if (body.event() == ChatConstant.MSG_GIFT) {
                            val params = body.params
                            if (params.containsKey("giftSvga")) {
                                val svgaUrl = params["giftSvga"]
                                playSvga(svgaUrl)
                            }
                        }
                    }
                }
            }
        }
    }

    //播放动画
    private fun playSvga(url: String?) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        showGiftSvgAnim(url)
    }

    private val giftAnimTaskManager = TaskQueueManagerImpl()
    private fun showGiftSvgAnim(url: String?) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        //显示礼物特效svg动画
        giftAnimTaskManager.addTask(GiftPopAnimTask(url!!,mBinding.svgGiftAnim))
    }

    override fun initListener() {
        super.initListener()
        mBinding.ivBack.setOnSingleClickListener {
            finishPage()
        }
        mBinding.ivMore.setOnSingleClickListener {
            UserChatSettingActivity.lunch(mContext, userInfo)
        }
        mBinding.tvAddFriend.setOnSingleClickListener {
            showAddFriendPop()
        }
        LiveEventBus.get<EMMessage>(EventBusKeyConfig.RECEIVE_CMD_MSG).observe(this) {
            val source = it.getIntAttribute("source", -1)
            if (source == CmdMsgTypeConfig.ADD_FRIEND) {
                mBinding.vgAddFriendTips.visibility = View.GONE
            }
        }
        LiveEventBus.get("sendGift", String::class.java).observe(this) { svga ->
            logcom("礼盒：$svga")
            playSvga(svga.toString())
        }
        LiveEventBus.get<MutableList<EMMessage>>(EventBusKeyConfig.RECEIVE_CHAT_MSG).observe(this) {
            for (message in it) {
                if (message.from!= userId){
                    break
                }
                val body = message.body
                if (body is EMCustomMessageBody) {
                    logcom("event：" + (message.body as EMCustomMessageBody).event())
                    if ((message.body as EMCustomMessageBody).event() == ChatConstant.MSG_GIFT) { //接收礼物，播放动画
                        val params = (message.body as EMCustomMessageBody).params
                        if (message.from.equals(userId)) {
                            LiveDataEventManager.sendLiveDataMessage(
                                "sendGift",
                                params["giftSvga"].toString()
                            )
                        }
                    } else if ((message.body as EMCustomMessageBody).event() == ChatConstant.MSG_ADD_FRIEND) { //是否同意好友
                        val params = (message.body as EMCustomMessageBody).params
                        if (params["isApplySuccess"] == "1") {
                            userInfo?.isFriend = true
                            chatFragment.setUserInfo(userInfo)
                        }
                    }
                }
            }
        }
    }

    private fun showAddFriendPop(): BasePopupView {
        return DialogUtils.showConfirmDialog(
            "添加好友",
            {
                addFriend()
            },
            {
            },
            content = "是否同意花费${userInfo?.needRoseNum}玫瑰，添加好友？",
            cancel = "取消",
            confirm = "加好友",
            cancelBg = cn.yanhu.baselib.R.drawable.shape_cancel_btn_r30
        )
    }

    private fun addFriend() {
        mViewModel.addFriend(userId)
        mViewModel.addFriendObservable.observe(this) { it ->
            parseState(it, {
                userInfo?.isFriend = true
                ChatUserInfoManager.saveUserInfo(userInfo)
                chatFragment.setUserInfo(userInfo)
                mBinding.vgAddFriendTips.visibility = View.GONE
                EmMsgManager.sendCmdMessagePeople(userId, CmdMsgTypeConfig.ADD_FRIEND, null)
//                EmMsgManager.sendApplyFriend(
//                    userId,
//                    ImUserManager.getSelfUserInfo().nickName,
//                    userInfo!!.nickName
//                )
            },{
                if (it.code == ErrorCode.CODE_NO_BALANCE){
                    ApplicationProxy.instance.showRechargePop(mContext, true)
                }
            })
        }
    }


    override fun requestData() {
        super.requestData()
        mViewModel.getUserInfo(userId)
    }

    private var userInfo: UserDetailInfo? = null

    @SuppressLint("SetTextI18n")
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.userInfoObserver.observe(this) { it ->
            parseState(it, {
                ChatUserInfoManager.saveUserInfo(it)
                bindUserInfo(it)
            },{
                if (it.code == ErrorCode.HAS_BLACK){
                    finishPage()
                }
            })
        }
    }

    private fun bindUserInfo(it: UserDetailInfo) {
        mBinding.userInfo = it
        mBinding.executePendingBindings()
        userInfo = it

        chatFragment.setUserInfo(userInfo)
        if (it.isFriend) {
            mBinding.vgAddFriendTips.visibility = View.GONE
        } else {
            mBinding.vgAddFriendTips.visibility = View.VISIBLE
            mBinding.tvAddFriend.text = "加好友丨${it.needRoseNum}玫瑰"
        }
    }


    fun onNewIntent(intent: Intent?) {
        //chatFragment!!.onNewIntent(intent)
    }

}