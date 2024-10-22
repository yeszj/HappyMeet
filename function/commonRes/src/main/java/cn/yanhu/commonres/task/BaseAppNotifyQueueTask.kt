package cn.yanhu.commonres.task

import cn.yanhu.baselib.queue.BaseQueueTask
import cn.yanhu.commonres.bean.AppMsgNotifyInfo
import com.lxj.xpopup.core.BasePopupView

/**
 * @author: zhengjun
 * created: 2024/5/20
 * desc:
 */
open class BaseAppNotifyQueueTask(val appMsgNotifyInfo: AppMsgNotifyInfo): BaseQueueTask() {
     var currentPop: BasePopupView?=null
    override fun doTask() {

    }


    override fun getTaskName(): String {
        return "userId="+appMsgNotifyInfo.userId+"msgType="+appMsgNotifyInfo.msgType
    }

    override fun getDuration(): Long {
        return 3000L
    }

    override fun finishTask() {
        currentPop?.dismiss()
    }
}