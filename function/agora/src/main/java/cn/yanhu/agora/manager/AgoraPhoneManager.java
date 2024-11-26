package cn.yanhu.agora.manager;

import static cn.yanhu.baselib.utils.ext.LogExtKt.logcom;
import static io.agora.rtc2.Constants.VIDEO_SOURCE_CAMERA_PRIMARY;

import android.content.Context;
import android.view.SurfaceView;


import cn.yanhu.commonres.bean.ChatCallResponseInfo;
import cn.yanhu.agora.listener.IRtcEngineEventHandlerListener;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.ClientRoleOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IMediaExtensionObserver;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class AgoraPhoneManager implements IMediaExtensionObserver {

    // 填入在声网控制台创建项目时生成的 App ID
    public static final int TOKEN_WILL_EXPIRE = 0;//token即将过期
    public static final int TOKEN_EXPIRE = 1;//token已过期
    public static final int TO_USER_LINE = 2;//对方连接通话
    public static final int TO_USER_OFFLINE_QUIT = 3;//对方结束通话
    public static final int TO_USER_MIC_OPEN = 4001;//远端用户麦克风状态打开
    public static final int TO_USER_MIC_CLOSE = 4002;//远端用户麦克风状态关闭
    public static final int TO_USER_VIDEO_OPEN = 5001;//远端用户摄像头状态打开
    public static final int TO_USER_VIDEO_CLOSE = 5002;//远端用户摄像头状态关闭
    public static final int USER_ERROR_CAMERA_DISABLED = 6;//主播相机设备由于设备策略而无法打开
    public static final int USER_LOCAL_VIDEO_INIT_FINISH = 7;//本地视频渲染成功
    public static final int USER_REMOTE_VIDEO_INIT_FINISH = 8;//远端用户视频渲染成功

    private RtcEngine mRtcEngine;

    private ChatCallResponseInfo userInfo;

    public void setUserInfo(ChatCallResponseInfo userInfo) {
        this.userInfo = userInfo;
    }

    public ChatCallResponseInfo getUserInfo() {
        return userInfo;
    }

    private IRtcEngineEventHandlerListener iRtcEngineEventHandlerListener;

    public void setRtcEngineEventHandlerListener(IRtcEngineEventHandlerListener iRtcEngineEventHandlerListener) {
        this.iRtcEngineEventHandlerListener = iRtcEngineEventHandlerListener;
    }

    private static final class AgoraPhoneManagerHolder {
        static final AgoraPhoneManager agoraPhoneManager = new AgoraPhoneManager();
    }

    public static AgoraPhoneManager getInstance() {
        return AgoraPhoneManagerHolder.agoraPhoneManager;
    }

    public boolean isInit() {
        return mRtcEngine != null;
    }

    public void switchCamera() {
        mRtcEngine.switchCamera();
    }

    public void init(Context baseContext,boolean isVideo) {
        mRtcEngine = RtcEngineInit.INSTANCE.getMRtcEngine();
        if (mRtcEngine==null){
            RtcEngine.destroy();
            mRtcEngine = RtcEngineInit.INSTANCE.initRtcEngine(baseContext);
            if (mRtcEngine==null){
                return;
            }
        }
        if (iRtcEngineEventHandlerListener!=null){
            mRtcEngine.addHandler(mRtcEventHandler);
        }

        // 在互动直播中，设置频道场景为 BROADCASTING
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);

        // 根据实际情况，设置用户角色为 BROADCASTER 或 AUDIENCE
        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        mRtcEngine.adjustPlaybackSignalVolume(128);
        mRtcEngine.adjustRecordingSignalVolume(128);
        mRtcEngine.enableLocalAudio(true);
        if(isVideo){
            // SDK 默认关闭视频。调用 enableVideo 开启视频
            setVideoEncoderConfiguration(720,1280);
            mRtcEngine.enableVideo();
            BeautySetManager.getInstance().initExtension(mRtcEngine);

        }
    }

    public void setVideoEncoderConfiguration(int width,int height){
        VideoEncoderConfiguration videoEncoderConfiguration = new VideoEncoderConfiguration();
        videoEncoderConfiguration.dimensions =new VideoEncoderConfiguration.VideoDimensions(width,height);
        mRtcEngine.setVideoEncoderConfiguration(videoEncoderConfiguration);
    }

    //加入声网频道
    public Integer joinChannel(Integer uid, String roomID, String token) {
        logcom("uid:" + uid);
        logcom("channelName:" + roomID);
        logcom("token:" + token);

        //用户未加入频道，使用 token 加入频道
        ChannelMediaOptions options = new ChannelMediaOptions();
        int joined = mRtcEngine.joinChannel(token, roomID, uid, options);
        logcom("joined:" + joined);
        return joined;
    }

    //本地重载
    public void setupLocalVideo(SurfaceView surfaceView) {
        logcom("视频用户信息:" + 0);
        // 开启本地视频预览。
        mRtcEngine.startPreview();
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        BeautySetManager.getInstance().enableBeauty(true);

    }

    //连接远端用户
    public void setupRemoteVideo(int uid, SurfaceView surfaceView) {
        logcom("连接远端用户，视频用户信息:" + uid);
        if (surfaceView==null){
            return;
        }
        if (mRtcEngine!=null){
            mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        }

    }

    //开/关用户麦
    public Integer muteLocalAudioStream(Boolean switchMike) {
        int status = mRtcEngine.muteLocalAudioStream(switchMike);
        if (status == 0) {
            logcom("开关麦成功");
        } else {
            logcom("开关麦失败" + status);
        }
        return status;
    }

    //开/关用户摄像头
    public Integer muteLocalVideoStream(Boolean switchMike) {
        int status = mRtcEngine.muteLocalVideoStream(switchMike);
        if (status == 0) {
            logcom("开关麦成功");
        } else {
            logcom("开关麦失败" + status);
        }
        return status;
    }

    //用户已加入频道，调用 renewToken 重新生成 token
    public void renewToken(String token) {
        if (mRtcEngine!=null){
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

        //有用户加入通话
        @Override
        public void onUserJoined(int uid, int elapsed) {
            logcom("用户加入通话：" + uid);
            iRtcEngineEventHandlerListener.agoraListener(TO_USER_LINE, uid);
        }

        //远端用户离开通话
        @Override
        public void onUserOffline(int uid, int reason) {
            logcom("用户结束通话：" + uid + "  reason：" + reason);
            iRtcEngineEventHandlerListener.agoraListener(TO_USER_OFFLINE_QUIT, uid);
            super.onUserOffline(uid, reason);
        }

        //用户角色类型切换
        @Override
        public void onClientRoleChanged(int oldRole, int newRole, ClientRoleOptions newRoleOptions) {
            logcom("切换用户角色成功：" + newRole);
            super.onClientRoleChanged(oldRole, newRole, newRoleOptions);
        }

        //用户角色类型切换失败
        @Override
        public void onClientRoleChangeFailed(int reason, int currentRole) {
            logcom("切换用户角色失败：" + reason);
            super.onClientRoleChangeFailed(reason, currentRole);
        }

        //远端用户麦克风状态监听
        @Override
        public void onUserMuteAudio(int uid, boolean muted) {
            super.onUserMuteAudio(uid, muted);
            logcom("远端用户麦克风状态监听:" + uid);
            iRtcEngineEventHandlerListener.agoraListener(muted ? TO_USER_MIC_CLOSE : TO_USER_MIC_OPEN, uid);
        }

        //远端用户摄像头状态监听
        @Override
        public void onUserMuteVideo(int uid, boolean muted) {
            super.onUserMuteVideo(uid, muted);
            logcom("远端用户摄像头状态监听:" + uid + muted);
            iRtcEngineEventHandlerListener.agoraListener(muted ? TO_USER_VIDEO_CLOSE : TO_USER_VIDEO_OPEN, uid);
        }

        //本地摄像头初始化完毕
        @Override
        public void onFirstLocalVideoFrame(Constants.VideoSourceType source, int width, int height, int elapsed) {
            super.onFirstLocalVideoFrame(source, width, height, elapsed);
            logcom("本地摄像头初始化完毕 ————elapsed：" + source + "————" + elapsed);

            iRtcEngineEventHandlerListener.agoraListener(USER_LOCAL_VIDEO_INIT_FINISH, -1);
        }

        //远端用户摄像头初始化完毕
        @Override
        public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
            super.onFirstRemoteVideoFrame(uid, width, height, elapsed);
            logcom("远端用户摄像头初始化完毕   uid：" + uid + "————elapsed：" + elapsed);
            iRtcEngineEventHandlerListener.agoraListener(USER_REMOTE_VIDEO_INIT_FINISH, uid);
        }

        //本地摄像头状态监听
        @Override
        public void onLocalVideoStateChanged(Constants.VideoSourceType source, int state, int error) {
            super.onLocalVideoStateChanged(source, state, error);
            logcom(String.format("onLocalVideoStateChanged %s %d %d", source.toString(), state, error));

            if (source.getValue() == VIDEO_SOURCE_CAMERA_PRIMARY && state == 3 && error == 4) {
                mRtcEngine.enableLocalVideo(true);
                iRtcEngineEventHandlerListener.agoraListener(USER_ERROR_CAMERA_DISABLED, -1);
            }

            if (source.getValue() == VIDEO_SOURCE_CAMERA_PRIMARY && state == 1 && error == 0) {

            }
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            logcom("离开频道");
            super.onLeaveChannel(stats);

        }
    };


    public void onDestory() {
        if (mRtcEngine != null) {
            logcom("离开");
            mRtcEngine.enableLocalAudio(false);
            mRtcEngine.enableLocalVideo(false);
            mRtcEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
            mRtcEngine.stopPreview();
            mRtcEngine.disableVideo();
            userInfo = null;
            mRtcEngine.removeHandler(mRtcEventHandler);
            iRtcEngineEventHandlerListener = null;
            mRtcEngine.leaveChannel();
           // mRtcEngine = null;
           // RtcEngine.destroy();
        }
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
