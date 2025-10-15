package cn.yanhu.agora.queuetask

import cn.yanhu.baselib.queue.BaseQueueTask
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.imchat.manager.EmMsgManager
import com.blankj.utilcode.util.ThreadUtils
import com.hyphenate.EMCallBack

/**
 * @author: zhengjun
 * created: 2023/5/29
 */
class SendCmdTask(private val uid: String, val content: String, val action: Int) : BaseQueueTask() {

    override fun doTask() {
        val currentTime = System.currentTimeMillis()
        var timeSinceLastCmd = currentTime - lastCmdSendTime
        val differTime = if (ChatConstant.ACTION_SEND_ROSE==action){
            500
        }else{
            5000
        }
        timeSinceLastCmd = if (timeSinceLastCmd < differTime) {
            1000
        } else {
            0
        }
        ThreadUtils.getMainHandler().postDelayed({
            logcom("startSendComboGift", "开始发送送礼透传")
            lastCmdSendTime = currentTime
            EmMsgManager.sendCmdMessageToChatRoom(
                uid, content, action, callBack = object : EMCallBack {
                    override fun onSuccess() {
                        logcom("startSendComboGift", "发送送礼透传成功")
                        doNextTask()
                    }

                    override fun onError(code: Int, error: String?) {
                        logcom("startSendComboGift", "发送送礼透传失败:${error}")
                        doNextTask()
                    }
                }
            )
        }, timeSinceLastCmd)

    }

    override fun finishTask() {

    }

    companion object {
        var lastCmdSendTime: Long = 0
    }
}