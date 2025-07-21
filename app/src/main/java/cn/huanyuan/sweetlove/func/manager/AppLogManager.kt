package cn.huanyuan.sweetlove.func.manager

import android.annotation.SuppressLint
import cn.huanyuan.sweetlove.bean.ErrorLogInfo
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import cn.huanyuan.sweetlove.net.rxApi
import cn.yanhu.baselib.utils.ext.logcom
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.BaseBean
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * @author: zhengjun
 * created: 2023/9/8
 * desc:
 */
object AppLogManager {

    @SuppressLint("CheckResult")
    fun uploadLog() {
        try {
            val currentLogFilePath = LogUtils.getCurrentLogFilePath()

            if (FileUtils.isFileExists(currentLogFilePath)) {
                val file = File(currentLogFilePath)
                val requestBody: RequestBody =
                    file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                val body: MultipartBody.Part =
                    MultipartBody.Part.createFormData("file", file.name, requestBody)
                request2(
                    { rxApi.uploadFile(body, 2) },
                    object : OnRequestResultListener<String> {
                        override fun onSuccess(data: BaseBean<String>) {
                            FileUtils.delete(currentLogFilePath)
                            val url = data.data
                            logcom("EaseIM", "上传本地log日志成功，url=$url")
                            updateLog(url)
                        }
                        override fun onFail(code: Int?, msg: String?) {
                            super.onFail(code, msg)
                            logcom("EaseIM", "上传本地log日志失败")
                        }

                    })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateLog(url: String?) {
        val logInfo = ErrorLogInfo()
        logInfo.url = url.toString()
        logInfo.description = "本地业务日志"
        request2({ rxApi.uploadLog(logInfo) }, object : OnRequestResultListener<String> {
            override fun onSuccess(data: BaseBean<String>) {
            }
        })
    }
}

