package cn.huanyuan.sweetlove.func.dialog

import android.annotation.SuppressLint
import android.content.Context
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.databinding.DialogDeviceChangeAuthTipBinding
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.manager.LiveDataEventManager
import cn.yanhu.commonres.router.PageIntentUtil
import cn.zj.netrequest.application.ApplicationProxy
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.interfaces.SimpleCallback

@SuppressLint("ViewConstructor")
class DeviceChangeAuthTipDialog(context: Context, private val onClickAuthListener: OnClickAuthListener) : CenterPopupView(context){
    companion object {
        @JvmStatic
        fun showPop(
            mContext: Context, onCommitAccostListener: OnClickAuthListener,simpleCallback: SimpleCallback
        ): DeviceChangeAuthTipDialog {
            val matchPop = DeviceChangeAuthTipDialog(mContext, onCommitAccostListener)
            val builder = XPopup.Builder(mContext)
            builder
                .isDestroyOnDismiss(true)
                .setPopupCallback(simpleCallback)
                .dismissOnTouchOutside(false)
                .dismissOnBackPressed(false)
                .asCustom(matchPop).show()
            return matchPop
        }
    }
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_device_change_auth_tip
    }

    private lateinit var mBinding: DialogDeviceChangeAuthTipBinding
    override fun onCreate() {
        super.onCreate()
        mBinding = DialogDeviceChangeAuthTipBinding.bind(popupImplView)
        mBinding.divider.setOnSingleClickListener {
            PageIntentUtil.url2Page(context, PageIntentUtil.TYPE_CONTACT_UNION)
        }
        mBinding.btnExit.setOnSingleClickListener {
            ApplicationProxy.instance.loginInvalid()
        }
        mBinding.btnNext.setOnSingleClickListener {
            onClickAuthListener.onAuth()
        }
        LiveEventBus.get<Boolean>(LiveDataEventManager.FACE_RESULT).observe(this) {
            if (it){
                dismiss()
            }
        }
    }
    interface OnClickAuthListener{
        fun onAuth()
    }

}