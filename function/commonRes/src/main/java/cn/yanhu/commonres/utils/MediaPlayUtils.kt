package cn.yanhu.commonres.utils

import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener

/**
 * @author: zhengjun
 * created: 2023/12/27
 * desc:
 */
object MediaPlayUtils {
    private var mMediaPlayer: MediaPlayer? = null

    @JvmStatic
    var isPause = false

    @JvmStatic
    fun playSound(
        path: String,
        onCompletionListener: OnCompletionListener,
        onMediaStartListener: OnMediaStartListener? = null
    ) {
        try {
            isPause = false
            mMediaPlayer = MediaPlayer()
            // 重置
            mMediaPlayer!!.reset()
            // 设置播放器的声音源
            mMediaPlayer!!.setDataSource(path)
            if (!mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.prepareAsync()
                mMediaPlayer!!.setOnPreparedListener {
                    mMediaPlayer!!.start()
                    onMediaStartListener?.onStart()
                    mMediaPlayer!!.setOnCompletionListener { mp ->
                        isPause = false
                        onCompletionListener.onCompletion(mp)
                    }
                }
            } else {
                mMediaPlayer!!.stop()
            }
        } catch (e: Exception) {
        }
    }


    @JvmStatic
    fun isPlaying(): Boolean {
        return mMediaPlayer?.isPlaying == true
    }

    @JvmStatic
    fun pauseMedia() {
        if (mMediaPlayer?.isPlaying == true) {
            mMediaPlayer?.pause()
            isPause = true
        }
    }

    @JvmStatic
    fun resumeMedia() {
        mMediaPlayer?.start()
    }

    @JvmStatic
    fun stopMedia() {
        if (mMediaPlayer?.isPlaying == true) {
            mMediaPlayer?.stop()
            mMediaPlayer?.reset()
        }
    }

    @JvmStatic
    fun release() {
        stopMedia()
        isPause = false
        mMediaPlayer?.release()
        mMediaPlayer = null
    }

    interface OnMediaStartListener {
        fun onStart()
    }

}