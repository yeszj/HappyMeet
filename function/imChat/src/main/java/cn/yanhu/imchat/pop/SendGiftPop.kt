package cn.yanhu.imchat.pop

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import cn.yanhu.baselib.adapter.MyFrgFragmentStateAdapter
import cn.yanhu.baselib.base.BaseSheetDialog
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.SendGiftRequest
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
import cn.yanhu.imchat.view.GiftShowFrg
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ErrorCode
import com.blankj.utilcode.util.VibrateUtils
import com.jeremyliao.liveeventbus.LiveEventBus
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
    private var sendUserInfo: UserDetailInfo = UserDetailInfo()
    private var onSendGiftListener: OnSendGiftListener? = null

    @SuppressLint("CommitTransaction", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            sendUserInfo =
                requireArguments().getSerializable(IntentKeyConfig.DATA) as UserDetailInfo
            source = requireArguments().getInt("source")
            callId = requireArguments().getInt("callId")
            this.userInfo = sendUserInfo
            this.executePendingBindings()
            logcom("showGiftPop = show")

            initTabLayout()
            this.tvAddFriend.setOnSingleClickListener {
                onSendGiftListener?.onAddFriend()
            }
            this.tvRecharge.setOnSingleClickListener {
                ApplicationProxy.instance.showRechargePop(requireActivity(), true)
            }
            this.tvSendAll.setOnSingleClickListener {
                val fragment = giftViewsList[viewPager.currentItem] as GiftShowFrg
                val selectItem = fragment.getSelectItem()
                selectItem?.apply {
                    val balanceRose = binding!!.tvRoseNum.text.toString()
                    onSendGiftListener?.onSendAll(selectItem,balanceRose)
                }
            }
            this.ivAvatar.setOnSingleClickListener {
                onSendGiftListener?.onShowUserInfo(sendUserInfo.userId)
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
        this.sendUserInfo = userInfo
        if (binding != null) {
            if (sendUserInfo.isFriend || (sendUserInfo.isSameGender && AppCacheManager.isMan())) {
                binding!!.tvAddFriend.visibility = View.INVISIBLE
            } else {
                binding!!.tvAddFriend.visibility = View.VISIBLE
            }
        }
    }

    private var sendGiftListener = object : GiftShowFrg.OnClickSendListener {
        override fun onSendGift(item: GiftInfo?) {
            item?.apply {
                startSendGift(this)
            }
        }

        override fun setGiftInfo(giftResponse: GiftResponse) {
            giftInfo = giftResponse
            setGiftInfo()
        }
    }

    private val giftViewsList = mutableListOf<Fragment>()
    private fun initTabLayout() {
        binding?.apply {
            val giftShowView = GiftShowFrg.newInstance(source, GiftShowFrg.TYPE_GIFT)
            giftShowView.registerClickSendListener(sendGiftListener)
            giftViewsList.add(giftShowView)
            if (SendGiftRequest.SOURCE_LIVE_ROOM == source) {
                val faceGiftShowView = GiftShowFrg.newInstance(source, GiftShowFrg.TYPE_FACE)
                giftViewsList.add(faceGiftShowView)
                faceGiftShowView.registerClickSendListener(sendGiftListener)
                if (!sendUserInfo.isSameGender) {
                    val loversGiftShowView =
                        GiftShowFrg.newInstance(source, GiftShowFrg.TYPE_LOVER)
                    giftViewsList.add(loversGiftShowView)
                    loversGiftShowView.registerClickSendListener(sendGiftListener)
                } else {
                    tvLovers.visibility = View.INVISIBLE
                }
            } else {
                tvFace.visibility = View.INVISIBLE
                tvLovers.visibility = View.INVISIBLE
            }
            viewPager.offscreenPageLimit = 1
            viewPager.adapter = MyFrgFragmentStateAdapter(this@SendGiftPop, giftViewsList)
            tabLayout.setOnCheckedChangeListener { _, checkedId ->
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
                }
            }
            tabLayout.check(tabLayout.getChildAt(0).id)
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    tabLayout.check(tabLayout.getChildAt(position).id)
                    if (position==2){
                        tvSendAll.visibility = View.GONE
                    }else{
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
            binding?.tvRoseNum?.text = this.roseNum.toPlainString()
        }
    }


    private fun startSendGift(item: GiftInfo) {
        val sendGiftRequest = SendGiftRequest()
        sendGiftRequest.roomId = sendUserInfo.roomId.toString()
        sendGiftRequest.toUid = sendUserInfo.userId
        sendGiftRequest.giftId = item.id
        sendGiftRequest.num = 1
        sendGiftRequest.source = source
        sendGiftRequest.callId = callId
        sendGift(sendGiftRequest, item)

    }

    private fun sendGift(sendGiftRequest: SendGiftRequest, item: GiftInfo) {
        request2(
            { imChatRxApi.sendGift(sendGiftRequest) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    giftInfo?.roseNum = BigDecimal(
                        CommonUtils.subString(
                            giftInfo!!.roseNum.toPlainString(),
                            item.price.toString()
                        )
                    )
                    binding?.tvRoseNum?.text = giftInfo!!.roseNum.toPlainString()
                    showToast("赠送礼物成功")
                    VibrateUtils.vibrate(50)
                    val map = HashMap<String, Any>()
                    map["giftName"] = item.name
                    map["giftIcon"] = item.giftIcon
                    map["num"] = sendGiftRequest.num
                    map["svga"] = item.svga
                    if (source == SendGiftRequest.SOURCE_CALL) {
                        EmMsgManager.sendCmdMessagePeople(
                            sendUserInfo.userId,
                            ChatConstant.ACTION_PHONE_SEND_GIFT,
                            map,
                        )
                    }
                    if (item.type == GiftInfo.TYPE_RANDOM_BOX) {
                        item.randomBoxGiftInfo = data.data
                    }
                    onSendGiftListener?.onSendGift(item)
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    if (code == ErrorCode.CODE_NO_BALANCE) {
                        ApplicationProxy.instance.showRechargePop(requireActivity(), true)
                        dismiss()
                    }
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logcom("礼物弹框销毁")
    }

    fun hideFriendBtn() {
        binding?.tvAddFriend?.visibility = View.INVISIBLE
    }

    interface OnSendGiftListener {
        fun onSendGift(item: GiftInfo)
        fun onShowUserInfo(userId: String) {}
        fun onAddFriend() {}
        fun onShowFriendBtn() {}
        fun onSendAll(item: GiftInfo,balance: String){}
    }

    companion object {
        @JvmStatic
        fun showDialog(
            context: FragmentActivity,
            sendUserInfo: UserDetailInfo,
            source: Int,
            callId: Int,
            onSendGiftListener: OnSendGiftListener

            ): SendGiftPop {
            val createGroupPop =
                SendGiftPop()
            val arguments = Bundle()
            arguments.putInt("source", source)
            arguments.putInt("callId", callId)
            arguments.putSerializable(IntentKeyConfig.DATA, sendUserInfo)
            createGroupPop.onSendGiftListener = onSendGiftListener
            createGroupPop.arguments = arguments
            createGroupPop.showNow(context.supportFragmentManager, "sendGiftPop")
            return createGroupPop
        }
    }
}