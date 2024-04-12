package cn.yanhu.agora.config.rtc

import android.util.Log
import cn.yanhu.baselib.utils.ext.logComToFile
import cn.yanhu.baselib.utils.ext.logcom
import com.blankj.utilcode.util.ThreadUtils
import io.agora.rtc2.ClientRoleOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler

class AgoraRtcHandler : IRtcEngineEventHandler() {
    private val mHandlers: MutableList<RtcEventHandler>

    companion object {
        const val TAG = "rtcHandler"
    }

    fun registerEventHandler(handler: RtcEventHandler) {
        if (!mHandlers.contains(handler)) {
            mHandlers.add(handler)
        }
    }

    override fun onError(err: Int) {
        logComToFile("voiceRoomTag","声网sdk发生异常err=$err")
        ThreadUtils.getMainHandler().post {
            for (handler in mHandlers) {
                handler.onError(err)
            }
        }
        super.onError(err)
    }

    override fun onLeaveChannel(stats: RtcStats?) {
        logcom("voiceRoomTag","离开房间=${stats.toString()}")
        super.onLeaveChannel(stats)
    }

    /**Rtc回调
     * @reason
     * 1.USER_OFFLINE_QUIT(0)：用户主动离开。
    2.USER_OFFLINE_DROPPED(1)：因过长时间收不到对方数据包，超时掉线。**Note:** 由于 SDK 使用的是不可靠通道，也有可能对方主动离开本方没收到对方离开消息而误判为超时掉线。在 Android 系统上，用户通过手势滑动杀死 app 后，因系统限制，SDK 会报告 USER_OFFLINE_DROPPED(1)，而非报告 USER_OFFLINE_QUIT(0)。
    3.USER_OFFLINE_BECOME_AUDIENCE(2)：直播场景下，用户身份从主播切换为观众
     */
    override fun onUserOffline(uid: Int, reason: Int) {
        super.onUserOffline(uid, reason)
        ThreadUtils.getMainHandler().post {
            for (handler in mHandlers) {
                Log.d(TAG, "onUserOffline----uid=$uid")
                handler.onUserOffline(uid, reason)
            }
        }

    }


    /**
     * Token 服务即将过期回调。
     */
    override fun onTokenPrivilegeWillExpire(token: String?) {
        super.onTokenPrivilegeWillExpire(token)
        ThreadUtils.getMainHandler().post {
            for (handler in mHandlers) {
                Log.d(TAG, "uid, reason----token=$token")
                handler.onTokenExpire()
            }
        }
    }

    /**
     * token 过期
     */
    override fun onRequestToken() {
        super.onRequestToken()
        ThreadUtils.getMainHandler().post {
            for (handler in mHandlers) {
                handler.onTokenExpire()
            }
        }
    }

    /**Rtc回调
     * 有主播加入频道
     */
    override fun onUserJoined(uid: Int, elapsed: Int) {
        super.onUserJoined(uid, elapsed)
        ThreadUtils.getMainHandler().post {
            logComToFile("voiceRoomTag", "监听到有用户加入房间，耗时$elapsed 毫秒")
            for (handler in mHandlers) {
                Log.d(TAG, "onUserJoined----uid=$uid")
                handler.onUserJoined(uid)
            }
        }
    }

    override fun onAudioSubscribeStateChanged(
        channel: String?,
        uid: Int,
        oldState: Int,
        newState: Int,
        elapseSinceLastState: Int,
    ) {
        super.onAudioSubscribeStateChanged(channel, uid, oldState, newState, elapseSinceLastState)
    }

    override fun onLocalAudioStateChanged(state: Int, error: Int) {
        super.onLocalAudioStateChanged(state, error)
    }

    override fun onUserMuteAudio(uid: Int, muted: Boolean) {
        super.onUserMuteAudio(uid, muted)
        ThreadUtils.getMainHandler().post {
            for (handler in mHandlers) {
                Log.d(TAG, "onUserMuteAudio----uid=$uid muted=$muted")
                handler.onUserMuteAudio(uid, muted)
            }
        }

    }



