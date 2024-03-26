package cn.yanhu.commonres.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import com.blankj.utilcode.util.ThreadUtils
import com.lihang.ShadowLayout
import xyz.doikki.videoplayer.player.BaseVideoView
import xyz.doikki.videoplayer.player.VideoView

/**
 * @author: zhengjun
 * created: 2024/2/26
 * desc:
 */
@SuppressLint("ViewConstructor")
class CustomVideoView(context: Context, val radius: Int, val url: String) : ShadowLayout(context) {
    private lateinit var videoView: VideoView

    override fun onAttachedToWindow() {
        isAttach = true
        super.onAttachedToWindow()
        ThreadUtils.getMainHandler().post {
            videoView = VideoView(context)
            setCornerRadius(CommonUtils.getDimension(radius))
            videoView.apply {
                setPlayerBackgroundColor(Color.TRANSPARENT)
                setLooping(true)
                isMute = true
                setScreenScaleType(BaseVideoView.SCREEN_SCALE_CENTER_CROP)
                setOnStateChangeListener(object : BaseVideoView.OnStateChangeListener {
                    override fun onPlayerStateChanged(playerState: Int) {
                    }

                    override fun onPlayStateChanged(playState: Int) {
                    }
                })
                setVideoController(null)
            }
            playVideoView(url)
            addView(this)
        }
    }

    private var isAttach = false
    override fun onDetachedFromWindow() {
        isAttach = false
        clearVideoView()
        super.onDetachedFromWindow()
    }

    fun playVideoView(url: String) {
        if (isAttach) {
            ThreadUtils.getMainHandler().post {
                videoView.setUrl(url)
                videoView.start()
            }
        }
    }

    fun clearVideoView() {
        videoView.pause()
        videoView.release()
        ViewUtils.removeViewFormParent(videoView)
    }
}