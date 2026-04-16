package cn.yanhu.agora.pop.roomPk

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import cn.yanhu.agora.bean.PkInviteMsgInfo
import cn.yanhu.agora.databinding.PopReceivePkInviteBinding
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import com.blankj.utilcode.util.ScreenUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import cn.yanhu.agora.R
import androidx.core.graphics.toColorInt
import cn.yanhu.baselib.utils.CoilImgUtils

/**
 * @author: zhengjun
 * created: 2024/9/10
 * desc:
 */
@SuppressLint("ViewConstructor")
class ReceivePkInvitePop(context: Context, val userInfo: PkInviteMsgInfo,val onPkInviteListener: OnPkInviteListener) :
    CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_receive_pk_invite
    }

    private lateinit var mBinding: PopReceivePkInviteBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = PopReceivePkInviteBinding.bind(popupImplView)
        mBinding.tvNickName.text = userInfo.nickName
        CoilImgUtils.loadCircleImg(userInfo.portrait, mBinding.ivAvatar)
        mBinding.tvRefuse.setOnSingleClickListener {
            onPkInviteListener.rejectPk()
            dismiss()
        }
        mBinding.tvAgree.setOnSingleClickListener {
            onPkInviteListener.agreePk()
            dismiss()
        }
    }

    override fun doAfterShow() {
        super.doAfterShow()
        pkCountDownTime()
    }

    private var pkRandomTimer: CountDownTimer? = null

    //定时邀请弹窗
    private fun pkCountDownTime() {
        pkRandomTimer = object : CountDownTimer(1000L * 15, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }
            override fun onFinish() {
                onPkInviteListener.rejectPk()
                dismiss()
            }
        }
        pkRandomTimer?.start()
    }


    override fun beforeDismiss() {
        super.beforeDismiss()
        pkRandomTimer?.cancel()
    }

    interface OnPkInviteListener {
        fun agreePk()
        fun rejectPk()
    }

    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Context,
            userInfo: PkInviteMsgInfo,onPkInviteListener: OnPkInviteListener
        ): ReceivePkInvitePop {
            val matchPop =
                ReceivePkInvitePop(mContext, userInfo,onPkInviteListener)
            val builder = XPopup.Builder(mContext)
            builder
                .autoOpenSoftInput(false)
                .shadowBgColor("#99000000".toColorInt())
                .dismissOnBackPressed(false)
                .dismissOnTouchOutside(false)
                .maxWidth(ScreenUtils.getScreenWidth())
                .maxHeight(ScreenUtils.getAppScreenHeight())
                .isDestroyOnDismiss(true).asCustom(matchPop).show()
            return matchPop
        }
    }
}