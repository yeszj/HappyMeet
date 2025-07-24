package cn.yanhu.commonres.utils

import android.content.Context
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.commonres.manager.AppCacheManager
import cn.zj.netrequest.download.DownloadUtil
import cn.zj.netrequest.download.FileDownloadListener
import cn.zj.netrequest.download.InputParameter
import com.blankj.utilcode.util.FileUtils
import com.tencent.qgame.animplayer.AnimView
import com.tencent.qgame.animplayer.util.ScaleType
import java.io.File
import java.net.URL

/**
 * @author: zhengjun
 * created: 2025/7/23
 * desc:
 */
object VideoAnimUtils {
    interface OnLoadVideoAnimListener {
        fun onLoadFail()
    }

    fun loadAssetsVideoAnim(
        context: Context,
        assetsName: String,
        videoAnimView: AnimView, loadListener: OnLoadVideoAnimListener? = null
    ) {
        videoAnimView.setScaleType(ScaleType.CENTER_CROP)
        videoAnimView.setMute(!AppCacheManager.isOpenGiftAudio)
        logcom("播发assets礼物特效")
        val dir = context.getExternalFilesDir(null)?.absolutePath + File.separator + "sweetLove"
        val boxPath = "$dir/$assetsName"
        if (FileUtils.isFileExists(boxPath)) {
            logcom("特效本地已经缓存")
            videoAnimView.startPlay(File(boxPath))
        } else {
            FileUtils.createOrExistsDir(dir)
            FileAssetsUtils.copyAssetsToStorage(context, dir, arrayOf(assetsName), {
                videoAnimView.startPlay(File(boxPath))
            }, {
                loadListener?.onLoadFail()
            })
        }
    }

    fun loadNetVideoAnim(
        context: Context,
        animUrl: String,
        videoAnimView: AnimView,
        loadListener: OnLoadVideoAnimListener? = null
    ) {
        videoAnimView.setScaleType(ScaleType.CENTER_CROP)
        videoAnimView.setMute(!AppCacheManager.isOpenGiftAudio)
        val url = URL(animUrl)
        logcom(
            "urlParse",
            "host=" + url.host + "authority=" + url.authority + "path=" + url.path + "protocol=" + url.protocol
        )
        val baseUrl = url.protocol + "://" + url.host + "/"
        val relativeUrl = animUrl.replace(baseUrl, "")
        val split = relativeUrl.split("/")
        val loadFilePath = split[split.size - 1]
        val cachePath = loadFilePath.substringBefore("?")
        val s =
            context.getExternalFilesDir(null)?.absolutePath + File.separator + "sweetLove/" + cachePath
        val fileExists = FileUtils.isFileExists(s)
        if (fileExists) {
            logcom("视频礼物特效本地已经缓存")
            videoAnimView.startPlay(File(s))
        } else {
            downloadVideo(baseUrl, relativeUrl, cachePath, videoAnimView, loadListener)
        }
    }


    private fun downloadVideo(
        baseUrl: String,
        relativeUrl: String,
        loadFilePath: String,
        vgGiftAnim: AnimView, loadListener: OnLoadVideoAnimListener? = null
    ) {
        DownloadUtil().downloadFile(
            InputParameter.Builder(
                baseUrl,
                relativeUrl,
                "sweetLove",
                loadFilePath
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
                        if (file != null) {
                            vgGiftAnim.startPlay(file)
                        } else {
                            loadListener?.onLoadFail()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        loadListener?.onLoadFail()
                    }
                }

                override fun onFailed(msg: String?) {
                    loadListener?.onLoadFail()
                }
            })
    }
}