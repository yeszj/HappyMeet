package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.PopReceiveInviteSeatBinding
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import com.blankj.utilcode.util.VibrateUtils
import com.hyphenate.chat.EMMessage
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/**
 * @author: zhengjun
 * created: 2024/10/16
 * desc:
 */
@SuppressLint("ViewConstructor")
class ReceiveInviteSeatPop(context: Context, val it: EMMessage, private val onClickSeatUpListener: OnClickSeatUpListener) : BottomPopupView(context) {
    private lateinit var mBiding: PopReceiveInviteSeatBinding
    override fun getImplLayoutId(): Int {
        return R.layout.pop_receive_invite_seat
    }

    override fun onCreate() {
        super.onCreate()
        mBiding = PopReceiveInviteSeatBinding.bind(popupImplView)
        val fromNickName: String = it.getStringAttribute("fromNickName")
        val portrait: String = it.getStringAttribute("portrait")
        VibrateUtils.vibrate(1000L)
        GlideUtils.load(context, portrait, mBiding.ivAvatar)
        mBiding.tvNickName.text = fromNickName
        mBiding.tvAgree.setOnSingleClickListener {
            onClickSeatUpListener.onClickSeatUp()
            dismiss()
        }
        mBiding.tvRefuse.setOnSingleClickListener {
            dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context, it: EMMessage,onClickSeatUpListener: OnClickSeatUpListener
        ): ReceiveInviteSeatPop {
            val matchPop = ReceiveInviteSeatPop(mContext, it,onClickSeatUpListener)
            val builder =
                XPopup.Builder(mContext)
                    .dismissOnBackPressed(false)
                    .dismissOnTouchOutside(false)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }
    interface OnClickSeatUpListener{
        fun onClickSeatUp()
    }
}