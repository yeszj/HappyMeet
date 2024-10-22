package cn.yanhu.commonres.task

import cn.yanhu.baselib.queue.BaseQueueTask
import cn.yanhu.commonres.pop.SvgaImageAnimPop
import com.blankj.utilcode.util.ActivityUtils
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback

/**
 * @author: zhengjun
 * created: 2023/5/29
 * desc:svga动画播放队列实现
 */
class GiftPopAnimTask( private val url: String): BaseQueueTask() {
    override fun doTask() {
        val topActivity = ActivityUtils.getTopActivity()
        SvgaImageAnimPop.showDialog(topActivity,url,object : SimpleCallback() {
            override fun onDismiss(popupView: BasePopupView?) {
                doNextTask()
            }
        })
    }

    override fun finishTask() {

    }
}