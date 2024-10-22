package cn.yanhu.agora.pop

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.AppMsgNotifyInfo
import com.blankj.utilcode.util.ScreenUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.XPopupCallback
import cn.yanhu.imchat.R
import cn.yanhu.imchat.databinding.PopImChatMsgBinding

/**
 * @author: zhengjun
 * created: 2024/5/17
 * desc:
 */
@SuppressLint("ViewConstructor")
class ImChatMsgPop(context: Context, appMsgNotifyInfo: AppMsgNotifyInfo) : BasePositionPopupView(context,appMsgNotifyInfo) {
    override fun getImplLayoutId(): Int {
        return R.layout.pop_im_chat_msg
    }

    override fun onCreate() {
        super.onCreate()
        val bind = PopImChatMsgBinding.bind(popupImplView)
        bind.appMsgInfo = appMsgNotifyInfo
        bind.vgParent.setOnSingleClickListener {
            toPrivacyChat()
            dismiss()
        }
        bind.executePendingBindings()
    }

    override fun onShow() {
        super.onShow()
//        if (!AppCacheManager.hasCloseSound){
//            AppAudioManager.playRing(context,R.raw.angel_end)
//        }
//        if (!AppCacheManager.hasCloseShake){
//            VibrateUtils.vibrate(100)
//        }
    }

    override fun doAfterDismiss() {
        super.doAfterDismiss()
//        AppAudioManager.clearMediaPlay()
//        VibrateUtils.cancel()
    }


    companion object {
        @JvmStatic
        fun showDialog(
            mContext: Activity,
            appMsgNotifyInfo: AppMsgNotifyInfo,
            xPopupCallback: XPopupCallback
        ): ImChatMsgPop {
            val readMsgTipPop = ImChatMsgPop(mContext, appMsgNotifyInfo)
            val builder = XPopup.Builder(mContext)
            builder.dismissOnTouchOutside(false)
                .dismissOnBackPressed(false)
                .maxWidth(ScreenUtils.getScreenWidth())
                .enableDrag(true)
                .hasShadowBg(false)
                .isRequestFocus(false)
                .isViewMode(true)
                .isClickThrough(true)
                .isTouchThrough(true)
                .setPopupCallback(xPopupCallback)
                .isDestroyOnDismiss(true).asCustom(readMsgTipPop).show()
            return readMsgTipPop
        }
    }
}