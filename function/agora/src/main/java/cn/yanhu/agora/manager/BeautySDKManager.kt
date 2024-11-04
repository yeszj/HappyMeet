package cn.yanhu.agora.manager

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.BeautyFileCacheInfo
import cn.yanhu.agora.bean.ConfigSdkVersion
import cn.yanhu.agora.manager.dbCache.BeautyCacheManager
import cn.yanhu.baselib.utils.ext.logcom
import cn.zj.netrequest.download.DownloadUtil
import cn.zj.netrequest.download.FileDownloadListener
import cn.zj.netrequest.download.InputParameter
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ZipUtils
import java.io.File
import java.net.URL


/**
 * 美颜sdk管理
 */
class BeautySDKManager {
    fun downloadBundle() {
        val beautyCache1 = BeautyCacheManager.getBeautyCache()
        val beautyVersion = beautyCache1?.version ?: 0
        request(
            { agoraRxApi.assertConfigInf(beautyVersion) },
            object : OnRequestResultListener<ConfigSdkVersion> {
                override fun onSuccess(data: BaseBean<ConfigSdkVersion>) {
                    val configSdkVersion = data.data
                    configSdkVersion?.apply {
                        val beautyCache = BeautyCacheManager.getBeautyCache()
                        if (beautyCache != null) {
                            val destFile = getAssetsFile()
                            val length = FileUtils.getLength(destFile)
                            if (beautyCache.fileMd5 != length.toString() || hasNewVersion|| length<=0) {
                                downloadSdkInfo()
                            }
                        } else {
                            downloadSdkInfo()
                        }
                    }
                }
            },
            activity = ActivityUtils.getTopActivity() as FragmentActivity
        )


    }

    private fun ConfigSdkVersion.downloadSdkInfo() {
        val url = URL(this.downloadUrl)
        logcom(
            "urlParse",
            "host=" + url.host + "authority=" + url.authority + "path=" + url.path + "protocol=" + url.protocol
        )
        val baseUrl = url.protocol + "://" + url.host + "/"
        val relativeUrl = downloadUrl.replace(baseUrl, "")
        DownloadUtil().downloadFile(
            InputParameter.Builder(
                baseUrl,
                relativeUrl,
                "beautyBundle",
                "assets.zip"
            ).setCallbackOnUiThread(true).build(), object :
                FileDownloadListener {
                override fun onProgress(
                    progress: Int,
                    downloadedLength: Long,
                    totalLength: Long,
                ) {
                }

                override fun onFinish(file: File?) {
                    try {
                        val destFile = getAssetsFile()
                        if (FileUtils.isFileExists(destFile)
                        ) {
                            FileUtils.delete(destFile)
                        }
                        val unzipFile = ZipUtils.unzipFile(file!!, destFile)
                        if (unzipFile.size > 0) {
                            var beautyCache = BeautyCacheManager.getBeautyCache()
                            if (beautyCache == null) {
                                beautyCache = BeautyFileCacheInfo(FileUtils.getLength(destFile).toString(),version)
                            } else {
                                beautyCache.fileMd5 =  FileUtils.getLength(destFile).toString()
                                beautyCache.version = version
                            }
                            BeautyCacheManager.saveBeautySdkInfo(beautyCache)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

                override fun onFailed(msg: String?) {
                }
            })
    }

    private fun getAssetsFile(): File {
        val unZipPath: String =
            ActivityUtils.getTopActivity()
                .getExternalFilesDir(null)?.absolutePath + File.separator + "/assets"
        return File(unZipPath)
    }

    private fun getFile(path: String): File {
        val topActivity = ActivityUtils.getTopActivity()
        return File(
            topActivity.getExternalFilesDir("assets"), path
        )
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var sdkManage: BeautySDKManager? = null


        @JvmStatic
        fun sharedInstance(): BeautySDKManager {
            if (sdkManage == null) {
                synchronized(BeautySDKManager::class.java) {
                    if (sdkManage == null) {
                        sdkManage = BeautySDKManager()
                    }
                }
            }
            return sdkManage!!
        }

    }
}