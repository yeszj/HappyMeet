package cn.yanhu.agora.config.rtc

import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler


interface RtcEventHandler {
    fun onRtcJoinChannelSuccess(channel: String, uid: Int, elapsed: Int){}
    fun onRtcStats(stats: IRtcEngineEventHandler.RtcStats){}
    fun onRtcAudioVolumeIndication(speakers: Array<IRtcEngineEventHandler.AudioVolumeInfo>, totalVolume: Int){}
    fun onUserOffline(uid: Int, reason: Int){}
    fun onUserJoined(uid: Int){}
    fun onClientRoleChanged(oldRole: Int, newRole: Int){}
    fun onUserMuteAudio(uid: Int, muted: Boolean){}
    fun onTokenExpire(){}
    fun onAudioMixingStateChanged(state:Int ,  reasonCode:Int){}

    fun onRtcRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int){}
    fun onUserEnableVideo(uid: Int, enabled: Boolean){}
    fun onUserMuteVideo(uid: Int, muted: Boolean){}
    fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {}
    fun onNetworkTypeChanged(type: Int) {}
     fun onLocalVideoStateChanged(
        source: Constants.VideoSourceType?,
        state: Int,
        error: Int
    ){}

    fun onError(err: Int){}
}