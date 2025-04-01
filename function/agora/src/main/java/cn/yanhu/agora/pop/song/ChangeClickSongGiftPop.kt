package cn.yanhu.agora.pop.song

import android.annotation.SuppressLint
import android.content.Context
import com.lxj.xpopup.core.BottomPopupView
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.GiftSelectItemAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.databinding.PopChangeClickSongGiftBinding
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.response.GiftResponse
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.view.GiftShowView
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2025/3/25
 * desc:
 */
@SuppressLint("ViewConstructor")
class ChangeClickSongGiftPop(context: Context,val roomId:String) :
    BottomPopupView(context) {
    private val selectGiftAdapter by lazy { GiftSelectItemAdapter() }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,roomId:String
        ): ChangeClickSongGiftPop {
            val matchPop = ChangeClickSongGiftPop(mContext,roomId)
            val builder = XPopup.Builder(mContext)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }

    override fun getImplLayoutId(): Int {
        return R.layout.pop_change_click_song_gift
    }

    private lateinit var mBinding: PopChangeClickSongGiftBinding
    private var giftInfo: GiftResponse? = null
    override fun onCreate() {
        super.onCreate()
        mBinding = PopChangeClickSongGiftBinding.bind(popupImplView)
        mBinding.rvGift.adapter = selectGiftAdapter
        selectGiftAdapter.setOnItemClickListener { _, _, position ->
            selectGiftAdapter.setSelectPosition(
                position
            )
            mBinding.rvGift.scrollToPosition(position)
        }
        getGiftInfo()
        mBinding.tvChange.setOnSingleClickListener {
            changeSongGift()
        }
    }

    private fun changeSongGift() {
        request({
            agoraRxApi.changeSongGift(
                roomId,
                selectGiftAdapter.getSelectItem()!!.id.toString()
            )
        }, object : OnRequestResultListener<String> {
            override fun onSuccess(data: BaseBean<String>) {
                showToast("更换成功")
                dismiss()
            }
        })
    }

    private fun getGiftInfo() {
        request({ imChatRxApi.getGiftList(GiftShowView.TYPE_SONG) }, object : OnRequestResultListener<GiftResponse> {
            override fun onSuccess(data: BaseBean<GiftResponse>) {
                giftInfo = data.data
                setGiftInfo()
            }
        })
    }

    private fun setGiftInfo() {
        selectGiftAdapter.submitList(giftInfo!!.list)
    }

}