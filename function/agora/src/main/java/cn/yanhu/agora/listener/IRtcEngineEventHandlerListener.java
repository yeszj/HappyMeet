package cn.yanhu.agora.listener;

import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;

public interface IRtcEngineEventHandlerListener {

   void agoraListener(int type,int uid);
   void onAudioVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume);
   void onLocalVideoStats(Constants.VideoSourceType source, IRtcEngineEventHandler.LocalVideoStats stats);
}
