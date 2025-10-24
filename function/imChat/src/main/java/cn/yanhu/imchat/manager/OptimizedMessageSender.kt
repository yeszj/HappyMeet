package cn.yanhu.imchat.manager

import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * @author: zhengjun
 * created: 2025/10/17
 * desc:
 */
object OptimizedMessageSender {
    private val concurrentSendExecutor: ExecutorService = Executors.newFixedThreadPool(15) // 并发数量可配置
    fun sendMessageConcurrent(msg: EMMessage?) {
        concurrentSendExecutor.submit(Runnable {
            try {
                // 直接调用环信的发送方法，但每个消息在独立的线程中执行
                EMClient.getInstance().chatManager().sendMessage(msg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }
}