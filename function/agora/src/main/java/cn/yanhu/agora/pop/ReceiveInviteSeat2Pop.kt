package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.PopReceiveInviteSeat2Binding
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.manager.AppCacheManager
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.VibrateUtils
import com.hyphenate.chat.EMMessage
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView

/**
 * @author: zhengjun
 * created: 2024/10/16
 * desc:
 */
@SuppressLint("ViewConstructor")
class ReceiveInviteSeat2Pop(context: Context, val it: EMMessage, private val onClickSeatUpListener: OnClickSeatUpListener) : CenterPopupView(context) {
    private lateinit var mBiding: PopReceiveInviteSeat2Binding
    override fun getImplLayoutId(): Int {
        return R.layout.pop_receive_invite_seat2
    }

    override fun onCreate() {
        super.onCreate()
        mBiding = PopReceiveInviteSeat2Binding.bind(popupImplView)
        val roomInfo = it.getStringAttribute("roomInfo", "")
        val fromJson = GsonUtils.fromJson(roomInfo, RoomDetailInfo::class.java)
        mBiding.roomBean = fromJson
        mBiding.executePendingBindings()
        VibrateUtils.vibrate(1000L)
        mBiding.tvAgree.setOnSingleClickListener {
            onClickSeatUpListener.onClickSeatUp()
            dismiss()
        }
        mBiding.tvRefuse.setOnSingleClickListener {
            dismiss()
        }
        mBiding.ivClose.setOnSingleClickListener { dismiss() }
        if (AppCacheManager.isWoman()){
            mBiding.tvRoseNum.visibility = View.GONE
        }else{
            mBiding.tvRoseNum.visibility = View.VISIBLE
        }
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context, it: EMMessage,onClickSeatUpListener: OnClickSeatUpListener
        ): ReceiveInviteSeat2Pop {
            val matchPop = ReceiveInviteSeat2Pop(mContext, it,onClickSeatUpListener)
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