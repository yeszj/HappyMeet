package cn.huanyuan.sweetlove.func.task

import android.app.Activity
import cn.huanyuan.sweetlove.bean.GlobalGiftBean
import cn.huanyuan.sweetlove.func.dialog.GlobalGiftPop
import cn.yanhu.baselib.queue.BaseQueueTask
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.pop.TeenTipPop
import cn.yanhu.commonres.task.AppPopTypeManager
import com.blankj.utilcode.util.ActivityUtils
import com.google.gson.Gson
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback

/**
 * @author: zhengjun
 * created: 2024/10/28
 * desc:
 */
class AppPopTask(val type: Int, val info: String) : BaseQueueTask() {
    companion object {
        var currentPopTask: AppPopTask? = null
    }
    override fun doTask() {
        currentPopTask = this
        val topActivity = ActivityUtils.getTopActivity()
        when (type) {
            AppPopTypeManager.TYPE_TEE_POP -> {
                showTeenTipPop(topActivity)
            }
            ChatConstant.GLOBAL_GIFT_ALERT -> {
                //礼物飘屏
                showGiftGlobal(topActivity)
            }
        }
    }

    private fun showGiftGlobal(topActivity: Activity?) {
        topActivity?.apply {
            val globalGiftBean = Gson().fromJson(
                info, GlobalGiftBean::class.java
            )
            GlobalGiftPop.showDialog(topActivity, globalGiftBean, dismissCallBack)
        }
    }

    private var teenTipPop: TeenTipPop? = null
    private fun showTeenTipPop(topActivity: Activity) {
        if (CommonUtils.isPopShow(teenTipPop)) {
            return
        }
        teenTipPop = TeenTipPop.showDialog(topActivity, dismissCallBack)
    }

    private val dismissCallBack = object : SimpleCallback() {
        override fun onDismiss(popupView: BasePopupView?) {
            super.onDismiss(popupView)
            doNextTask()
        }
    }
}