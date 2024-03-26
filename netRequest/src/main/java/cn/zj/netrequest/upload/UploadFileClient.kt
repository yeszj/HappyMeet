package cn.zj.netrequest.upload

import android.text.TextUtils
import android.util.Log
import cn.zj.netrequest.FilePartManager
import cn.zj.netrequest.R
import com.blankj.utilcode.util.ThreadUtils
import cn.zj.netrequest.status.ErrorCode
import com.blankj.utilcode.BuildConfig
import com.blankj.utilcode.util.StringUtils.getString
import cn.zj.netrequest.application.ApplicationProxy
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object UploadFileClient {
    private var client: OkHttpClient? = null

    @get:Synchronized
    private val okHttpClient: OkHttpClient?
        get() {
            if (null == client) {
                val builder: OkHttpClient.Builder = OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                if (BuildConfig.DEBUG || !TextUtils.equals(BuildConfig.BUILD_TYPE, "release")) {
                    val loggingInterceptor = HttpLoggingInterceptor()
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                    builder.addInterceptor(loggingInterceptor)
                }
                client = builder.build()
            }
            return client
        }

    fun uploadFile(
        imagePath: String,
        progressRequestListener: UploadFileProgressListener
    ) {
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<JSONObject>() {
            override fun onSuccess(result: JSONObject?) {
                if (result != null) {
                    Log.d("uploadProgress", "上传成功")
                    if (result.optInt("code") == ErrorCode.SUCCESS) {
                        progressRequestListener.onUploadSuccess(result.optString("data"))
                    } else {
                        progressRequestListener.onUploadFail(getString(R.string.file_size_too_big))
                    }
                }
            }

            override fun doInBackground(): JSONObject? {
                return executePostFile(
                    imagePath,
                    progressRequestListener
                )
            }
        })
    }


    fun uploadFile(
        url: String,
        path: String,
        bucket: String,
        fileName: String,
        progressRequestListener: UploadFileProgressListener
    ) {
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<JSONObject>() {
            override fun onSuccess(result: JSONObject?) {
                if (result != null) {
                    Log.d("uploadProgress", "上传成功")
                    if (result.optInt("code") == ErrorCode.SUCCESS) {
                        progressRequestListener.onUploadSuccess(result.optString("data"))
                    } else {
                        progressRequestListener.onUploadFail(getString(R.string.file_size_too_big))
                    }
                }
            }

            override fun doInBackground(): JSONObject? {

                return executePostFile(
                    path,
                    progressRequestListener, bucket, fileName
                )
            }
        })
    }

    private fun executePostFile(
        path: String,
        progressRequestListener: UploadFileProgressListener?,
        bucket: String = "",
        fileName: String = ""
    ): JSONObject{
        val client = okHttpClient
        val body: RequestBody = FilePartManager.getParam(path, "file")
        val header = ApplicationProxy.instance.getHead()

        val builder = Request.Builder()
        for ((key, value) in header) {
            Log.d("okhttp.OkHttpClient", "$key:$value")
            if (!TextUtils.isEmpty(value)){
                builder.addHeader(key, value!!)
            }
        }
        if (!TextUtils.isEmpty(bucket)) {
            builder.addHeader("path", bucket)
        }
        if (!TextUtils.isEmpty(fileName)) {
            builder.addHeader("fileName", fileName)
        }
        val request: Request = builder
            .url("${ApplicationProxy.instance.getServeAddress()}app/v1/file/upload")
            .post(UploadFileRequestBody(body, progressRequestListener))
            .build()
        try {
            val newCall = client!!.newCall(request)
            val response = newCall.execute()
            val result = response.body?.string().toString()
            val jsonObject = JSONObject(result)
            val code = jsonObject.optInt("code")
            if (code == ErrorCode.TOKEN_INVALID || code == ErrorCode.TOKEN_FAIL || code == ErrorCode.UNLOGIN) {
                ApplicationProxy.instance.loginInvalid()
            }
            return jsonObject
        } catch (e: FileNotFoundException) {
            val resultObj = JSONObject()
            try {
                resultObj.put("msg", "上传截图失败。无法找到截图，请确认截图未被删除")
            } catch (e1: JSONException) {
                e1.printStackTrace()
            }
            return resultObj
        } catch (e: SocketException) {
            val resultObj = JSONObject()
            try {
                resultObj.put("msg", "上传截图超时，请检查网络正常或换个网络尝试。se")
            } catch (e1: JSONException) {
                e1.printStackTrace()
            }
            return resultObj
        } catch (e: SocketTimeoutException) {
            val resultObj = JSONObject()
            try {
                resultObj.put("msg", "上传截图超时，请检查网络正常或换个网络尝试。")
            } catch (e1: JSONException) {
                e1.printStackTrace()
            }
            return resultObj
        } catch (e: Exception) {
            val resultObj = JSONObject()
            try {
                resultObj.put(
                    "msg",
                    "上传截图失败,如尝试多次仍旧无法上传截图，请截图此提示联系客服---" + Log.getStackTraceString(e)
                )
            } catch (e1: JSONException) {
                e1.printStackTrace()
            }
            return resultObj
        }
    }
}