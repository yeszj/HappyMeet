package cn.yanhu.imchat.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.SendGiftRequest
import cn.yanhu.commonres.bean.response.GiftResponse
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.imchat.R
import cn.yanhu.imchat.adapter.SendGiftItemAdapter
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.databinding.ViewGiftShowBinding
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.GsonUtils

/**
 * @author: zhengjun
 * created: 2025/2/12
 * desc:
 */
class GiftShowView : LinearLayout {
    constructor(context: Context,source: Int,type: Int) : super(context) {
        this.source = source
        this.type = type
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
        initView(context)
    }

    private lateinit var mBinding: ViewGiftShowBinding
    private val giftAdapter by lazy { SendGiftItemAdapter() }

    @SuppressLint("Recycle")
    private fun initView(context: Context) {
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.view_gift_show, this, true
        )
        initGiftAdapter()
        getGiftInfo()
    }


    private var giftInfo: GiftResponse? = null
    fun getGiftInfo() {
        request({ imChatRxApi.getGiftList(type) }, object : OnRequestResultListener<GiftResponse> {
            override fun onSuccess(data: BaseBean<GiftResponse>) {
                giftInfo = data.data
                removeRandomBoxGift()
                setGiftInfo()
            }
        })
    }

    private fun setGiftInfo() {
        giftAdapter.submitList(giftInfo?.list)
        onClickSendListener?.setGiftInfo(giftInfo!!)
    }

    private var source: Int = 0
    private var type:Int = TYPE_GIFT

    private fun removeRandomBoxGift() {
        //如果不是直播间 移除随机盲盒礼物
        if (SendGiftRequest.SOURCE_LIVE_ROOM != source) {
            giftInfo?.list?.removeIf {
                it.type == GiftInfo.TYPE_RANDOM_BOX
            }
        }
    }

    private fun initGiftAdapter() {
        mBinding.rvGift.adapter = giftAdapter
        giftAdapter.setOnItemClickListener { _, _, position ->
            if (position == giftAdapter.getSelectPosition()) {
                onClickSendListener?.onSendGift(giftAdapter.getItem(position))
            } else {
                giftAdapter.setSelectPosition(
                    position
                )
                mBinding.rvGift.scrollToPosition(position)
            }

        }
        giftAdapter.addOnItemChildClickListener(
            R.id.tv_send
        ) { _, _, position ->
            onClickSendListener?.onSendGift(giftAdapter.getItem(position))
        }
        if (!TextUtils.isEmpty(AppCacheManager.giftInfo) && type == TYPE_GIFT) {
            giftInfo = GsonUtils.fromJson(AppCacheManager.giftInfo, GiftResponse::class.java)
            removeRandomBoxGift()
            setGiftInfo()
        }
    }

    private var onClickSendListener: OnClickSendListener? = null
    fun registerClickSendListener(sendListener: OnClickSendListener) {
        onClickSendListener = sendListener
    }

    interface OnClickSendListener {
        fun onSendGift(item: GiftInfo?)
        fun setGiftInfo(giftResponse: GiftResponse)
    }

    companion object {
        const val TYPE_GIFT = 1
        const val TYPE_FACE = 12
    }

}