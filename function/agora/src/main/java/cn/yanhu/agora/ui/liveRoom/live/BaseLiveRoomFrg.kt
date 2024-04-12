package cn.yanhu.agora.ui.liveRoom.live

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.UserEnterAdapter
import cn.yanhu.agora.adapter.liveRoom.LiveRoomChatMessageAdapter
import cn.yanhu.agora.bean.ChatRoomMsgInfo
import cn.yanhu.agora.bean.RoomDetailInfo
import cn.yanhu.agora.databinding.FrgBaseLiveRoomBinding
import cn.yanhu.agora.listener.SwitchRoomListener
import cn.yanhu.agora.pop.SendMessagePop
import cn.yanhu.agora.ui.liveRoom.LiveRoomViewModel
import cn.yanhu.baselib.anim.AnimManager
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.adapter.GiftAnimAdapter
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.ChatRoomGiftMsg
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.imchat.manager.ImUserManager
import cn.zj.netrequest.ext.parseState
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ThreadUtils
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage
import com.smart.adapter.interf.SmartFragmentImpl2


/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
open class BaseLiveRoomFrg : BaseFragment<FrgBaseLiveRoomBinding, LiveRoomViewModel>(
    R.layout.frg_base_live_room, LiveRoomViewModel::class.java
), SmartFragmentImpl2<RoomListBean>, SwitchRoomListener {

    protected var roomDetailInfo: RoomDetailInfo? = null
    protected var roomId: String = ""
    private var roomType: Int = 0
    private val chatRoomMsgAdapter by lazy { LiveRoomChatMessageAdapter() }
    private var messageDialog: SendMessagePop? = null
    private var currentUser: BaseUserInfo? = null
    private var isShowEmoji = false
    private lateinit var roomSourceBean: RoomListBean


    override fun initData() {
        mBinding.rvSeat.itemAnimator?.changeDuration = 0
        mBinding.rvChat.itemAnimator?.changeDuration = 0
        mBinding.rvChat.adapter = chatRoomMsgAdapter
        roomId = roomSourceBean.roomId.toString()
        roomType = roomSourceBean.roomType

        val list: MutableList<ChatRoomMsgInfo> = mutableListOf()
        list.add(ChatRoomMsgInfo(ChatRoomMsgInfo.TYPE_NOTICE, ""))
        for (i in 0..20) {
            list.add(ChatRoomMsgInfo(ChatRoomMsgInfo.TYPE_TEXT_MSG, ""))
        }
        chatRoomMsgAdapter.submitList(list)

        initGiftAnimAdapter()
        setUserEnterAdapter()
        requestData()
        registerChatRoomMessage()
    }

    private fun initGiftAnimAdapter() {
        mBinding.giftAnimView.setRewardAnimAdapter(GiftAnimAdapter(object :
            GiftAnimAdapter.OnClearListener {
            override fun onClear() {
            }

            override fun onSendGiftSuccess(chatRoomGiftMsg: ChatRoomGiftMsg) {
                super.onSendGiftSuccess(chatRoomGiftMsg)
            }
        }))
    }

    private fun setUserEnterAdapter() {
        mBinding.userEnterView.setRewardAnimAdapter(
            UserEnterAdapter(
                mContext,
                object : UserEnterAdapter.OnClickUserListener {
                    override fun onClickUser(userId: String) {
                    }
                })
        )
    }


    override fun initListener() {
        mBinding.bgInput.setOnSingleClickListener {
            showInputDialog(null, true)
        }
        mBinding.ivExtension.setOnSingleClickListener {
            showInputDialog(null, false)
        }
        val liveRoomActivity = requireActivity() as LiveRoomActivity?
        liveRoomActivity?.addSwitchRoomListener(roomId, this)
    }


    /**
     * 显示聊天输入弹框
     * user 不为null时表示是@用户
     * isKeyboard true 软禁盘自动显示 false 不自动显示
     */
    open fun showInputDialog(user: BaseUserInfo?, isKeyboard: Boolean) {
        currentUser = user
        if (messageDialog == null) {
            messageDialog = SendMessagePop.showDialog(mContext,
                isKeyboard,
                currentUser,
                object : SendMessagePop.OnMessageSendListener {
                    override fun onShowEmoji(height: Int) {
                        isShowEmoji = height > 0
                        ViewUtils.setMarginBottom(mBinding.rvChat, height)
                        setRvChatMessageTop(true)
                    }

                    override fun onSendMessage(content: String) {
                        clickSendMessage(content, currentUser)
                    }

                    override fun onSendEmoji(url: String) {
                        clickSendEmoji(url)
                    }
                },
                object : SimpleCallback() {
                    override fun onDismiss(popupView: BasePopupView) {
                        super.onDismiss(popupView)
                        isShowEmoji = false
                        setRvChatMessageTop(false)
                        ViewUtils.setMarginBottom(mBinding.rvChat, 0)
                        KeyboardUtils.hideSoftInput(context as Activity?)
                    }

                    override fun onKeyBoardStateChanged(popupView: BasePopupView, height: Int) {
                        super.onKeyBoardStateChanged(popupView, height)
                        if (isShowEmoji) {
                            return
                        }
                        setRvChatMessageTop(height != 0)
                        if (height == 0) {
                            ViewUtils.setMarginBottom(mBinding.rvChat, height)
                        } else {
                            ThreadUtils.getMainHandler().postDelayed({
                                ViewUtils.setMarginBottom(
                                    mBinding.rvChat, height + CommonUtils.getDimension(
                                        com.zj.dimens.R.dimen.dp_30
                                    )
                                )
                                scrollChatToBottom(0)
                            }, 450)
                        }
                    }
                })
        } else {
            messageDialog?.showDialog(isKeyboard, currentUser)
        }
    }

    /**
     * TODO 发送自定义表情
     */
    private fun clickSendEmoji(url: String) {

    }

    /**
     * TODO 发送文本消息
     */
    private fun clickSendMessage(content: String, currentUser: BaseUserInfo?) {

    }

    override fun lazyLoad() {
    }

    override fun requestData() {
        super.requestData()
        logcom(LiveRoomActivity.LIVE_ROOM_TAG, "获取房间详情---roomId${roomId}")
        mViewModel.getRoomDetail(roomId, roomType)
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.roomDetailLivedata.observe(this) { it ->
            parseState(it, {
                if (isResumed) {
                    preJoinRoom()
                }
                mBinding.roomInfo = it
                roomDetailInfo = it
                loadRoomBg(it)
                initRvChatTop()
                getRoomInfoSuccess()
            })
        }
    }

    /**
     * 添加新用户进房漂流提示动画
     */
    private fun addUserEnterAnim(user: BaseUserInfo) {
        val chatRoomGiftMsg = ChatRoomGiftMsg(user)
        chatRoomGiftMsg.giftStayTime = 1500
        mBinding.userEnterView.put(chatRoomGiftMsg)
    }


    private fun initRvChatTop() {
        mBinding.rvSeat.viewTreeObserver.addOnGlobalLayoutListener (object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val maxTop: Int =
                    mBinding.rvSeat.height + CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_84)
                ViewUtils.setMarginTop(mBinding.rvChat, maxTop)
                ViewUtils.setMarginBottom(mBinding.rvChat, 0)
                mBinding.rvSeat.viewTreeObserver.removeOnGlobalLayoutListener (this)
            }
        })
    }


    /**
     * @param isShow true软键盘显示 或者表情ui显示
     */
    private fun setRvChatMessageTop(isShow: Boolean) {
        mBinding.rvSeat.post {
            val maxTop: Int =
                mBinding.rvSeat.height + CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_84)
            val minTop: Int = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20)
            if (isShow) {
                if (mBinding.viewMask.visibility == View.INVISIBLE) {
                    mBinding.viewMask.visibility = View.VISIBLE
                    AnimManager.showMarginTopAnimator(mBinding.rvChat, maxTop, minTop, 300)
                }
            } else {
                if (mBinding.viewMask.visibility == View.VISIBLE) {
                    mBinding.viewMask.visibility = View.INVISIBLE
                    AnimManager.showMarginTopAnimator(mBinding.rvChat, minTop, maxTop, 200)
                }
            }
            scrollChatToBottom(400)
        }

    }

    /**
     * 聊天列表滑动到底部
     */
    private fun scrollChatToBottom(duration: Long) {
        ThreadUtils.getMainHandler().postDelayed(
            { mBinding.rvChat.scrollToPosition(chatRoomMsgAdapter.itemCount - 1) }, duration
        )
    }

    /**
     * 网易聊天室消息监听回调
     * 在回调内处理文本，礼物,表情等消息
     */
    private val incomingChatRoomMsg: Observer<List<ChatRoomMessage>> =
        Observer<List<ChatRoomMessage>> {
            it.forEach {
                val msgType = it.msgType
                val content = it.content
                val attachment = it.attachment
            }
        }

    private fun registerChatRoomMessage(isRegister: Boolean = true) {
        NIMClient.getService(ChatRoomServiceObserver::class.java)
            .observeReceiveMessage(incomingChatRoomMsg, isRegister)
    }

    open fun getRoomInfoSuccess() {}

    private fun loadRoomBg(it: RoomDetailInfo) {
        GlideUtils.loadAsDrawable(mContext, it.roomBgUrl, object : CustomTarget<Drawable>() {
            override fun onResourceReady(
                resource: Drawable, transition: Transition<in Drawable>?
            ) {
                mBinding.vgParent.background = resource
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
    }

    override fun preJoinRoom() {
        logcom(LiveRoomActivity.LIVE_ROOM_TAG, "加载房间---roomId${roomId}")
    }


    override fun destroyRoom() {
        logcom(LiveRoomActivity.LIVE_ROOM_TAG, "销毁房间---roomId${roomId}")
        pauseAnimView()
        clearAnimView()
    }


    private fun pauseAnimView() {
        mBinding.userEnterView.onPause()
        mBinding.giftAnimView.onPause()
    }

    private fun destroyAnimView() {
        mBinding.giftAnimView.onDestroy()
        mBinding.userEnterView.onDestroy()
    }

    private fun clearAnimView() {
        mBinding.svgGiftAnim.clearsAfterDetached = true
        mBinding.svgGiftAnim.stopAnimation(true)
        mBinding.svgGiftAnim.clear()
        mBinding.giftAnimView.clear()
        mBinding.userEnterView.clear()
    }

    override fun initSmartFragmentData(bean: RoomListBean) {
        roomSourceBean = bean
    }


    override fun onResume() {
        super.onResume()
        mBinding.userEnterView.onResume()
        mBinding.giftAnimView.onResume()
        addUserEnterAnim(ImUserManager.getSelfUserInfo())
    }

    override fun onPause() {
        super.onPause()
        pauseAnimView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logcom(LiveRoomActivity.LIVE_ROOM_TAG, "onDestroyView---roomId${roomId}")
        (requireActivity() as LiveRoomActivity).removeSwitchRoomListener(roomId)
        registerChatRoomMessage(false)
        destroyAnimView()
    }
}