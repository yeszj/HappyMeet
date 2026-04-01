package cn.yanhu.imchat.pop

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import cn.yanhu.baselib.adapter.FrgFragmentStateAdapter
import cn.yanhu.baselib.base.BaseSheetDialog
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.logComToFile
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.ComboCountInfo
import cn.yanhu.commonres.bean.CommonErrorTipsInfo
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.SendGiftRequest
import cn.yanhu.commonres.bean.SendGiftRequest.SOURCE_CHAT
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.bean.response.GiftResponse
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.imchat.R
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.databinding.PopSendGiftBinding
import cn.yanhu.imchat.manager.EmMsgManager
import cn.yanhu.imchat.manager.SendGiftCheckManager
import cn.yanhu.imchat.manager.SendGiftCheckManager.checkSendGift
import cn.yanhu.imchat.manager.SendGiftCheckManager.sendGiftRequest
import cn.yanhu.imchat.manager.SmSdkUtils.SOURCE_VIDEO
import cn.yanhu.imchat.view.GiftShowFrg
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ErrorCode
import com.blankj.utilcode.util.VibrateUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.xiaomi.push.bi
import com.xiaomi.push.da
import java.math.BigDecimal

/**
 * @author: zhengjun
 * created: 2024/10/11
 * desc:
 */
