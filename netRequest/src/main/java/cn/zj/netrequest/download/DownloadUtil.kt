package cn.zj.netrequest.download

import android.util.Log
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ThreadUtils
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author: zhengjun
 * created: 2023/7/10
 * desc:
 */
class DownloadUtil {
    private val mExecutorService: ExecutorService = Executors.newSingleThreadExecutor()
    private var mBuilder: OkHttpClient.Builder? = null


    fun initConfig(builder: OkHttpClient.Builder?) {
        this.mBuilder = builder
    }

    /**
     * download file and show the progress
     *
     * @param listener
     */
    fun downloadFile(inputParam: InputParameter, listener: FileDownloadListener?) {
        val interceptor = FileDownloadInterceptor(listener)
        if (mBuilder != null) {
            mBuilder!!.addInterceptor(interceptor)
        } else {
            mBuilder = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .retryOnConnectionFailure(true)
                .connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        }
        val api = Retrofit.Builder()
            .client(mBuilder!!.build())
            .baseUrl(inputParam.baseUrl)
            .build()
            .create<DownloadService>(DownloadService::class.java)
        mExecutorService.execute(Runnable {
            try {
                val result = api.downloadWithDynamicUrl(inputParam.relativeUrl).execute()
                val body = result.body()
                if (body == null) {
                    if (inputParam.isCallbackOnUiThread) {
                        ThreadUtils.getMainHandler().post(Runnable { listener!!.onFailed("") })
                    } else {
                        listener!!.onFailed("")
                    }
                    return@Runnable
                }
                val file = FileUtil(ActivityUtils.getTopActivity()).write2SDFromInput(
                    inputParam.loadedFilePath,
                    inputParam.dir,
                    body.byteStream()
                )
                if (listener != null) {
                    if (inputParam.isCallbackOnUiThread) {
                        ThreadUtils.getMainHandler().post(Runnable { listener.onFinish(file) })
                    } else {
                        listener.onFinish(file)
                    }
                }
            } catch (e: Exception) {
                if (listener != null) {
                    if (inputParam.isCallbackOnUiThread) {
                        ThreadUtils.getMainHandler().post(Runnable { listener.onFailed(e.message) })
                    } else {
                        listener.onFailed(e.message)
                    }
                }
                Log.e(TAG, e.message, e)
            }
        })
    }

    companion object {
        private const val TAG = "DownloadUtil"
        private const val DEFAULT_TIMEOUT = 15
    }
}