    override fun onClientRoleChanged(
        oldRole: Int,
        newRole: Int,
        newRoleOptions: ClientRoleOptions?,
    ) {
        super.onClientRoleChanged(oldRole, newRole, newRoleOptions)
        ThreadUtils.getMainHandler().post {
            for (handler in mHandlers) {
                Log.d(TAG, "onClientRoleChanged-----oldRole = $oldRole newRole=$newRole")
                handler.onClientRoleChanged(oldRole, newRole)
            }
        }
    }

    override fun onClientRoleChangeFailed(reason: Int, currentRole: Int) {
        super.onClientRoleChangeFailed(reason, currentRole)
        Log.d(TAG, "onClientRoleChangeFailed-----reason = $reason currentRole=$currentRole")
    }

    fun removeEventHandler(handler: RtcEventHandler) {
        mHandlers.remove(handler)
    }
    fun clearEventHandler() {
        mHandlers.clear()
    }
    override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
        ThreadUtils.getMainHandler().post {
            for (handler in mHandlers) {
                Log.d(TAG, "onJoinChannelSuccess-----channel = $channel uid=$uid")
                handler.onRtcJoinChannelSuccess(channel, uid, elapsed)
            }
        }
    }

    override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
        ThreadUtils.getMainHandler().post {
            for (handler in mHandlers) {
                handler.onRtcRemoteVideoStateChanged(uid, state, reason, elapsed)
            }
        }
    }

    override fun onUserEnableVideo(uid: Int, enabled: Boolean) {
        super.onUserEnableVideo(uid, enabled)
        ThreadUtils.getMainHandler().post {
            for (handler in mHandlers) {
                handler.onUserEnableVideo(uid, enabled)
            }
        }
    }

    override fun onUserMuteVideo(uid: Int, muted: Boolean) {
        super.onUserMuteVideo(uid, muted)
        ThreadUtils.getMainHandler().post {
            for (handler in mHandlers) {
                handler.onUserMuteVideo(uid, muted)
            }
        }
    }

    override fun onRtcStats(stats: IRtcEngineEventHandler.RtcStats) {
        ThreadUtils.getMainHandler().post {
            for (handler in mHandlers) {
                handler.onRtcStats(stats)
            }
        }
    }


    override fun onAudioVolumeIndication(speakers: Array<AudioVolumeInfo>, totalVolume: Int) {
        ThreadUtils.getMainHandler().post {
            for (handler in mHandlers) {
                handler.onRtcAudioVolumeIndication(speakers, totalVolume)
            }
        }
    }

    override fun onAudioMixingStateChanged(state: Int, reason: Int) {
        ThreadUtils.getMainHandler().post {
            for (handler in mHandlers) {
                handler.onAudioMixingStateChanged(state, reason)
            }
        }
    }
    override fun onAudioEffectFinished(soundId: Int) {
        super.onAudioEffectFinished(soundId)
    }

    override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
        super.onNetworkQuality(uid, txQuality, rxQuality)
        ThreadUtils.getMainHandler().post {
            for (handler in mHandlers) {
                handler.onNetworkQuality(uid, txQuality, rxQuality)
            }
        }
    }

    override fun onNetworkTypeChanged(type: Int) {
        super.onNetworkTypeChanged(type)
        ThreadUtils.getMainHandler().post {
            for (handler in mHandlers) {
                handler.onNetworkTypeChanged(type)
            }
        }
    }

    override fun onLocalVideoStateChanged(
        source: Constants.VideoSourceType?,
        state: Int,
        error: Int
    ) {
        super.onLocalVideoStateChanged(source, state, error)
        ThreadUtils.getMainHandler().post {
            for (handler in mHandlers) {
                handler.onLocalVideoStateChanged(source, state, error)
            }
        }
    }


    init {
        mHandlers = ArrayList()
    }
}