@SuppressLint("ViewConstructor")
class SendGiftPop() : BaseSheetDialog<PopSendGiftBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): PopSendGiftBinding {
        return PopSendGiftBinding.inflate(inflater, container, false)
    }


    private var giftInfo: GiftResponse? = null
    private var source: Int = 0
    private var callId: Int = 0
    private var targetUserInfo: UserDetailInfo = UserDetailInfo()
    private var onSendGiftListener: OnSendGiftListener? = null
    private var isShowContinueClick: Boolean = false
    private var foreverFaceCount = 0
    private var randomBoxMaxNum = 1//盲盒最大可选择数量

    @SuppressLint("CommitTransaction", "ClickableViewAccessibility", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            targetUserInfo =
                requireArguments().getSerializable(IntentKeyConfig.DATA) as UserDetailInfo
            source = requireArguments().getInt("source")
            callId = requireArguments().getInt("callId")
            isShowContinueClick = requireArguments().getBoolean("isShowContinueClick", false)
            randomBoxMaxNum = requireArguments().getInt("randomBoxMaxNum")
            this.userInfo = targetUserInfo
            this.executePendingBindings()
            logcom("showGiftPop = show")
            val balanceRose = requireArguments().getString("balanceRose", "")
            if (!TextUtils.isEmpty(balanceRose) && isShowContinueClick) {
                binding?.tvRoseNum?.text = balanceRose
            }

            initTabLayout()
            this.ivSub.setOnClickListener {
                if (binding!!.tvNum.text.toString().toInt() > 1) {
                    val count = binding!!.tvNum.text.toString().toInt() - 1
                    binding!!.tvNum.text = count.toString()
                    if (count == 1) {
                        this.ivSub.setImageResource(cn.yanhu.commonres.R.drawable.svg_gift_gray_sub)
                    }
                }
            }
            this.ivAdd.setOnClickListener {
                val count = binding!!.tvNum.text.toString().toInt() + 1
                val fragment = giftViewsList[viewPager.currentItem] as GiftShowFrg
                val selectItem = fragment.getSelectItem()
                selectItem?.apply {
                    if (this.type == GiftInfo.TYPE_RANDOM_BOX && count > randomBoxMaxNum) {
                        //盲盒礼物根据配置项限制连送数量
                        if (randomBoxMaxNum == 1) {
                            showToast("当前礼物不支持连送")
                        } else {
                            showToast("${this.name}连送最多上限${randomBoxMaxNum}个哦")
                        }
                        return@apply
                    }
                    val balanceRose = binding!!.tvRoseNum.text.toString()
                    val totalPrice =
                        CommonUtils.multiplyString(count.toString(), selectItem.price.toString())
                    if (CommonUtils.compareString(balanceRose, totalPrice)) {
                        binding!!.tvNum.text = count.toString()
                        binding!!.ivSub.setImageResource(cn.yanhu.commonres.R.drawable.svg_gift_white_sub)
                    } else {
                        showRechargePop()
                    }
                }
            }
            this.tvAddFriend.setOnSingleClickListener {
                onSendGiftListener?.onAddFriend()
            }
            this.tvRecharge.setOnSingleClickListener {
                showRechargePop()
            }
            this.btnSend.setOnSingleClickListener {
                val fragment = giftViewsList[viewPager.currentItem] as GiftShowFrg
                val selectItem = fragment.getSelectItem()
                selectItem?.apply {
                    this.sendNumber = binding!!.tvNum.text.toString().toInt()
                    checkSendGift(this)
                }
            }
            this.tvSendAll.setOnSingleClickListener {
                val fragment = giftViewsList[viewPager.currentItem] as GiftShowFrg
                val selectItem = fragment.getSelectItem()
                selectItem?.apply {
                    this.sendNumber = binding!!.tvNum.text.toString().toInt()
                    if (this.sendNumber > 1) {
                        showToast("连送时不支持全麦赠送哦")
                    } else {
                        val balanceRose = binding!!.tvRoseNum.text.toString()
                        onSendGiftListener?.onSendAll(selectItem, balanceRose)
                    }
                }
            }
            this.ivAvatar.setOnSingleClickListener {
                onSendGiftListener?.onShowUserInfo(targetUserInfo.userId)
            }
            if (SendGiftRequest.SOURCE_LIVE_ROOM == source) {
                onSendGiftListener?.onShowFriendBtn()
            }
        }

        LiveEventBus.get<Boolean>(LiveDataEventManager.PAY_RESULT).observe(this) {
            if (it) {
                (giftViewsList[0] as GiftShowFrg).getGiftInfo()
            }
        }

    }

    fun showAddFriendsBtn(userInfo: UserDetailInfo) {
        if (targetUserInfo.userId != userInfo.userId){
            return
        }
        this.targetUserInfo = userInfo
        if (binding != null) {
            if (targetUserInfo.isFriend || (targetUserInfo.isSameGender && AppCacheManager.isMan())) {
                binding!!.tvAddFriend.visibility = View.INVISIBLE
            } else {
                binding!!.tvAddFriend.visibility = View.VISIBLE
            }
        }
    }

    private var sendGiftListener = object : GiftShowFrg.OnClickSendListener {
        override fun onSendGift(item: GiftInfo?) {
            checkSendGift(item!!)
        }

        override fun onClickGift(item: GiftInfo?) {
            if (item == null) {
                return
            }
            if (item.type == GiftInfo.TYPE_FACE) {
                request({
                    imChatRxApi.getStickerGiftMinNum(
                        targetUserInfo.roomId.toString(),
                        targetUserInfo.userId,
                        item.id
                    )
                }, object : OnRequestResultListener<ComboCountInfo> {
                    override fun onSuccess(data: BaseBean<ComboCountInfo>) {
                        data.data?.apply {
                            binding?.apply {
                                if (this.tabLayout.checkedRadioButtonId == R.id.tv_face) {
                                    foreverFaceCount = minNum
                                    if (minNum > 0) {
                                        vgFaceTips.visibility = View.VISIBLE
                                        tvFaceCount.text = minNum.toString()
                                    } else {
                                        vgFaceTips.visibility = View.INVISIBLE
                                    }
                                }
                            }

                        }
                    }
                })
            } else if (item.type == GiftInfo.TYPE_RANDOM_BOX) {
                binding?.apply {
                    val sendNumber = tvNum.text.toString().toInt()
                    if (sendNumber > randomBoxMaxNum) {
                        tvNum.text = randomBoxMaxNum.toString()
                    }
                }

            }
        }

        override fun setGiftInfo(giftResponse: GiftResponse, type: Int) {
            giftInfo = giftResponse
            if (giftResponse.list.isEmpty() && SOURCE_VIDEO != source && SOURCE_CHAT != source && type == GiftInfo.TYPE_LOVER) {
                myFragmentStateAdapter?.removeItem(2)
                binding?.tvLovers?.visibility = View.GONE
                binding?.tabLayout?.removeView(binding?.tvLovers)
            }
            setGiftInfo()
        }
    }

    private fun clickSendGift(item: GiftInfo?) {
        if (SOURCE_VIDEO != source && SOURCE_CHAT != source && isShowContinueClick && item?.type != GiftInfo.TYPE_LOVER && item?.type != GiftInfo.TYPE_RANDOM_BOX && item?.type != GiftInfo.TYPE_FRAME) {
            onSendGiftListener?.onSendGift(item!!, true)
        } else {
            item?.apply {
                startSendGift(this)
            }
        }
    }

    private fun checkSendGift(item: GiftInfo) {
        item.sendNumber = binding!!.tvNum.text.toString().toInt()
        val balanceRose = binding?.tvRoseNum?.text.toString()
        if (CommonUtils.compareString(
                balanceRose,
                CommonUtils.multiplyString(item.price.toString(), item.sendNumber.toString())
            )
        ) {
            SendGiftCheckManager.checkSendGift(
                targetUserInfo.userId,
                item.id,
                object : SendGiftCheckManager.OnCheckGiftListener {
                    override fun onCanSend() {
                        clickSendGift(item)
                    }
                })
        } else {
            showRechargePop(true)
        }
    }


    private val giftViewsList = mutableListOf<Fragment>()
    private var myFragmentStateAdapter: FrgFragmentStateAdapter? = null
    private fun initTabLayout() {
        binding?.apply {
            val giftShowView = GiftShowFrg.newInstance(source, GiftInfo.TYPE_GIFT)
            giftShowView.registerClickSendListener(sendGiftListener)
            giftViewsList.add(giftShowView)
            if (SOURCE_VIDEO != source && SOURCE_CHAT != source) {
                val faceGiftShowView = GiftShowFrg.newInstance(source, GiftInfo.TYPE_FACE)
                giftViewsList.add(faceGiftShowView)
                faceGiftShowView.registerClickSendListener(sendGiftListener)
                if (!targetUserInfo.isSameGender) {
                    val loversGiftShowView =
                        GiftShowFrg.newInstance(source, GiftInfo.TYPE_LOVER)
                    giftViewsList.add(loversGiftShowView)
                    loversGiftShowView.registerClickSendListener(sendGiftListener)
                } else {
                    tabLayout.removeView(tvLovers)
                    tvLovers.visibility = View.GONE
                }
            } else {
                tabLayout.removeView(tvFace)
                tvFace.visibility = View.GONE
                tvLovers.visibility = View.GONE
                tabLayout.removeView(tvLovers)
            }
            val frameShowView = GiftShowFrg.newInstance(source, GiftInfo.TYPE_FRAME)
            frameShowView.registerClickSendListener(sendGiftListener)
            giftViewsList.add(frameShowView)

            viewPager.offscreenPageLimit = giftViewsList.size
            myFragmentStateAdapter = FrgFragmentStateAdapter(this@SendGiftPop, giftViewsList)
            viewPager.adapter = myFragmentStateAdapter
            tabLayout.setOnCheckedChangeListener { _, checkedId ->
                vgFaceTips.visibility = View.INVISIBLE
                when (checkedId) {
                    R.id.tv_gift -> {
                        setCurrentItem(0)
                    }

                    R.id.tv_face -> {
                        setCurrentItem(1)
                    }

                    R.id.tv_lovers -> {
                        setCurrentItem(2)
                    }

                    R.id.tv_frame -> {
                        setCurrentItem(giftViewsList.size - 1)
                    }
                }
            }
            tabLayout.check(tabLayout.getChildAt(0).id)
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    tabLayout.check(tabLayout.getChildAt(position).id)
                    if (position == 2) {
                        tvSendAll.visibility = View.GONE
                    } else {
                        tvSendAll.visibility = View.VISIBLE
                    }
                }
            })
        }
    }


    private fun setCurrentItem(position: Int) {
        binding?.viewPager?.setCurrentItem(position, true)
    }

    private fun setGiftInfo() {
        giftInfo?.apply {
            if (!isShowContinueClick) {
                binding?.tvRoseNum?.text = this.roseNum.toPlainString()
            }
        }
    }

    private var sendPosition = 0
    private fun startSendGift(item: GiftInfo) {
        val balanceRose = binding?.tvRoseNum?.text.toString()
        if (CommonUtils.compareString(
                balanceRose,
                CommonUtils.multiplyString(item.price.toString(), item.sendNumber.toString())
            )
        ) {
            sendPosition = 0
            sendGift(item, item.sendNumber)
        } else {
            showRechargePop(true)
        }
    }

    private fun createSendRequest(item: GiftInfo): SendGiftRequest {
        val sendGiftRequest = SendGiftRequest()
        sendGiftRequest.roomId = targetUserInfo.roomId.toString()
        sendGiftRequest.toUid = targetUserInfo.userId
        sendGiftRequest.giftId = item.id
        if (item.type == GiftInfo.TYPE_RANDOM_BOX) {
            sendGiftRequest.num = 1
        } else {
            sendGiftRequest.num = item.sendNumber
        }
        sendGiftRequest.source =
            if (source == SOURCE_VIDEO) SendGiftRequest.SOURCE_CALL else if (source == SOURCE_CHAT) SendGiftRequest.SOURCE_CHAT else SendGiftRequest.SOURCE_LIVE_ROOM
        sendGiftRequest.callId = callId
        return sendGiftRequest
    }

    private fun sendGift(item: GiftInfo, count: Int) {
        val sendGiftRequest = createSendRequest(item)
        request2(
            { imChatRxApi.sendGift(sendGiftRequest) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {

                    if (item.type == GiftInfo.TYPE_RANDOM_BOX) {
                        item.sendNumber = 1
                        sendPosition++
                        if (sendPosition < count) {
                            sendGift(item, count)
                        } else {
                            showToast("赠送礼物成功")
                            VibrateUtils.vibrate(50)
                        }
                    } else {
                        showToast("赠送礼物成功")
                        VibrateUtils.vibrate(50)
                    }
                    giftInfo?.roseNum = BigDecimal(
                        CommonUtils.subString(
                            giftInfo!!.roseNum.toPlainString(),
                            CommonUtils.multiplyString(
                                item.price.toString(),
                                sendGiftRequest.num.toString()
                            )
                        )
                    )
                    binding?.tvRoseNum?.text = giftInfo!!.roseNum.toPlainString()

                    sendCallTypeMsg(item, sendGiftRequest)

                    if (item.type == GiftInfo.TYPE_RANDOM_BOX) {
                        item.randomBoxGiftInfo = data.data
                    }
                    logComToFile("sendGift", "赠送礼物成功，giftName=${item.name}")
                    if (foreverFaceCount > 0 && item.sendNumber >= foreverFaceCount) {
                        item.isForeverFaceEffect = true
                    }
                    onSendGiftListener?.onSendGift(item, false)
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    if (code == ErrorCode.CODE_NO_BALANCE) {
                        showRechargePop(true)
                    }
                }
            })
    }

    private fun sendCallTypeMsg(
        item: GiftInfo,
        sendGiftRequest: SendGiftRequest
    ) {
        val map = HashMap<String, Any>()
        map["giftName"] = item.name
        map["giftIcon"] = item.giftIcon
        map["num"] = sendGiftRequest.num
        map["svga"] = item.svga
        if (source == SendGiftRequest.SOURCE_CALL) {
            EmMsgManager.sendCmdMessagePeople(
                targetUserInfo.userId,
                ChatConstant.ACTION_PHONE_SEND_GIFT,
                map,
            )
        }
    }

    private fun showRechargePop(isDismiss: Boolean = false) {
        activity?.apply {
            val balanceRose = binding?.tvRoseNum?.text.toString()
            if (CommonUtils.isEmpty(balanceRose)) {
                return
            }
            ApplicationProxy.instance.showRechargePop(
                this,
                true,
                balanceRose = BigDecimal(balanceRose)
            )
            if (isDismiss) {
                dismiss()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        logcom("礼物弹框销毁")
    }

    fun hideFriendBtn() {
        binding?.tvAddFriend?.visibility = View.INVISIBLE
    }

    fun refreshBalanceRose(balanceRose: BigDecimal) {
        binding?.tvRoseNum?.text = balanceRose.toPlainString()
    }

    interface OnSendGiftListener {
        fun onSendGift(item: GiftInfo, isCombo: Boolean)
        fun onShowUserInfo(userId: String) {}
        fun onAddFriend() {}
        fun onShowFriendBtn() {}
        fun onSendAll(item: GiftInfo, balance: String) {}
    }

    companion object {
        const val SOURCE_VIDEO = 9
        const val SOURCE_CHAT = 0

        @JvmStatic
        fun showDialog(
            context: FragmentActivity,
            targetUserInfo: UserDetailInfo,
            source: Int,
            callId: Int,
            onSendGiftListener: OnSendGiftListener,
            isShowContinueClick: Boolean = false,
            balanceRose: BigDecimal? = null,
            randomBoxMaxNum: Int = 1
        ): SendGiftPop {
            val createGroupPop =
                SendGiftPop()
            val arguments = Bundle()
            arguments.putInt("source", source)
            arguments.putInt("callId", callId)
            if (balanceRose != null) {
                arguments.putString("balanceRose", balanceRose.toPlainString())
            }
            arguments.putInt("randomBoxMaxNum", randomBoxMaxNum)
            arguments.putBoolean("isShowContinueClick", isShowContinueClick)
            arguments.putSerializable(IntentKeyConfig.DATA, targetUserInfo)
            createGroupPop.onSendGiftListener = onSendGiftListener
            createGroupPop.arguments = arguments
            createGroupPop.showNow(context.supportFragmentManager, "sendGiftPop")
            return createGroupPop
        }
    }
}