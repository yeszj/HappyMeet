package cn.yanhu.commonres.task

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.TextUtils
import cn.yanhu.baselib.queue.BaseQueueTask
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.logComToFile
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.manager.RoomSwitchCacheManager
import cn.yanhu.commonres.utils.SVGAUtils
import cn.yanhu.commonres.utils.VideoAnimUtils
import cn.yanhu.commonres.utils.VideoAnimUtils.OnLoadVideoAnimListener
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ThreadUtils
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
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
import com.tencent.qgame.animplayer.inter.IFetchResource
import com.tencent.qgame.animplayer.mix.Resource

/**
 * @author: zhengjun
 * created: 2023/5/29
 * desc:svga动画播放队列实现
 */
class GiftPopAnimTask(
    private val giftInfo: GiftInfo,
    private val svgaImageView: SVGAImageView?,
    private val videoAnimView: AnimView? = null,
    val type: Int = 1,
    var roomId: String = ""
) :
    BaseQueueTask() {

    fun getMemoryStatus(): String {
        val runtime = Runtime.getRuntime()
        val usedMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val maxMB = runtime.maxMemory() / (1024 * 1024)
        val usage = (runtime.totalMemory() - runtime.freeMemory()).toDouble() / runtime.maxMemory().toDouble() * 100

        return "已用: ${usedMB}MB / ${maxMB}MB (${"%.1f".format(usage)}%)"
    }

    override fun doTask() {
        try {
            var randomGift: GiftInfo? = null
            if (!TextUtils.isEmpty(giftInfo.randomBoxGiftInfo)) {
                randomGift =
                    GsonUtils.fromJson(giftInfo.randomBoxGiftInfo, GiftInfo::class.java)
            }
            svgaImageView?.apply {
                if (giftInfo.type == GiftInfo.TYPE_FRAME){
                    val width = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_100)
                    ViewUtils.setViewSize(svgaImageView,width,width)
                }else{
                    ViewUtils.setViewMatch(svgaImageView)
                }
            }

            if (randomGift!=null){
                playGiftAnim(randomGift,false)
            }else{
                playGiftAnim(giftInfo,false)
            }

            svgaImageView?.callback = object : SVGACallback {
                override fun onFinished() {
                    val tag = svgaImageView.tag
                    if (tag != null && tag is SVGAVideoEntity) {
                        tag.clear()
                    }
                    doNextTask()
//
//                    if (randomGift != null) {
//                        playGiftAnim(randomGift!!)
//                        randomGift = null
//                    } else {
//                        doNextTask()
//                    }
                }

                override fun onPause() {
                }

                override fun onRepeat() {
                }

                override fun onStep(frame: Int, percentage: Double) {
                }
            }
            videoAnimView?.setFetchResource(object : IFetchResource {
                override fun fetchImage(
                    resource: Resource,
                    result: (Bitmap?) -> Unit
                ) {
//                    if (randomGift != null) {
//                        val srcTag = resource.tag
//                        if (srcTag.isNotEmpty() && "01" == srcTag) {
//                            GlideUtils.loadAsBitmap(
//                                ActivityUtils.getTopActivity(),
//                                randomGift!!.giftIcon,
//                                object :
//                                    CustomTarget<Bitmap>() {
//                                    override fun onResourceReady(
//                                        resource: Bitmap,
//                                        transition: Transition<in Bitmap>?
//                                    ) {
//                                        result(resource)
//                                    }
//
//                                    override fun onLoadCleared(placeholder: Drawable?) {
//                                    }
//
//                                    override fun onLoadFailed(errorDrawable: Drawable?) {
//                                        result(null)
//                                    }
//                                })
//                        } else {
//                            result(null)
//                        }
//                    } else {
//                        result(null)
//                    }
                    result(null)
                }

                override fun fetchText(
                    resource: Resource,
                    result: (String?) -> Unit
                ) {
                    result(null)
                }

                override fun releaseResource(resources: List<Resource>) {
                    resources.forEach {
                        it.bitmap?.recycle()
                        it.bitmap = null
                    }
                }

            })
            videoAnimView?.setAnimListener(object : IAnimListener {
                override fun onFailed(errorType: Int, errorMsg: String?) {
                    logComToFile("playGift","视频礼物特效播放失败:errorType=$errorType，errorMsg=$errorMsg")
                    doNextTask()
                }

                override fun onVideoComplete() {
                    ThreadUtils.getMainHandler().post {
//                        if (randomGift != null) {
//                            playGiftAnim(randomGift!!)
//                            randomGift = null
//                        } else {
//                            doNextTask()
//                        }
//                        System.gc()
                        doNextTask()

                    }
                }

                override fun onVideoDestroy() {
                }

                override fun onVideoRender(frameIndex: Int, config: AnimConfig?) {
                }

                override fun onVideoStart() {
                }
            })

            //logcom(getMemoryStatus())

        } catch (e: Exception) {
            e.printStackTrace()
            doNextTask()
        }
    }

    private fun playGiftAnim(giftInfo: GiftInfo,isBox: Boolean = false) {
        try {
            logComToFile("memoryInfo","送礼："+getMemoryStatus()+"\nanimUrl="+giftInfo.svga+"giftName="+giftInfo.name)
            val svga = giftInfo.svga
            if (isMp4(svga) || isBox) {
                logcom("播放视频礼物特效")
                playVideoAnim(svga,isBox)
            } else {
                logcom("播放svga礼物特效")
                playSvgaAnim(svga, giftInfo)
            }
        }catch (e: Exception){
            logComToFile("memoryInfo","playGiftAnim异常："+e.message)
            doNextTask()
        }

    }

    private fun playSvgaAnim(svga: String?, giftInfo: GiftInfo) {
        if (TextUtils.isEmpty(svga)) {
            doNextTask()
            return
        }
        if (RoomSwitchCacheManager.isOpenGiftVoice(roomId)) {
            SVGASoundManager.setVolume(1f)
        } else {
            SVGASoundManager.setVolume(0f)
        }
        SVGAUtils.loadCustomSVGAAnim(
            svga,
            object : SVGAParser.ParseCompletion {
                override fun onComplete(videoItem: SVGAVideoEntity) {
                    if (!TextUtils.isEmpty(giftInfo.randomBoxGiftInfo)) {
                        svgaImageView?.apply {
                            val randomGift =
                                GsonUtils.fromJson(
                                    giftInfo.randomBoxGiftInfo,
                                    GiftInfo::class.java
                                )
                            val dynamicItem = SVGADynamicEntity()
                            val drawable = SVGADrawable(videoItem, dynamicItem)
                            dynamicItem.setDynamicImage(randomGift!!.giftIcon, "01")
                            svgaImageView.setImageDrawable(drawable)
                            svgaImageView.tag = videoItem
                            svgaImageView.startAnimation()
                        }
                    } else {
                        svgaImageView?.apply {
                            svgaImageView.setVideoItem(videoItem)
                            svgaImageView.startAnimation()
                            svgaImageView.tag = videoItem
                        }
                    }
                }

                override fun onError() {
                    logComToFile("playGift","视频礼物特效加载失败:svga=$svga")
                    doNextTask()
                }
            })
    }

    private fun playVideoAnim(svga: String,isBox: Boolean = false) {
        val topActivity = ActivityUtils.getTopActivity()
        val onLoadVideoAnimListener = object : OnLoadVideoAnimListener{
            override fun onLoadFail() {
                doNextTask()
            }
        }
        if (isBox){
            VideoAnimUtils.loadAssetsVideoAnim(topActivity,"lucky_gift_box.mp4",videoAnimView!!,onLoadVideoAnimListener,roomId)
        }else{
            VideoAnimUtils.loadNetVideoAnim(topActivity,svga,videoAnimView!!,onLoadVideoAnimListener,roomId)
        }
    }

    private fun isMp4(svga: String?): Boolean =
        !TextUtils.isEmpty(svga) && svga!!.endsWith(".mp4") && videoAnimView != null

    override fun finishTask() {

    }
}