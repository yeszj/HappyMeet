package cn.yanhu.commonres.task

import cn.yanhu.baselib.queue.BaseQueueTask
import cn.yanhu.commonres.utils.SVGAUtils
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity

/**
 * @author: zhengjun
 * created: 2023/5/29
 * desc:svga动画播放队列实现
 */
class GiftPopAnimTask(private val url: String, private val svgaImageView: SVGAImageView) :
    BaseQueueTask() {
    override fun doTask() {
        svgaImageView.callback = object : SVGACallback {
            override fun onFinished() {
                doNextTask()
            }

            override fun onPause() {
            }

            override fun onRepeat() {
            }

            override fun onStep(frame: Int, percentage: Double) {
            }
        }
        SVGAUtils.loadSVGAAnim(svgaImageView, url, object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
            }

            override fun onError() {
                doNextTask()
            }
        })


    }

    override fun finishTask() {

    }
}