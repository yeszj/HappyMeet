package cn.yanhu.agora.manager

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import cn.yanhu.commonres.R
/**
 * @author: zhengjun
 * created: 2024/2/21
 * desc:
 */
object AppAudioManager {
    @SuppressLint("ServiceCast")
    private fun isSilent(context: Context): Boolean {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        return am?.ringerMode != AudioManager.RINGER_MODE_NORMAL
    }

    private var mediaPlayerRing: MediaPlayer? = null
    fun playRing(context: Context) {
        playRing(context,R.raw.chatting)
    }
    fun playRing(context: Context,rawId:Int) {
        if (isSilent(context)) {
            return
        }
        if (mediaPlayerRing != null && mediaPlayerRing?.isPlaying == true) {
            return
        }
        mediaPlayerRing = MediaPlayer.create(context, rawId)
        mediaPlayerRing?.isLooping = true // 设置循环播放
        mediaPlayerRing?.start() // 开始播放
    }

    fun clearMediaPlay() {
        mediaPlayerRing?.apply {
            pause()
            stop()
            release()
            mediaPlayerRing = null
        }
    }

}