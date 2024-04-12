package cn.yanhu.agora.config.rtc

import android.view.SurfaceView
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.config.AgoraManager
import cn.yanhu.agora.manager.BeautySetManager
import cn.yanhu.baselib.utils.ext.logcom
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ThreadUtils
import io.agora.rtc2.ClientRoleOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
import io.agora.rtc2.Constants.AUDIENCE_LATENCY_LEVEL_ULTRA_LOW_LATENCY
import io.agora.rtc2.video.VideoCanvas

/**
 * @author: witness
 * created: 2022/12/14
 * desc:
 */
object RtcManager {
    fun setClientBroadCaster(): Boolean {
        val options = ClientRoleOptions()
        options.audienceLatencyLevel = AUDIENCE_LATENCY_LEVEL_ULTRA_LOW_LATENCY
        return AgoraManager.getInstance().rtcEngine()
            .setClientRole(Constants.CLIENT_ROLE_BROADCASTER, options) == 0
    }

    fun setClientAudience(): Boolean {
        val options = ClientRoleOptions()
        options.audienceLatencyLevel = AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
        return AgoraManager.getInstance().rtcEngine()
            .setClientRole(Constants.CLIENT_ROLE_AUDIENCE, options) == 0
    }

    fun joinRtcChannel(agoraToken: String, channelName: String, userId: String): Boolean {
        return AgoraManager.getInstance().rtcEngine()
            .joinChannel(agoraToken, channelName, "", userId.toInt()) == 0
    }

    fun leaveChannel(): Boolean {
        logcom("voiceRoomTag", "开始离开房间")
        return AgoraManager.getInstance().rtcEngine().leaveChannel() == 0
    }


    fun reNewToken(roomId: String) {
        ThreadUtils.getMainHandler().post {
            request({
                agoraRxApi.getAgoraToken(roomId)
            }, object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    AgoraManager.getInstance().rtcEngine().renewToken(data.data)
                }
            })
        }


    }

    /**
     * 取消或恢复订阅所有远端用户的音频流。
     * @isMute true  取消订阅(直播静音) false打开声音
     */
    @JvmStatic
    fun muteAllRemoteAudioStreams(isMute: Boolean): Boolean {
        val muteAllRemoteAudioStreams =
            AgoraManager.getInstance().rtcEngine().muteAllRemoteAudioStreams(isMute)
        return muteAllRemoteAudioStreams == 0
    }

    /**
     * 打开/关闭麦克风
     * @isOpen true 打开 false 关闭
     */
    @JvmStatic
    fun enableMicPhone(isOpen: Boolean): Boolean {
        val enableLocalAudio = AgoraManager.getInstance().rtcEngine().muteLocalAudioStream(!isOpen)
        return enableLocalAudio == 0
    }

    fun setEnableSpeakerphone(isSpeaker: Boolean): Boolean {
        return AgoraManager.getInstance().rtcEngine().setEnableSpeakerphone(isSpeaker) == 0
    }

    fun isSpeakerphoneEnabled(): Boolean {
        return AgoraManager.getInstance().rtcEngine().isSpeakerphoneEnabled
    }


    //关闭或者打开本地视频
    fun muteLocalVideoStream(isStartPlaying: Boolean): Boolean {
        return AgoraManager.getInstance().rtcEngine().muteLocalVideoStream(!isStartPlaying) == 0
    }

    /**
     * 打开或关闭视频功能
     */
    fun enableVideo(isOpen: Boolean) {
        if (isOpen) {
            AgoraManager.getInstance().rtcEngine().enableVideo()
        } else {
            AgoraManager.getInstance().rtcEngine().disableVideo()
        }
    }


    fun startPreLocalVideoView(surfaceView: SurfaceView?) {
        val rtcEngine = AgoraManager.getInstance().rtcEngine()
        BeautySetManager.getInstance().enableBeauty(true)
        if (surfaceView != null) {
            rtcEngine.enableVideo()
            rtcEngine.startPreview()
            rtcEngine.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
        }
    }

}