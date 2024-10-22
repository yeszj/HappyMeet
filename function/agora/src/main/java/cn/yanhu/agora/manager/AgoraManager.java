package cn.yanhu.agora.manager;

import static cn.yanhu.baselib.utils.ext.LogExtKt.logcom;
import static io.agora.rtc2.Constants.REMOTE_VIDEO_STATE_FAILED;
import static io.agora.rtc2.Constants.VIDEO_SOURCE_CAMERA_PRIMARY;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;

import androidx.fragment.app.FragmentActivity;

import com.blankj.utilcode.util.ThreadUtils;
import com.hjq.toast.ToastUtils;


import cn.yanhu.agora.listener.IRtcEngineEventHandlerListener;
import cn.yanhu.commonres.manager.AppCacheManager;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.ClientRoleOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IMediaExtensionObserver;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcConnection;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class AgoraManager implements IMediaExtensionObserver {

    // 填入在声网控制台创建项目时生成的 App ID
    public static final int TOKEN_WILL_EXPIRE = 0;//token即将过期
    public static final int TOKEN_EXPIRE = 1;//token已过期
    public static final int USER_SET_UP = 2;//上麦
    public static final int USER_SIT_DOWN = 3;//下麦
    public static final int USER_QUIT = 4;//用户离开房间
    public static final int USER_SHORT_LEAVE = 5;//远端用户超时掉线
    public static final int USER_ERROR_CAMERA_DISABLED = 6;//主播相机设备由于设备策略而无法打开
    public static final int USER_REMOTE_VIDEO_INIT_FINISH = 7;//远端用户视频渲染成功
    public static final int USER_NETWOKR_GOOD = 8;//本地用户网络良好
    public static final int USER_NETWOKR_BAD = 9;//本地用户网络不佳
    public static final int USER_NETWOKR_DOWN = 10;//本地用户网络断开
    public static final int REMOTE_USER_VIDEO_DOWN = 11;//远端用户视频断开
    public static final int REMOTE_USER_VIDEO_LEAVE = 12;//远端用户视频断开
    public static final int REMOTE_USER_VIDEO_UP = 13;//远端用户视频重连

    public static final int USER_REMOTE_VIDEO_PLAY_FAIL = 14;//远端用户视频播放失败
    public String currentRoomID = "";


    private AgoraManager() {

    }

    private static final class AgoraManagerHolder {
        private static final AgoraManager agoraManager = new AgoraManager();
    }

    public static AgoraManager getInstence() {
        return AgoraManagerHolder.agoraManager;
    }


    public static boolean isLiveRoom = false;
    public static boolean isVideoCall = false;

    public RtcEngineEx mRtcEngine;
    public boolean isInitSuccess = false;

    private IRtcEngineEventHandlerListener iRtcEngineEventHandlerListener;

    public void setRtcEngineEventHandlerListener(IRtcEngineEventHandlerListener iRtcEngineEventHandlerListener){
        this.iRtcEngineEventHandlerListener = iRtcEngineEventHandlerListener;
    }

    public void init(Activity baseContext, Integer userRole, View surfaceView) {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = baseContext;
            config.mAppId = "301729ee939d4470b6b60a795e9ccc22";
            config.mEventHandler = mRtcEventHandler;
            config.mExtensionObserver = this;
            // 添加美颜插件
            config.addExtension("AgoraFaceUnityExtension");
            config.mNativeLibPath = AgoraSdkDownloadManager.getSoPath();
            mRtcEngine = (RtcEngineEx) RtcEngineEx.create(config);
            // 启用插件
            int i = mRtcEngine.enableExtension("FaceUnity", "Effect", true);
        } catch (Exception e) {
          // TraceUtils.getInstance().onEventObject("app_agora_fail", e.getMessage());
            ToastUtils.show("直播间初始化异常,请重新进入直播间尝试");
            logcom("声网：" + e.getMessage());
            return;
        }
        mRtcEngine.enableAudioVolumeIndication(2000, 3, false);
        mRtcEngine.adjustPlaybackSignalVolume(128);
        mRtcEngine.adjustRecordingSignalVolume(128);

        // SDK 默认关闭视频。调用 enableVideo 开启视频
        mRtcEngine.enableVideo();
        // 在互动直播中，设置频道场景为 BROADCASTING
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        // 根据实际情况，设置用户角色为 BROADCASTER 或 AUDIENCE
        mRtcEngine.setClientRole(userRole != 0 ? Constants.CLIENT_ROLE_BROADCASTER : Constants.CLIENT_ROLE_AUDIENCE);
        BeautySetManager.getInstance().initExtension(mRtcEngine);
        if (userRole == 1) {
            // 开启本地视频预览。
            mRtcEngine.startPreview();
            BeautySetManager.getInstance().enableBeauty(true);
            mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        }
        //AgoraVideoWriteTest.registerVideoFrameObserver(mRtcEngine);
    }


    public void writeRtcLog(String format, Object... value) {
        if (mRtcEngine != null) {
            mRtcEngine.writeLog(Constants.LogLevel.getValue(Constants.LogLevel.LOG_LEVEL_INFO), format, value);
        }
    }

    public void removeHandler() {
        if (mRtcEngine != null) {
            mRtcEngine.removeHandler(mRtcEventHandler);
        }
    }


    public void setVideoEncoderConfiguration(int width, int height) {
        VideoEncoderConfiguration videoEncoderConfiguration = new VideoEncoderConfiguration();
        videoEncoderConfiguration.dimensions = new VideoEncoderConfiguration.VideoDimensions(width, height);
        mRtcEngine.setVideoEncoderConfiguration(videoEncoderConfiguration);
    }

    //加入声网频道
    public Integer joinChannel(Integer uid, String roomID, String token) {
        logcom("uid:" + uid);
        logcom("channelName:" + roomID);
        logcom("token:" + token);
        currentRoomID = roomID;
        //用户未加入频道，使用 token 加入频道
        ChannelMediaOptions options = new ChannelMediaOptions();
        int joined = mRtcEngine.joinChannel(token, roomID, uid, options);
        logcom("joined:" + joined);
        return joined;
    }

    public void preloadChannel(FragmentActivity context, String roomID, String token) {
        init(context, 0, null);
        logcom("preloadChannel:" + roomID);
        currentRoomID = roomID;
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<Integer>() {
            @Override
            public Integer doInBackground() {
                return mRtcEngine.preloadChannel(token, roomID, AppCacheManager.INSTANCE.getUserId());
            }
            @Override
            public void onSuccess(Integer result) {

            }
        });
    }

    private RtcConnection rtcConnection;

    public Integer joinPkChannel(Integer uid, String roomID, String token, boolean isBroadCast, IRtcEngineEventHandler iRtcEngineEventHandler) {
        // 主播加入对方频道的设置
        rtcConnection = new RtcConnection();
        rtcConnection.channelId = roomID;
        rtcConnection.localUid = uid;
        ChannelMediaOptions channelMediaOptions = new ChannelMediaOptions();
        channelMediaOptions.publishCameraTrack = isBroadCast;
        channelMediaOptions.publishMicrophoneTrack = isBroadCast;
        channelMediaOptions.publishCustomAudioTrack = isBroadCast;
        channelMediaOptions.autoSubscribeVideo = true;
        channelMediaOptions.autoSubscribeAudio = subScribeAudio;
        channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE;
        if (isBroadCast) {
            channelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_ULTRA_LOW_LATENCY;
            channelMediaOptions.isInteractiveAudience = true;
        }
        return mRtcEngine.joinChannelEx(token, rtcConnection, channelMediaOptions, iRtcEngineEventHandler);
    }

    public void leavePkChannel() {
        if (mRtcEngine != null && rtcConnection != null) {
            mRtcEngine.leaveChannelEx(rtcConnection);
        }
        rtcConnection = null;
    }

    public void leaveChannel() {
        if (mRtcEngine != null ) {
            mRtcEngine.leaveChannel();
        }
    }

    private boolean subScribeAudio = false;

    public void subScribeAudio(boolean subscribe) {
        subScribeAudio = subscribe;
        if (rtcConnection != null) {
            ChannelMediaOptions channelMediaOptions = new ChannelMediaOptions();
            channelMediaOptions.autoSubscribeAudio = subscribe;
            mRtcEngine.updateChannelMediaOptionsEx(channelMediaOptions, rtcConnection);
        }
    }


    //设置角色类型  BROADCASTER：主播，AUDIENCE：观众
    public void setClientRole(Integer clientRoleType) {
        if (mRtcEngine != null) {
            mRtcEngine.setClientRole(clientRoleType);
        }
    }

    //用户上麦
    public void setupVideo(int uid, boolean isLocal, View surfaceView) {
        logcom("上麦信息:" + uid + "是否本地" + isLocal);
        if (mRtcEngine != null) {
            //判断哪个座位上麦，麦位是否有人，是否麦位上的人重新连接麦位,防止一个麦位同时存在多人
            if (isLocal) {
                // 开启本地视频预览。
                mRtcEngine.startPreview();
                BeautySetManager.getInstance().enableBeauty(true);
                mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
                setupLocalVideo(true);
                mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
                publishPkVideo();
            } else {
                mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
            }
        }
    }

    private void publishPkVideo() {
        if (rtcConnection != null) {
            ChannelMediaOptions channelMediaOptions = new ChannelMediaOptions();
            // 发布音视频流
            channelMediaOptions.publishCameraTrack = true;
            channelMediaOptions.publishMicrophoneTrack = true;
            channelMediaOptions.publishCustomAudioTrack = false;
            channelMediaOptions.enableAudioRecordingOrPlayout = true;
            channelMediaOptions.autoSubscribeVideo = true;
            channelMediaOptions.autoSubscribeAudio = subScribeAudio;
            channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE;
            mRtcEngine.updateChannelMediaOptionsEx(channelMediaOptions, rtcConnection);
        }

    }

    public void setPkSeatVideo(int uid, View surfaceView, String pkRoomId) {
        if (rtcConnection == null) {
            rtcConnection = new RtcConnection();
            rtcConnection.channelId = pkRoomId;
            rtcConnection.localUid = Integer.parseInt(AppCacheManager.INSTANCE.getUserId());
        }
        mRtcEngine.setupRemoteVideoEx(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid), rtcConnection);
    }

    public void clearRtcConnection() {
        rtcConnection = null;
    }

    //用户下麦
    public void setDownVideo(int uid, boolean isLocal) {
        logcom("下麦信息:" + uid + "是否本地" + isLocal);
        if (mRtcEngine != null) {
            if (isLocal) {
                mRtcEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
                setupLocalVideo(false);
                mRtcEngine.setupLocalVideo(new VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, uid));
                cancelPublishPkVideo();
            } else {
                mRtcEngine.setupRemoteVideo(new VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, uid));
            }
        }
    }

    private void cancelPublishPkVideo() {
        if (rtcConnection != null) {
            ChannelMediaOptions channelMediaOptions = new ChannelMediaOptions();
            // 发布音视频流
            channelMediaOptions.publishCameraTrack = false;
            channelMediaOptions.publishMicrophoneTrack = false;
            channelMediaOptions.publishCustomAudioTrack = false;
            channelMediaOptions.enableAudioRecordingOrPlayout = true;
            channelMediaOptions.autoSubscribeVideo = true;
            channelMediaOptions.autoSubscribeAudio = true;
            channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE;
            channelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY;
            mRtcEngine.updateChannelMediaOptionsEx(channelMediaOptions, rtcConnection);
        }

    }

    public void setupLocalAudio(boolean isOpen) {
        if (mRtcEngine != null) {
            mRtcEngine.enableLocalAudio(isOpen);
        }
    }

    public void setupLocalVideo(boolean isOpen) {
        if (mRtcEngine != null) {
            mRtcEngine.enableLocalVideo(isOpen);
        }
    }

    //开/关用户麦 true：关，false：开
    public Integer muteLocalAudioStream(Boolean switchMike) {
        if (mRtcEngine == null) {
            return 0;
        }
        int status = mRtcEngine.muteLocalAudioStream(switchMike);
        if (status == 0) {
            logcom("开关麦成功 " + switchMike);
        } else {
            logcom("开关麦失败" + status);
        }
        return status;
    }

    //用户已加入频道，调用 renewToken 重新生成 token
    public void renewToken(String token) {
        if (mRtcEngine != null) {
            mRtcEngine.renewToken(token);
        }
    }

    IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {

        //Token过期前30秒回调
        @Override
        public void onTokenPrivilegeWillExpire(String token) {
            logcom("onTokenPrivilegeWillExpire");
            iRtcEngineEventHandlerListener.agoraListener(TOKEN_WILL_EXPIRE, 0);
            super.onTokenPrivilegeWillExpire(token);
        }

        //token过期回调
        @Override
        public void onRequestToken() {
            logcom("onRequestToken");
            iRtcEngineEventHandlerListener.agoraListener(TOKEN_EXPIRE, 0);
            super.onRequestToken();
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            logcom("有人上麦：" + uid);
            if (isInitSuccess) {
                logcom("上麦：" + uid);
                iRtcEngineEventHandlerListener.agoraListener(USER_SET_UP, uid);
            }
            super.onUserJoined(uid, elapsed);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            logcom("onUserOffline：" + uid + "  reason：" + reason);

            if (reason == Constants.USER_OFFLINE_BECOME_AUDIENCE) {//用户下麦
                logcom("下麦：" + uid);
                iRtcEngineEventHandlerListener.agoraListener(USER_SIT_DOWN, uid);

            } else if (reason == 1) {//远端用户超时掉线
                logcom("远端用户超时掉线：" + uid);
                iRtcEngineEventHandlerListener.agoraListener(USER_SHORT_LEAVE, uid);

            } else if (reason == 0) {//用户离开
                logcom("用户离开：" + uid);
                iRtcEngineEventHandlerListener.agoraListener(USER_QUIT, uid);
            }
            super.onUserOffline(uid, reason);
        }

        @Override
        public void onClientRoleChanged(int oldRole, int newRole, ClientRoleOptions newRoleOptions) {
            logcom("切换用户角色成功：" + newRole);
            super.onClientRoleChanged(oldRole, newRole, newRoleOptions);
        }

        @Override
        public void onClientRoleChangeFailed(int reason, int currentRole) {
            logcom("切换用户角色失败：" + reason);
            super.onClientRoleChangeFailed(reason, currentRole);
        }

        @Override
        public void onLocalVideoStateChanged(Constants.VideoSourceType source, int state, int error) {
            super.onLocalVideoStateChanged(source, state, error);
            logcom(String.format("onLocalVideoStateChanged %s %d %d", source.toString(), state, error));
            writeRtcLog("onLocalVideoStateChanged %s %d %d", source.toString(), state, error);
            if (source.getValue() == VIDEO_SOURCE_CAMERA_PRIMARY && state == 3 && error == 4) {
                mRtcEngine.enableLocalVideo(true);
                iRtcEngineEventHandlerListener.agoraListener(USER_ERROR_CAMERA_DISABLED, -1);
            }

            if (source.getValue() == VIDEO_SOURCE_CAMERA_PRIMARY && state == 1 && error == 0) {

            }
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed);
            logcom(String.format("远端视频回调 uid=%d state=%d reason=%d elapsed=%d", uid, state, reason, elapsed));
            writeRtcLog("远端视频回调 uid=%d state=%d reason=%d elapsed=%d", uid, state, reason, elapsed);
            if (state == 2) {
                if (reason == 2) {
                    logcom("远端视频恢复正常播放   uid：" + uid + "————elapsed：" + elapsed);
                    iRtcEngineEventHandlerListener.agoraListener(REMOTE_USER_VIDEO_UP, uid);

                } else {
                    logcom("远端视频启用   uid：" + uid + "————elapsed：" + elapsed);
                    iRtcEngineEventHandlerListener.agoraListener(USER_REMOTE_VIDEO_INIT_FINISH, uid);
                }

            } else if (state == REMOTE_VIDEO_STATE_FAILED) {
                //远端视频流播放失败
                iRtcEngineEventHandlerListener.agoraListener(USER_REMOTE_VIDEO_PLAY_FAIL, uid);
            } else if ((reason == 5 || reason == 7 || reason == 8) || state >= 3) {
                logcom("远端视频卡顿、停止   uid：" + uid + "————elapsed：" + elapsed);
                iRtcEngineEventHandlerListener.agoraListener(REMOTE_USER_VIDEO_DOWN, uid);
            }
        }

        @Override
        public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
            super.onFirstRemoteVideoFrame(uid, width, height, elapsed);
            logcom("远端视频初始化完成   uid：" + uid + "————elapsed：" + elapsed);
            iRtcEngineEventHandlerListener.agoraListener(USER_REMOTE_VIDEO_INIT_FINISH, uid);
        }

        @Override
        public void onFirstLocalVideoFrame(Constants.VideoSourceType source, int width, int height, int elapsed) {
            super.onFirstLocalVideoFrame(source, width, height, elapsed);
            iRtcEngineEventHandlerListener.agoraListener(USER_REMOTE_VIDEO_INIT_FINISH, -1);
        }

        @Override
        public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
            super.onNetworkQuality(uid, txQuality, rxQuality);
            logcom("网络状态，uid：" + uid + "————txQuality：" + txQuality + "————rxQuality：" + rxQuality);
            if (uid == 0) {
                if (txQuality >= 4 || rxQuality >= 4) {
                    if (txQuality == 6 || rxQuality == 6) {
                        iRtcEngineEventHandlerListener.agoraListener(USER_NETWOKR_DOWN, uid);
                    } else {
                        iRtcEngineEventHandlerListener.agoraListener(USER_NETWOKR_BAD, uid);
                    }
                } else {
                    iRtcEngineEventHandlerListener.agoraListener(USER_NETWOKR_GOOD, uid);

                }
            }
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            logcom("离开频道");
            super.onLeaveChannel(stats);
        }

        @Override
        public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
            super.onAudioVolumeIndication(speakers, totalVolume);
            if (speakers != null && speakers.length > 0) {
                int uid = speakers[0].uid;
                if (uid != 0) {
                    iRtcEngineEventHandlerListener.onAudioVolumeIndication(speakers, totalVolume);
                }
            }
        }
    };

    public void onDestory() {
        logcom("离开");
        isLiveRoom = false;
        if (mRtcEngine != null) {
            removeHandler();
            setupLocalAudio(false);
            setupLocalVideo(false);
            mRtcEngine.leaveChannel();
            mRtcEngine.stopPreview();
            mRtcEngine.disableVideo();
            RtcEngine.destroy();
        }
        mRtcEngine = null;
        isInitSuccess = false;
        iRtcEngineEventHandlerListener = null;
    }


    @Override
    public void onEvent(String provider, String extension, String key, String value) {
        logcom("agora-onEvent", provider + "——————" + extension + "————————" + key + "————————" + value + "————————");
    }


    @Override
    public void onStarted(String provider, String extension) {
        logcom("agora-onStarted", provider + "——————" + extension);

    }

    @Override
    public void onStopped(String provider, String extension) {
        logcom("agora-onStopped", provider + "——————" + extension);

    }

    @Override
    public void onError(String provider, String extension, int error, String message) {
        logcom("agora-onError", provider + "——————" + extension + "————————" + error + "————————" + message + "————————");


    }
}
