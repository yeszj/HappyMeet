package cn.yanhu.imchat.pop

import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import cn.yanhu.baselib.adapter.CustomViewPagerAdapter
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.SendGiftRequest
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.bean.response.GiftResponse
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.imchat.R
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.databinding.PopSendGiftBinding
import cn.yanhu.imchat.manager.EmMsgManager
import cn.yanhu.imchat.view.GiftShowView
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ErrorCode
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.VibrateUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import java.math.BigDecimal

/**
 * @author: zhengjun
 * created: 2024/10/11
 * desc:
 */
@SuppressLint("ViewConstructor")
class SendGiftPop(
    val context: FragmentActivity,
    private var sendUserInfo: UserDetailInfo,
    private val source: Int,
    private val callId: Int,
    val onSendGiftListener: OnSendGiftListener
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_send_gift
    }

    private lateinit var mBinding: PopSendGiftBinding
    private var giftInfo: GiftResponse? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = PopSendGiftBinding.bind(popupImplView)
        mBinding.userInfo = sendUserInfo
        mBinding.executePendingBindings()

        ThreadUtils.getMainHandler().postDelayed({
            initTabLayout()
        },20)
        mBinding.tvAddFriend.setOnSingleClickListener {
            onSendGiftListener.onAddFriend()
        }
        mBinding.tvRecharge.setOnSingleClickListener {
            ApplicationProxy.instance.showRechargePop(context, true)
        }
        mBinding.tvUserDetail.setOnSingleClickListener {
            onSendGiftListener.onShowUserInfo(sendUserInfo.userId)
        }
        mBinding.ivAvatar.setOnSingleClickListener {
            onSendGiftListener.onShowUserInfo(sendUserInfo.userId)
        }
        LiveEventBus.get<Boolean>(LiveDataEventManager.PAY_RESULT).observe(context) {
            if (it) {
                giftViewsList[0].getGiftInfo()
            }
        }
    }

    fun showAddFriendsBtn(userInfo: UserDetailInfo){
        this.sendUserInfo = userInfo
        if (sendUserInfo.isFriend || (sendUserInfo.isSameGender && AppCacheManager.isMan())){
            mBinding.tvAddFriend.visibility = View.INVISIBLE
        }else{
            mBinding.tvAddFriend.visibility = View.VISIBLE
        }
    }

    private var sendGiftListener = object : GiftShowView.OnClickSendListener{
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

    private val giftViewsList = mutableListOf<GiftShowView>()
    private fun initTabLayout() {
        mBinding.apply {
            val giftShowView = GiftShowView(context,source,GiftShowView.TYPE_GIFT)
            giftShowView.registerClickSendListener(sendGiftListener)
            giftViewsList.add(giftShowView)
            if (SendGiftRequest.SOURCE_LIVE_ROOM== source){
                val faceGiftShowView = GiftShowView(context,source,GiftShowView.TYPE_FACE)
                giftViewsList.add(faceGiftShowView)
                faceGiftShowView.registerClickSendListener(sendGiftListener)
                if (!sendUserInfo.isSameGender){
                    val loversGiftShowView = GiftShowView(context,source,GiftShowView.TYPE_LOVER)
                    giftViewsList.add(loversGiftShowView)
                    loversGiftShowView.registerClickSendListener(sendGiftListener)
                }else{
                    mBinding.tvLovers.visibility = View.INVISIBLE
                }
            }else{
                mBinding.tvFace.visibility = View.INVISIBLE
                mBinding.tvLovers.visibility = View.INVISIBLE
            }

            viewPager.adapter = CustomViewPagerAdapter(giftViewsList)
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
            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }
                override fun onPageSelected(position: Int) {
                    mBinding.tabLayout.check(mBinding.tabLayout.getChildAt(position).id)
                }
                override fun onPageScrollStateChanged(state: Int) {
                }
            })
        }
    }

    private fun setCurrentItem(position: Int) {
        mBinding.viewPager.setCurrentItem(position, true)
    }

    private fun setGiftInfo() {
        giftInfo?.apply {
            mBinding.tvRoseNum.text = this.roseNum.toPlainString()
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
        request2({ imChatRxApi.sendGift(sendGiftRequest) },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    giftInfo?.roseNum = BigDecimal(
                        CommonUtils.subString(
                            giftInfo!!.roseNum.toPlainString(),
                            item.price.toString()
                        )
                    )
                    mBinding.tvRoseNum.text = giftInfo!!.roseNum.toPlainString()
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
                    onSendGiftListener.onSendGift(item)
                }

                override fun onFail(code: Int?, msg: String?) {
                    super.onFail(code, msg)
                    if (code == ErrorCode.CODE_NO_BALANCE) {
                        ApplicationProxy.instance.showRechargePop(context, true)
                        dismiss()
                    }
                }
            })
    }

    override fun doAfterShow() {
        super.doAfterShow()
        if (SendGiftRequest.SOURCE_LIVE_ROOM == source){
            onSendGiftListener.onShowFriendBtn()
        }
    }

    fun hideFriendBtn() {
        mBinding.tvAddFriend.visibility = View.INVISIBLE
    }

    interface OnSendGiftListener {
        fun onSendGift(item: GiftInfo)
        fun onShowUserInfo(userId: String) {}
        fun onAddFriend() {}
        fun onShowFriendBtn(){}
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
                SendGiftPop(context, sendUserInfo, source, callId, onSendGiftListener)
            val builder = XPopup.Builder(context)
            builder
                .asCustom(createGroupPop).show()
            return createGroupPop
        }
    }
}