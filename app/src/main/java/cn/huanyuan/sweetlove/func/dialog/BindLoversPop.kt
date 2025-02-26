package cn.huanyuan.sweetlove.func.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.lxj.xpopup.core.BottomPopupView
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.PopBindLoversBinding
import cn.huanyuan.sweetlove.ui.userinfo.adapter.LoversBindGiftAdapter
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.GiftInfo
import com.lxj.xpopup.XPopup

/**
 * @author: zhengjun
 * created: 2025/2/18
 * desc:
 */
@SuppressLint("ViewConstructor")
class BindLoversPop(context: Context, val list: MutableList<GiftInfo>, private val onSendGiftListener: OnSendListener) : BottomPopupView(context) {

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Activity,
            list: MutableList<GiftInfo>,
            onSendGiftListener: OnSendListener
        ): BindLoversPop {
            val sexPop = BindLoversPop(mContext, list,onSendGiftListener)
            val builder = XPopup.Builder(mContext)
            builder
                .isDestroyOnDismiss(true).asCustom(sexPop).show()
            return sexPop
        }
    }
    override fun getImplLayoutId(): Int {
        return R.layout.pop_bind_lovers
    }

    private lateinit var mBinding: PopBindLoversBinding
    private val giftAdapter by lazy { LoversBindGiftAdapter() }
    override fun onCreate() {
        super.onCreate()
        mBinding = PopBindLoversBinding.bind(popupImplView)
        mBinding.rvGift.adapter = giftAdapter
        giftAdapter.submitList(list)
        giftAdapter.setOnItemClickListener { _, _, position ->
            giftAdapter.setSelectPosition(
                position
            )
        }
        mBinding.tvSend.setOnSingleClickListener {
            val selectItem = giftAdapter.getSelectItem()
            selectItem?.apply {
                onSendGiftListener.onSendGift(this)
            }
        }
    }

    interface OnSendListener{
        fun onSendGift(giftInfo: GiftInfo)
    }
}