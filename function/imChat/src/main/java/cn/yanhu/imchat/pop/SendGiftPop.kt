package cn.yanhu.imchat.pop

import android.annotation.SuppressLint
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
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
import cn.yanhu.imchat.adapter.SendGiftItemAdapter
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.databinding.PopSendGiftBinding
import cn.yanhu.imchat.manager.EmMsgManager
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ErrorCode
import com.blankj.utilcode.util.GsonUtils
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
    private val sendUserInfo: UserDetailInfo,
    private val source: Int,
    private val callId: Int,
    val onSendGiftListener: OnSendGiftListener
) : BottomPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_send_gift
    }

    private val giftAdapter by lazy { SendGiftItemAdapter() }
    private lateinit var mBinding: PopSendGiftBinding
    private var giftInfo: GiftResponse? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = PopSendGiftBinding.bind(popupImplView)
        mBinding.userInfo = sendUserInfo
        mBinding.executePendingBindings()
        initGiftAdapter()
        if (!TextUtils.isEmpty(AppCacheManager.giftInfo)) {
            giftInfo = GsonUtils.fromJson(AppCacheManager.giftInfo, GiftResponse::class.java)
            removeRandomBoxGift()
            setGiftInfo()
        }
        mBinding.tvRecharge.setOnSingleClickListener {
            ApplicationProxy.instance.showRechargePop(context, true)
        }
        mBinding.tvUserDetail.setOnSingleClickListener {
            onSendGiftListener.onShowUserInfo(sendUserInfo.userId)
        }
        LiveEventBus.get<Boolean>(LiveDataEventManager.PAY_RESULT).observe(context) {
            if (it) {
                getGiftInfo()
            }
        }
        getGiftInfo()
    }

    private fun removeRandomBoxGift() {
        //如果不是直播间 移除随机盲盒礼物
        if (SendGiftRequest.SOURCE_LIVE_ROOM != source) {
            giftInfo?.list?.removeIf {
                it.type == GiftInfo.TYPE_RANDOM_BOX
            }
        }
    }

    private fun getGiftInfo() {
        request({ imChatRxApi.getGiftList() }, object : OnRequestResultListener<GiftResponse> {
            override fun onSuccess(data: BaseBean<GiftResponse>) {
                giftInfo = data.data
                removeRandomBoxGift()
                setGiftInfo()
            }
        })
    }

    private fun setGiftInfo() {
        giftInfo?.apply {
            mBinding.tvRoseNum.text = this.roseNum.toPlainString()
            giftAdapter.submitList(this.list)
        }
    }

    private fun initGiftAdapter() {
        mBinding.rvGift.adapter = giftAdapter
        giftAdapter.setOnItemClickListener { _, _, position ->
            if (position == giftAdapter.getSelectPosition()) {
                startSendGift(position)
            } else {
                giftAdapter.setSelectPosition(
                    position
                )
                mBinding.rvGift.scrollToPosition(position)
            }

        }
        giftAdapter.addOnItemChildClickListener(
            R.id.tv_send
        ) { _, _, position -> startSendGift(position) }
    }

    private fun startSendGift(position: Int) {
        val item = giftAdapter.getItem(position) ?: return
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

    interface OnSendGiftListener {
        fun onSendGift(item: GiftInfo)
        fun onShowUserInfo(userId: String) {}
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