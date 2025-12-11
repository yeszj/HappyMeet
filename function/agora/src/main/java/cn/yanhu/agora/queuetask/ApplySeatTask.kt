package cn.yanhu.agora.queuetask

import cn.yanhu.agora.pop.UserApplySeatPop
import cn.yanhu.baselib.queue.BaseQueueTask
import com.blankj.utilcode.util.ActivityUtils
import com.hyphenate.chat.EMMessage
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback

/**
 * @author: zhengjun
 * created: 2023/5/29
 */
class ApplySeatTask(private val it: EMMessage,val roomId:String,val roomType:Int): BaseQueueTask() {
    override fun doTask() {
        val topActivity = ActivityUtils.getTopActivity()
        UserApplySeatPop.showDialog(topActivity,it,roomId,roomType,object : SimpleCallback() {
            override fun onDismiss(popupView: BasePopupView?) {
                doNextTask()
            }
        })
    }
    override fun finishTask() {

    }
}