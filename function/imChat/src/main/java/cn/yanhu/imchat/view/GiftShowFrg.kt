package cn.yanhu.imchat.view

import android.os.Bundle
import android.text.TextUtils
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.SendGiftRequest
import cn.yanhu.commonres.bean.response.GiftResponse
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.imchat.ImChatViewModel
import cn.yanhu.imchat.databinding.ViewGiftShowBinding
import cn.yanhu.imchat.R
import cn.yanhu.imchat.adapter.SendGiftItemAdapter
import cn.yanhu.imchat.api.imChatRxApi
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.GsonUtils

/**
 * @author: zhengjun
 * created: 2025/7/24
 * desc:
 */
class GiftShowFrg : BaseFragment<ViewGiftShowBinding, ImChatViewModel>(
    R.layout.view_gift_show,
    ImChatViewModel::class.java
) {
    private val giftAdapter by lazy { SendGiftItemAdapter() }
    override fun initData() {
        source = requireArguments().getInt("source", 0)
        type = requireArguments().getInt("type", 0)
        initGiftAdapter()
        getGiftInfo()
    }

    private var giftInfo: GiftResponse? = null
    fun getGiftInfo() {
        request({ imChatRxApi.getGiftList(type,source) }, object : OnRequestResultListener<GiftResponse> {
            override fun onSuccess(data: BaseBean<GiftResponse>) {
                giftInfo = data.data
                removeRandomBoxGift()
                setGiftInfo()
            }

            override fun onFail(code: Int?, msg: String?) {
                super.onFail(code, msg)
            }
        })
    }

    private fun setGiftInfo() {
        giftAdapter.submitList(giftInfo?.list)
        onClickSendListener?.setGiftInfo(giftInfo!!,type)
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
        }
    }

    fun getSelectItem(): GiftInfo? {
        return giftAdapter.getSelectItem()
    }

    private var onClickSendListener: OnClickSendListener? = null
    fun registerClickSendListener(sendListener: OnClickSendListener) {
        onClickSendListener = sendListener
    }

    interface OnClickSendListener {
        fun onSendGift(item: GiftInfo?)
        fun setGiftInfo(giftResponse: GiftResponse,type:Int)
    }

    companion object {
        const val TYPE_GIFT = 1
        const val TYPE_SONG = 11 //点歌礼物
        const val TYPE_FACE = 12 //贴脸
        const val TYPE_LOVER = 14 //情侣
        fun newInstance(source: Int,type: Int): GiftShowFrg{
            val args = Bundle()
            args.putInt("source", source)
            args.putInt("type", type)
            val fragment = GiftShowFrg()
            fragment.arguments = args
            return fragment
        }
    }
}