package cn.yanhu.commonres.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.commonres.manager.AppCacheManager
import com.blankj.utilcode.util.ThreadUtils
import xyz.doikki.videoplayer.player.BaseVideoView
import xyz.doikki.videoplayer.player.VideoView

/**
 * @author: zhengjun
 * created: 2024/2/26
 * desc:
 */
@SuppressLint("ViewConstructor")
class CustomVideoView(context: Context, val radius: Int, val url: String) : VideoView(context) {

    override fun onAttachedToWindow() {
        isAttach = true
        super.onAttachedToWindow()
        ThreadUtils.getMainHandler().post {
            this.apply {
                setPlayerBackgroundColor(Color.TRANSPARENT)
                setLooping(false)
                isMute = !AppCacheManager.isOpenGiftAudio
                setScreenScaleType(BaseVideoView.SCREEN_SCALE_CENTER_CROP)
                setOnStateChangeListener(object : OnStateChangeListener {
                    override fun onPlayerStateChanged(playerState: Int) {
                    }

                    override fun onPlayStateChanged(playState: Int) {
                        if (playState == STATE_PLAYBACK_COMPLETED) {
                            clearVideoView()
                        }
                    }

                })
                setVideoController(null)
            }
            playVideoView(url)
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
                this.setUrl(url)
                this.start()
            }
        }
    }

    fun clearVideoView() {
        this.pause()
        this.release()
        ViewUtils.removeViewFormParent(this)
    }
}