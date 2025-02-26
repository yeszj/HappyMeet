package cn.yanhu.agora.queuetask

import cn.yanhu.agora.manager.BeautySetManager
import cn.yanhu.baselib.queue.BaseQueueTask
import com.blankj.utilcode.util.ThreadUtils

/**
 * @author: zhengjun
 * created: 2023/5/29
 */
class FaceEffectTask( private val faceEffectInfo:String): BaseQueueTask() {
    override fun doTask() {
        try {
            BeautySetManager.getInstance().openFaceEffect(faceEffectInfo)
            ThreadUtils.getMainHandler().postDelayed({
                BeautySetManager.getInstance().closeFaceEffect(faceEffectInfo)
                doNextTask()
            }, 8000)
        }catch (e:Exception){
            e.printStackTrace()
        }

    }
    override fun finishTask() {

    }
}