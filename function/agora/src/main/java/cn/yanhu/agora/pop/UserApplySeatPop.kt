package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.content.Context
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.PopUserApplySeatBinding
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.TextViewDrawableUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.imchat.manager.EmMsgManager
import com.blankj.utilcode.util.VibrateUtils
import com.hyphenate.chat.EMMessage
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.interfaces.XPopupCallback

/**
 * @author: zhengjun
 * created: 2024/10/16
 * desc:
 */
@SuppressLint("ViewConstructor")
class UserApplySeatPop(context: Context, val it: EMMessage, val roomID:String,val roomType:Int) : BottomPopupView(context) {
    private lateinit var mBiding: PopUserApplySeatBinding
    override fun getImplLayoutId(): Int {
        return R.layout.pop_user_apply_seat
    }

    private var isClosePopTip = false
    override fun onCreate() {
        super.onCreate()
        mBiding = PopUserApplySeatBinding.bind(popupImplView)
        val fromUid: String = it.getStringAttribute("fromUid","")
        val fromNickName: String = it.getStringAttribute("fromNickName","")
        val seatId: String = it.getStringAttribute("seatId","")
        val portrait: String = it.getStringAttribute("portrait","")
        VibrateUtils.vibrate(1000L)
        GlideUtils.load(context, portrait, mBiding.ivAvatar)
        mBiding.tvNickName.text = fromNickName
        mBiding.tvAgree.setOnSingleClickListener {
            EmMsgManager.sendAgreeSeatApplyMsg(fromUid,fromNickName,roomID,seatId)
            dismiss()
        }
        mBiding.tvRefuse.setOnSingleClickListener {
            dismiss()
        }
        if (RoomListBean.isThreeRoom(roomType)){
            mBiding.tvTip.visibility = INVISIBLE
        }else{
            mBiding.tvTip.visibility = VISIBLE
        }
        mBiding.tvTip.setOnSingleClickListener {
            isClosePopTip = !isClosePopTip
            if (isClosePopTip){
                TextViewDrawableUtils.setDrawableLeft(context,mBiding.tvTip, cn.yanhu.commonres.R.drawable.svg_check_select)
            }else{
                TextViewDrawableUtils.setDrawableLeft(context,mBiding.tvTip, cn.yanhu.commonres.R.drawable.svg_check_normal)
            }
            LiveEventBus.get<Boolean>(EventBusKeyConfig.CHANGEAPPLYPOPSTATUS).post(isClosePopTip)
        }
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context, it: EMMessage,roomId:String,roomType:Int,xPopupCallback: XPopupCallback
        ): UserApplySeatPop {
            val matchPop = UserApplySeatPop(mContext, it,roomId,roomType)
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