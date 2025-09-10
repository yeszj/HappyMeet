package cn.huanyuan.sweetlove.func.manager

import android.annotation.SuppressLint
import android.text.TextUtils
import cn.huanyuan.sweetlove.BuildConfig
import cn.huanyuan.sweetlove.bean.ErrorLogInfo
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import cn.huanyuan.sweetlove.net.rxApi
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.showToast
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request2
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ActivityUtils
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

    /**
     * 上传声网日志
     */
     fun uploadErrorFile(filePathName: String,copyName: String) {
        val logPath =
            "/storage/emulated/0/Android/data/" + BuildConfig.APPLICATION_ID + "/files/${filePathName}"

        val copyLogPath =
            "/storage/emulated/0/Android/data/" + BuildConfig.APPLICATION_ID + "/files/${copyName}"
        if (FileUtils.isFileExists(logPath)) {
            val isSuccess = FileUtils.copy(logPath, copyLogPath)
            val path = if (isSuccess) {
                copyLogPath
            } else {
                logPath
            }
            val file = File(path)
            val requestBody: RequestBody =
                file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData("file", file.name, requestBody)
            request2(
                { rxApi.uploadFile(body, 2) },
                object : OnRequestResultListener<String> {
                    override fun onSuccess(data: BaseBean<String>) {
                        FileUtils.delete(copyLogPath)
                        updateLog(data.data,"透传拉取声网日志")
                    }
                    override fun onFail(code: Int?, msg: String?) {
                        super.onFail(code, msg)
                        logcom("EaseIM", "上传本地log日志失败")
                    }
                })
        }
    }

    /**
     * 上传本地业务日志
     */
    @SuppressLint("CheckResult")
    fun uploadLocalLog() {
        try {
            val absolutePath = ActivityUtils.getTopActivity()
                .getExternalFilesDir(null)?.absolutePath
            val currentLogFilePath = LogUtils.getCurrentLogFilePath()
            val copyLogPath = "$absolutePath/" + System.currentTimeMillis() + "-happyMeet.log"
            if (FileUtils.isFileExists(currentLogFilePath)) {
                val isSuccess = FileUtils.copy(currentLogFilePath, copyLogPath)
                if (isSuccess){
                    val file = File(copyLogPath)
                    val requestBody: RequestBody =
                        file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    val body: MultipartBody.Part =
                        MultipartBody.Part.createFormData("file", file.name, requestBody)
                    request2(
                        { rxApi.uploadFile(body, 2) },
                        object : OnRequestResultListener<String> {
                            override fun onSuccess(data: BaseBean<String>) {
                                FileUtils.delete(copyLogPath)
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

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateLog(url: String?,description: String = "本地业务日志") {
        val logInfo = ErrorLogInfo()
        logInfo.url = url.toString()
        logInfo.description = description
        request2({ rxApi.uploadLog(logInfo) }, object : OnRequestResultListener<String> {
            override fun onSuccess(data: BaseBean<String>) {
            }
        })
    }
}

