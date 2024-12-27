package cn.yanhu.imchat.manager

import android.annotation.SuppressLint
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.imchat.api.imChatRxApi
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.upload.UploadFileClient
import cn.zj.netrequest.upload.UploadFileProgressListener
import com.blankj.utilcode.util.ThreadUtils

/**
 * @author: zhengjun
 * created: 2024/4/18
 * desc:
 */
object SmSdkUtils {
    const val APP_ID = "default"
    const val ORGANIZATION = "Od8oSLocLgb44sRvhSBZ"
    const val PUBLIC_KEY =
        "MIIDLzCCAhegAwIBAgIBMDANBgkqhkiG9w0BAQUFADAyMQswCQYDVQQGEwJDTjELMAkGA1UECwwCU00xFjAUBgNVBAMMDWUuaXNodW1laS5jb20wHhcNMjMwOTA3MDcwMDA2WhcNNDMwOTAyMDcwMDA2WjAyMQswCQYDVQQGEwJDTjELMAkGA1UECwwCU00xFjAUBgNVBAMMDWUuaXNodW1laS5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCBuqWlyMDBEu71WUIdg1Ff+kG10QmoDXvTv1uMMIsJRnPZN3t0QoUc1e9CQp4g1iypbINSHPVSpPmsK11mZAFGG1890L1kuoPU0eZoMi/lbfa6EEnKr2m+jloW+7wwI3NgnG7Ji2WvKgCyspxSXFGUG9D7QvIRJ+AL581YIa691hUu/sPl0eCNfdjfOEaZfBX8Hgj27/uhlw2TuGwetkk8F9kVwWEvqPtfpaOXu1rayhlZ630IBLM3NnRcS0kBywu7ZkgNK7hIGLB69m29YoVlmyjaIp/L3TprmdPO/MhGqE3UTa4rp766rJRyD+OnDaLItZuQyORITjDzmwy7dxTJAgMBAAGjUDBOMB0GA1UdDgQWBBRiHkNWYsZRCw0+iFwAM3LByS2fgDAfBgNVHSMEGDAWgBRiHkNWYsZRCw0+iFwAM3LByS2fgDAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBBQUAA4IBAQASihdoEaomgKrsKreRl1oGOoEfpw9gPfmnm5N54il3iB3+1A9PkP1BjGXgsYeB2XFHroZnBM++SmebXHCLUcGqDATIGPZuGwJ5YO9Y36g4f7pGKlqeq9q59UTDHIzF3AkaWfEv4kOQnLrXV4nATXsgvX7FC6kaoMDqaeku+QitVzJWGT3JxaX5XBS8O7glqFpc0tqPdq4DV7soD/WiZArl57Grih9G6dCazpr8m+yrhxIWFaZ5F0lEG7Tli/QcegoOkMOhkyT2UQYTuf7VPappzTXu3wm/eALGRkhEl+8/IYme8uPRZRFpWT5aUAfyBpm4VsB+kEL9LL6uq2n3DZ6k"
    const val TYPE_GROUP_CHAT = "groupchat" //群聊
    const val TYPE_LIVE_ROOM = "qxqliveroom"   //直播间
    const val TYPE_MESSAGE = "qxqmessage"      //私聊

    const val SOURCE_TXT = 1
    const val SOURCE_IMAGE = 2
    const val SOURCE_VIDEO = 3
    const val SOURCE_VOICE = 4


    @SuppressLint("CheckResult")
    @JvmStatic
    fun smCheckBeforeSend(
        content: String,
        chatId: String,
        messageType: String,
        source: Int,
        onSmCheckResultListener: OnSmCheckResultListener
    ) {
        if (source == SOURCE_TXT) {
            smCheck(content, chatId, messageType, source, onSmCheckResultListener)
        } else {
            if (isNetUrl(content)) {
                smCheck(content, chatId, messageType, source, onSmCheckResultListener)
            } else {

                UploadFileClient.uploadFile(
                    content,
                    object : UploadFileProgressListener {
                        @SuppressLint("SetTextI18n")
                        override fun onProgress(hasWrittenLen: Long, totalLen: Long) {
                        }

                        override fun onUploadSuccess(url: String) {
                            ThreadUtils.getMainHandler().post {
                                smCheck(url, chatId, messageType, source, onSmCheckResultListener)
                            }
                        }
                        override fun onUploadFail(msg: String) {
                            showToast(msg)
                            onUploadFileFail(msg, onSmCheckResultListener)
                        }
                    },source.toString())

            }

        }

    }

    private fun isNetUrl(path: String): Boolean {
        return path.startsWith("http") || path.startsWith("https")
    }

    private fun onUploadFileFail(
        msg: String,
        onSmCheckResultListener: OnSmCheckResultListener
    ) {
        onSmCheckResultListener.onCheckFail(1,msg)
    }

    @JvmStatic
    @SuppressLint("CheckResult")
    private fun smCheck(
        content: String,
        chatId: String,
        messageType: String,
        source: Int,
        onSmCheckResultListener: OnSmCheckResultListener
    ) {
        request({ imChatRxApi.sendMessageCheckOfShuMei(chatId, content, messageType, source)},object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {
                onSmCheckResultListener.onCheckSuccess(data.data)
            }

            override fun onFail(code: Int?, msg: String?) {
                super.onFail(code, msg)
                onSmCheckResultListener.onCheckFail(code,msg)
            }
        })
    }

    interface OnSmCheckResultListener {
        fun onCheckSuccess(smCheckId: String?)
        fun onCheckFail(code:Int?,msg:String?)
    }
}