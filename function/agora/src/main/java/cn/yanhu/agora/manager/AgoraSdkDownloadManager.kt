package cn.yanhu.agora.manager

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context.MODE_PRIVATE
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.AgoraSdkCacheInfo
import cn.yanhu.agora.bean.ConfigSdkVersion
import cn.yanhu.agora.listener.OnDownloadProgressListener
import cn.yanhu.agora.manager.dbCache.AgoraSdkCacheManager
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.utils.DeviceInfoUtil
import cn.zj.netrequest.download.DownloadUtil
import cn.zj.netrequest.download.FileDownloadListener
import cn.zj.netrequest.download.InputParameter
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ZipUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import org.litepal.LitePal
import java.io.File
import java.net.URL

/**
 * @author: zhengjun
 * created: 2024/7/12
 * desc:
 */
object AgoraSdkDownloadManager {
    @JvmStatic
    fun getSoPath(): String {
        return ActivityUtils.getTopActivity().getDir("libs", MODE_PRIVATE).absolutePath
    }

    private var downloadFailCount = 0
    private var startTime = 0L

    @SuppressLint("CheckResult")
    @JvmStatic
    fun downloadAgoraSdkInfo(downloadProgressListener: OnDownloadProgressListener) {
        val agoraSdkInfo = AgoraSdkCacheManager.getAgoraSdk()
        val beautyVersion = agoraSdkInfo?.version ?: 0
        startTime = System.currentTimeMillis()

        request({ agoraRxApi.agoraConfigInf(if (DeviceInfoUtil.isCpu64()) 2 else 1,beautyVersion)},object : OnRequestResultListener<ConfigSdkVersion>{
            override fun onSuccess(data: BaseBean<ConfigSdkVersion>) {
                val configSdkVersion = data.data
                configSdkVersion?.apply {
                    if (agoraSdkInfo != null) {
                        val soPath = getSoPath()
                        val length = FileUtils.getLength(soPath)
                        if (hasNewVersion || length <= 0 || length.toString() != agoraSdkInfo.fileSize) {
                            downloadSdkInfo(downloadProgressListener)
                        }
                    } else {
                        downloadSdkInfo(downloadProgressListener)
                    }
                }
            }

        })
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
            baseUrl, relativeUrl, "agoraSdk", "agora.zip"
        ).setCallbackOnUiThread(true).build(), object : FileDownloadListener {
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
                    val sdkFile = getAgoraSdkFile()
                    if (FileUtils.isFileExists(sdkFile)) {
                        FileUtils.delete(sdkFile)
                    }
                    val unzipFile = ZipUtils.unzipFile(file!!, getAgoraUnZipFile())
                    if (unzipFile.size > 0) {
                        val listFiles = sdkFile.listFiles()
                        listFiles?.forEach {
                            //把下载下来的.so文件复制到libs文件夹下 动态加载so库
                            FileUtils.copy(
                                it.toString(),
                                ActivityUtils.getTopActivity()
                                    .getDir("libs", MODE_PRIVATE).absolutePath + "/" + it.name
                            )
                        }
                       // val downLoadTimeSecond = (System.currentTimeMillis() - startTime) / 1000
                        downloadProgressListener.onProgress(100)
                        saveAgoraSdkToCache()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    logcom("agoraSdk", "声网sdk下载失败${e.printStackTrace()}")
                    reDownLoadWhenFail(downloadProgressListener)
                }
            }

            override fun onFailed(msg: String?) {
                reDownLoadWhenFail(downloadProgressListener)
                logcom("agoraSdk", "声网sdk下载失败$msg")
            }
        })
    }

    private fun ConfigSdkVersion.reDownLoadWhenFail(downloadProgressListener: OnDownloadProgressListener) {
        if (downloadFailCount < 2) {
            downloadSdkInfo(downloadProgressListener)
            downloadFailCount++
        } else {
            logcom("agoraSdk", "声网sdk重新下载2次失败")
            downloadProgressListener.onDownLoadFail()
        }
    }

    private fun ConfigSdkVersion.saveAgoraSdkToCache() {
        val agoraSdk = AgoraSdkCacheManager.getAgoraSdk()
        if (agoraSdk != null) {
            val values = ContentValues()
            values.put("fileSize", FileUtils.getLength(getSoPath()).toString())
            if (hasNewVersion) {
                values.put("version", version)
            }
            LitePal.update(
                AgoraSdkCacheInfo::class.java, values, agoraSdk.id
            )
        } else {
            AgoraSdkCacheInfo(
                FileUtils.getLength(getSoPath()).toString(), version
            ).save()
        }
        FileUtils.delete(getAgoraUnZipFile())
        RtcEngineInit.initRtcEngine(ActivityUtils.getTopActivity())
        LiveEventBus.get<Boolean>(EventBusKeyConfig.DOWNLOAD_AGORA_SDK_SUCCESS).post(true)
    }

    private fun getAgoraUnZipFile(): File {
        val unZipPath: String = ActivityUtils.getTopActivity()
            .getExternalFilesDir(null)?.absolutePath + File.separator + "agoraSdk"
        return File(unZipPath)
    }

    private fun getAgoraSdkFile(): File {
        val unZipPath: String = getAgoraSdkPath()
        return File(unZipPath)
    }

    private fun getAgoraSdkPath(): String {
        return ActivityUtils.getTopActivity()
            .getExternalFilesDir(null)?.absolutePath + File.separator + "agoraSdk/agora"
    }

}