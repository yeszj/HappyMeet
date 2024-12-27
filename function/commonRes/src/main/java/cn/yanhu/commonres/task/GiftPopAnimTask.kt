package cn.yanhu.commonres.task

import android.text.TextUtils
import cn.yanhu.baselib.queue.BaseQueueTask
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.utils.SVGAUtils
import com.blankj.utilcode.util.GsonUtils
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGADynamicEntity
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity

/**
 * @author: zhengjun
 * created: 2023/5/29
 * desc:svga动画播放队列实现
 */
class GiftPopAnimTask(private val giftInfo: GiftInfo, private val svgaImageView: SVGAImageView,val type:Int = 1) :
    BaseQueueTask() {
    override fun doTask() {
        var randomGift:GiftInfo?=null
        if (!TextUtils.isEmpty(giftInfo.randomBoxGiftInfo)){
            randomGift =
                GsonUtils.fromJson(giftInfo.randomBoxGiftInfo, GiftInfo::class.java)
            SVGAUtils.loadCustomSVGAAnim(giftInfo.svga, object : SVGAParser.ParseCompletion {
                override fun onComplete(videoItem: SVGAVideoEntity) {
                    val dynamicItem = SVGADynamicEntity()
                    val drawable = SVGADrawable(videoItem, dynamicItem)
                    dynamicItem.setDynamicImage(randomGift!!.giftIcon, "01")
                    svgaImageView.setImageDrawable(drawable)
                    svgaImageView.startAnimation()
                }
                override fun onError() {
                }
            })
        }else{
            SVGAUtils.loadSVGAAnim(svgaImageView, giftInfo.svga, object : SVGAParser.ParseCompletion {
                override fun onComplete(videoItem: SVGAVideoEntity) {
                }

                override fun onError() {
                    doNextTask()
                }
            })
        }

        svgaImageView.callback = object : SVGACallback {
            override fun onFinished() {
                if (randomGift!=null) {
                    SVGAUtils.loadSVGAAnim(svgaImageView, randomGift!!.svga, object : SVGAParser.ParseCompletion {
                        override fun onComplete(videoItem: SVGAVideoEntity) {
                        }

                        override fun onError() {
                            doNextTask()
                        }
                    })
                    randomGift = null
                }else{
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

    override fun finishTask() {

    }
}