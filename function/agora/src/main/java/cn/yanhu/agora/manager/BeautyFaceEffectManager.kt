package cn.yanhu.agora.manager

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import cn.yanhu.agora.bean.BeautyFileCacheInfo
import cn.yanhu.agora.bean.ConfigSdkVersion
import cn.yanhu.agora.listener.OnDownloadProgressListener
import cn.yanhu.agora.manager.dbCache.BeautyFaceEffectCacheManager
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.commonres.api.commonRxApi
import cn.zj.netrequest.download.DownloadUtil
import cn.zj.netrequest.download.FileDownloadListener
import cn.zj.netrequest.download.InputParameter
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ZipUtils
import java.io.File
import java.net.URL


/**
 * 美颜贴脸特效文件管理
 */
class BeautyFaceEffectManager {
    var stickerDir = "sticker_face_shape"
    fun downloadBundle(downloadProgressListener: OnDownloadProgressListener) {
        val beautyCache = BeautyFaceEffectCacheManager.getBeautyCache()
        val beautyVersion = beautyCache?.version ?: 0
        request(
            { commonRxApi.getConfigInfo("beauty_makeup_config") },
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    val configSdkVersion =
                        GsonUtils.fromJson(data.data, ConfigSdkVersion::class.java)
                    configSdkVersion?.apply {
                        if (this.version > beautyVersion) {
                            this.hasNewVersion = true
                        }
                        if (beautyCache != null) {
                            val destFile = File(
                                getAssetsFile(), stickerDir
                            )
                            val length = FileUtils.getLength(destFile)
                            if (hasNewVersion || length <= 0 || length.toString() != beautyCache.fileMd5) {
                                BeautyFaceEffectCacheManager.clearFaceEffectSdk()
                                if (FileUtils.isFileExists(destFile)) {
                                    FileUtils.delete(destFile)
                                }
                                downloadSdkInfo(downloadProgressListener)
                            }
                        } else {
                            downloadSdkInfo(downloadProgressListener)
                        }
                    }
                }
            },
            activity = ActivityUtils.getTopActivity() as FragmentActivity
        )


    }

    private fun ConfigSdkVersion.downloadSdkInfo(downloadProgressListener: OnDownloadProgressListener) {
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
                stickerDir,
                "${stickerDir}.zip"
            ).setCallbackOnUiThread(true).build(), object :
                FileDownloadListener {
                override fun onProgress(
                    progress: Int,
                    downloadedLength: Long,
                    totalLength: Long,
                ) {
                    val realProgress = if (progress == 100) {
                        99
                    } else {
                        progress
                    }
                    downloadProgressListener.onProgress(realProgress)
                }

                override fun onFinish(file: File?) {
                    try {
                        val destFile = getAssetsFile()
                        val unzipFile = ZipUtils.unzipFile(file!!, destFile)
                        if (unzipFile.isNotEmpty()) {
                            val stickerFile = File(
                                destFile, stickerDir
                            )
                            var beautyCache = BeautyFaceEffectCacheManager.getBeautyCache()
                            if (beautyCache == null) {
                                beautyCache = BeautyFileCacheInfo(
                                    FileUtils.getLength(stickerFile).toString(),
                                    version
                                )
                            } else {
                                beautyCache.fileMd5 = FileUtils.getLength(stickerFile).toString()
                                beautyCache.version = version
                            }
                            BeautyFaceEffectCacheManager.saveBeautySdkInfo(beautyCache)
                            downloadProgressListener.onProgress(100)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        reDownLoadWhenFail(downloadProgressListener)

                    }

                }

                override fun onFailed(msg: String?) {
                    reDownLoadWhenFail(downloadProgressListener)
                }
            })
    }

    private var downloadFailCount = 0
    private fun ConfigSdkVersion.reDownLoadWhenFail(downloadProgressListener: OnDownloadProgressListener) {
        if (downloadFailCount < 2) {
            downloadSdkInfo(downloadProgressListener)
            downloadFailCount++
        } else {
            logcom("agoraSdk", "声网sdk重新下载2次失败")
            downloadProgressListener.onDownLoadFail()
        }
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
        private var sdkManage: BeautyFaceEffectManager? = null


        @JvmStatic
        fun sharedInstance(): BeautyFaceEffectManager {
            if (sdkManage == null) {
                synchronized(BeautyFaceEffectManager::class.java) {
                    if (sdkManage == null) {
                        sdkManage = BeautyFaceEffectManager()
                    }
                }
            }
            return sdkManage!!
        }

    }
}