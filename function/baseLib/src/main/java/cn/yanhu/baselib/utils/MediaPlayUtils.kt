package cn.yanhu.baselib.utils

import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import cn.yanhu.baselib.utils.ext.logcom

/**
 * @author: zhengjun
 * created: 2023/12/27
 * desc:
 */
object MediaPlayUtils {
    private val mMediaPlayer by lazy { MediaPlayer() }

    @JvmStatic
    fun playSound(path: String,onCompletionListener: OnCompletionListener) {
        try {
            // 重置
            mMediaPlayer.reset()
            // 设置播放器的声音源
            mMediaPlayer.setDataSource(path)
            if (!mMediaPlayer.isPlaying) {
                mMediaPlayer.prepareAsync()
                mMediaPlayer.setOnPreparedListener {
                    mMediaPlayer.start()
                    mMediaPlayer.setOnCompletionListener(onCompletionListener)
                }
            } else {
                mMediaPlayer.stop()
            }
        } catch (e: Exception) {
            logcom("音频文件异常，播放失败")
        }
    }

    @JvmStatic
    fun stopMedia(){
        if (mMediaPlayer.isPlaying){
            mMediaPlayer.stop()
            mMediaPlayer.reset()
        }
    }

}