package cn.yanhu.agora.pop

import android.content.Context
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.PopUserApplySeatBinding
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.imchat.manager.EmMsgManager
import com.blankj.utilcode.util.VibrateUtils
import com.hyphenate.chat.EMMessage
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.interfaces.XPopupCallback

/**
 * @author: zhengjun
 * created: 2024/10/16
 * desc:
 */
class UserApplySeatPop(context: Context, val it: EMMessage,val roomID:String) : BottomPopupView(context) {
    private lateinit var mBiding: PopUserApplySeatBinding
    override fun getImplLayoutId(): Int {
        return R.layout.pop_user_apply_seat
    }

    override fun onCreate() {
        super.onCreate()
        mBiding = PopUserApplySeatBinding.bind(popupImplView)
        val fromUid: String = it.getStringAttribute("fromUid")
        val fromNickName: String = it.getStringAttribute("fromNickName")
        val seatId: String = it.getStringAttribute("seatId")
        val portrait: String = it.getStringAttribute("portrait")
        VibrateUtils.vibrate(1000L)
        GlideUtils.load(context, portrait, mBiding.ivAvatar)
        mBiding.tvNickName.text = fromNickName
        mBiding.tvAgree.setOnSingleClickListener {
            val map = HashMap<String, Any>()
            map["ownerNickname"] = fromNickName
            map["roomId"] = roomID
            map["seatId"] =seatId
            EmMsgManager.sendCmdMessagePeople(
                fromUid,
                ChatConstant.ACTION_MSG_APPLY_SET_UP_SUCCESS,
                map,

            )
            dismiss()
        }
        mBiding.tvRefuse.setOnSingleClickListener {
            dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context, it: EMMessage,roomId:String,xPopupCallback: XPopupCallback
        ): UserApplySeatPop {
            val matchPop = UserApplySeatPop(mContext, it,roomId)
            val builder =
                XPopup.Builder(mContext)
                    .setPopupCallback(xPopupCallback)
                    .dismissOnBackPressed(false)
                    .dismissOnTouchOutside(false)
            builder.asCustom(matchPop).show()
            return matchPop
        }
    }
}