package cn.yanhu.commonres.task

import android.text.TextUtils
import cn.yanhu.baselib.queue.BaseQueueTask
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.utils.SVGAUtils
import cn.zj.netrequest.download.DownloadUtil
import cn.zj.netrequest.download.FileDownloadListener
import cn.zj.netrequest.download.InputParameter
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.opensource.svgaplayer.SVGACache
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGADynamicEntity
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGASoundManager
import com.opensource.svgaplayer.SVGAVideoEntity
import com.tencent.qgame.animplayer.AnimConfig
import com.tencent.qgame.animplayer.AnimView
import com.tencent.qgame.animplayer.inter.IAnimListener
import com.tencent.qgame.animplayer.util.ScaleType
import java.io.File
import java.net.URL

/**
 * @author: zhengjun
 * created: 2023/5/29
 * desc:svga动画播放队列实现
 */
class GiftPopAnimTask(
    private val giftInfo: GiftInfo,
    private val svgaImageView: SVGAImageView,
    private val videoAnimView: AnimView?=null,
    val type: Int = 1
) :
    BaseQueueTask() {
    override fun doTask() {
        var randomGift: GiftInfo? = null

        if (!TextUtils.isEmpty(giftInfo.randomBoxGiftInfo)) {
            randomGift =
                GsonUtils.fromJson(giftInfo.randomBoxGiftInfo, GiftInfo::class.java)

            SVGAUtils.loadCustomSVGAAnim(giftInfo.svga, object : SVGAParser.ParseCompletion {
                override fun onComplete(videoItem: SVGAVideoEntity) {
                    val dynamicItem = SVGADynamicEntity()
                    val drawable = SVGADrawable(videoItem, dynamicItem)
                    dynamicItem.setDynamicImage(randomGift!!.giftIcon, "01")
                    svgaImageView.setImageDrawable(drawable)
                    svgaImageView.tag = videoItem
                    svgaImageView.startAnimation()
                }
                override fun onError() {
                    doNextTask()
                }
            })
        } else {
            playSvga(giftInfo)
        }

        svgaImageView.callback = object : SVGACallback {
            override fun onFinished() {
                val tag = svgaImageView.tag
                if (tag!=null && tag is SVGAVideoEntity){
                    tag.clear()
                }
                if (randomGift != null) {
                    playSvga(randomGift!!)
                    randomGift = null
                } else {
                    doNextTask()
                }
            }

            override fun onPause() {
            }

            override fun onRepeat() {
            }

            override fun onStep(frame: Int, percentage: Double) {
            }
        }
    }

    private fun playSvga(giftInfo: GiftInfo) {
        val svga = giftInfo.svga
        if (svga.endsWith(".mp4") && videoAnimView!=null){
            videoAnimView.setScaleType(ScaleType.CENTER_CROP)
            videoAnimView.setMute(!AppCacheManager.isOpenGiftAudio)
            videoAnimView.setAnimListener(object : IAnimListener{
                override fun onFailed(errorType: Int, errorMsg: String?) {
                }
                override fun onVideoComplete() {
                   doNextTask()
                }
                override fun onVideoDestroy() {
                }
                override fun onVideoRender(frameIndex: Int, config: AnimConfig?) {
                }
                override fun onVideoStart() {
                }
            })
            val url = URL(svga)
            logcom(
                "urlParse",
                "host=" + url.host + "authority=" + url.authority + "path=" + url.path + "protocol=" + url.protocol
            )
            val baseUrl = url.protocol + "://" + url.host + "/"
            val relativeUrl = svga.replace(baseUrl, "")
            val split = relativeUrl.split("/")
            val loadFilePath = split[split.size-1]
            val topActivity = ActivityUtils.getTopActivity()
            val s =
                topActivity.getExternalFilesDir(null)?.absolutePath + File.separator + "sweetLove/" + loadFilePath
            val fileExists = FileUtils.isFileExists(s)
            if (fileExists){
                videoAnimView.startPlay(File(s))
            }else{
                downloadVideo(baseUrl, relativeUrl, loadFilePath, videoAnimView)
            }
        }else{
            SVGACache.clearCache()
            if (AppCacheManager.isOpenGiftAudio) {
                SVGASoundManager.setVolume(1f)
            } else {
                SVGASoundManager.setVolume(0f)
            }
            SVGAUtils.loadCustomSVGAAnim(
                svga,
                object : SVGAParser.ParseCompletion {
                    override fun onComplete(videoItem: SVGAVideoEntity) {
                        svgaImageView.setVideoItem(videoItem)
                        svgaImageView.startAnimation()
                        svgaImageView.tag = videoItem
                    }

                    override fun onError() {
                        doNextTask()
                    }
                })
        }


    }

    private fun downloadVideo(
        baseUrl: String,
        relativeUrl: String,
        loadFilePath: String,
        vgGiftAnim: AnimView
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
                            doNextTask()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        doNextTask()
                    }
                }

                override fun onFailed(msg: String?) {
                    doNextTask()
                }
            })
    }

    override fun finishTask() {

    }
}