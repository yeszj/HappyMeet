package cn.huanyuan.sweetlove.func.manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import cn.huanyuan.sweetlove.bean.AppVersionInfo
import cn.yanhu.baselib.utils.ext.logcom
import cn.zj.netrequest.download.DownloadUtil
import cn.zj.netrequest.download.FileDownloadListener
import cn.zj.netrequest.download.InputParameter
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import java.io.File
import java.net.URL


class ApkDownloadManager {
    fun downloadApk(context: Context,appVersionInfo: AppVersionInfo,onProgressListener: OnProgressListener) {
        val url = URL(appVersionInfo.pageUrl)
        logcom(
            "urlParse",
            "host=" + url.host + "authority=" + url.authority + "path=" + url.path + "protocol=" + url.protocol
        )
        val baseUrl = url.protocol + "://" + url.host + "/"
        val relativeUrl = appVersionInfo.pageUrl.replace(baseUrl, "")
        DownloadUtil().downloadFile(
            InputParameter.Builder(
                baseUrl,
                relativeUrl,
                "apkDownload",
                "sweetLove.apk"
            ).setCallbackOnUiThread(true).build(), object :
                FileDownloadListener {
                override fun onProgress(
                    progress: Int,
                    downloadedLength: Long,
                    totalLength: Long,
                ) {
                    onProgressListener.onProgress(progress)
                }

                override fun onFinish(file: File?) {
                    try {
                        onProgressListener.onFinish()
                        installAPK(file,context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                override fun onFailed(msg: String?) {
                }
            })
    }


    fun installAPK(file: File?,context: Context) {
        try {
            if (file==null || !file.exists()) {
                return
            }
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) //安装完成后打开新版本
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // 给目标应用一个临时授权
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //判断版本大于等于7.0
                //如果SDK版本>=24，即：Build.VERSION.SDK_INT >= 24，使用FileProvider兼容安装apk
                val apkUri = FileProvider.getUriForFile(
                    context,
                    AppUtils.getAppPackageName()+".fileprovider",
                    file
                )
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            } else {
                intent.setDataAndType(
                    Uri.fromFile(file),
                    "application/vnd.android.package-archive"
                )
            }
            context.startActivity(intent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    private fun getAssetsFile(): File {
        val unZipPath: String =
            ActivityUtils.getTopActivity()
                .getExternalFilesDir(null)?.absolutePath + File.separator + "/assets"
        return File(unZipPath)
    }

     interface OnProgressListener{
        fun onProgress(progress:Int)
        fun onFinish()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var sdkManage: ApkDownloadManager? = null


        @JvmStatic
        fun sharedInstance(): ApkDownloadManager {
            if (sdkManage == null) {
                synchronized(ApkDownloadManager::class.java) {
                    if (sdkManage == null) {
                        sdkManage = ApkDownloadManager()
                    }
                }
            }
            return sdkManage!!
        }

    }
}