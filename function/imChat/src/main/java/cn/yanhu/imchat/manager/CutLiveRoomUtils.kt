package cn.yanhu.imchat.manager

import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.zj.netrequest.application.ApplicationProxy
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ThreadUtils
import com.lxj.xpopup.core.BasePopupView

object CutLiveRoomUtils {
    private var msgDialog: BasePopupView?=null
    @JvmStatic
    fun showChangeAlert(changeListener: ChangeListener) {
        showChangeAlert(changeListener,"你确定离开当前房间吗？")
    }

    fun isShow():Boolean{
        return CommonUtils.isPopShow(msgDialog)
    }

    @JvmStatic
    fun showChangeAlert(changeListener: ChangeListener, msg: String) {
        val topActivity = ActivityUtils.getTopActivity()

        if ( topActivity==null || isShow()){
            return
        }
        msgDialog =  DialogUtils.showConfirmDialog("",{
            DialogUtils.showLoading()
            ApplicationProxy.instance.getLiveRoomActivity()?.finish()
            ThreadUtils.getMainHandler().postDelayed({
                changeListener.sure()
                DialogUtils.dismissLoading()
            }, 500)
        },{

        },msg,cancel = "取消",confirm = "确定离开")
        msgDialog?.show()
    }

    interface ChangeListener {
        fun sure()
    }
}