package cn.yanhu.imchat.manager;

import android.content.Context;

import com.heytap.msp.push.HeytapPushManager;
import com.hihonor.push.sdk.HonorPushClient;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.delegate.EaseCustomAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseFileAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseLocationAdapterDelegate;
import com.hyphenate.easeui.manager.EaseMessageTypeSetManager;
import com.hyphenate.push.EMPushConfig;
import com.hyphenate.push.EMPushHelper;
import com.hyphenate.push.EMPushType;
import com.hyphenate.push.PushListener;
import com.hyphenate.util.EMLog;

import cn.yanhu.imchat.custom.message.chatEmojiView.ChatEmojiNewAdapterDelegate;
import cn.yanhu.imchat.custom.message.chatGifEmojiView.ChatGifEmojiNewAdapterDelegate;
import cn.yanhu.imchat.custom.message.chatRelatiionshipView.ChatRelationshipViewAdapterDelegate;
import cn.yanhu.imchat.custom.message.chataddfriendview.ChatAddFriendViewAdapterDelegate;
import cn.yanhu.imchat.custom.message.chatalertview.ChatAlertNewAdapterDelegate;
import cn.yanhu.imchat.custom.message.chatgiftview.ChatGiftNewAdapterDelegate;
import cn.yanhu.imchat.custom.message.chatimageview.ChatImageAdapterDelegate;
import cn.yanhu.imchat.custom.message.chatphoneview.ChatPhoneNewAdapterDelegate;
import cn.yanhu.imchat.custom.message.chattextview.ChatTextAdapterDelegate;
import cn.yanhu.imchat.custom.message.chatvideoview.ChatVideoAdapterDelegate;
import cn.yanhu.imchat.custom.message.chatvoiceview.ChatVoiceAdapterDelegate;
import cn.yanhu.imchat.custom.message.group.centerTipsView.CenterTipsViewAdapterDelegate;
import cn.yanhu.imchat.custom.message.group.inviteEnterGroup.InviteEnterGroupViewAdapterDelegate;
import cn.yanhu.imchat.custom.message.inviteSendGiftView.InviteSendGiftViewAdapterDelegate;


public class EMInitUtils {

    public static boolean isInit = false;


    //环信初始化
    public static boolean initIM(Context activity) {

        //初始化
        EMOptions options = new EMOptions();
        options.setRegardImportedMsgAsRead(true);
        options.setAutoLogin(false);
        options.setAppKey("1112200309107289#forlove");
        EMPushConfig.Builder builder = new EMPushConfig.Builder(activity);
        // 设置支持哪家手机厂商推送。
        builder.enableMiPush("2882303761520255699", "5202025566699")
                .enableOppoPush("d4180908bd974b14bf1818b29a2a5915", "0175984658864df0a5b9d038bc40da78")
                .enableVivoPush()
                //开发者需要调用该方法开启华为推送。
                .enableHWPush();
        // 将 pushconfig 设置为 ChatOptions.
        options.setPushConfig(builder.build());

        //EaseIM 初始化
        if (EaseIM.getInstance().init(activity, options)) {
            //在做打包混淆时，关闭 debug 模式，避免消耗不必要的资源
            EMClient.getInstance().setDebugMode(true);
            //oppo推送初始化
            HeytapPushManager.init(activity, true);
            // 荣耀推送 7.0.41.301及以上版本
            // 无需调用init初始化SDK即可调用
            boolean isSupport = HonorPushClient.getInstance().checkSupportHonorPush(activity);
            if (isSupport) {
                // true，调用初始化接口时SDK会同时进行异步请求PushToken。会触发HonorMessageService.onNewToken(String)回调。
                // false，不会异步请求PushToken，需要应用主动请求获取PushToken。
                HonorPushClient.getInstance().init(activity, false);
            }
            //推送初始化监听
            EMPushHelper.getInstance().setPushListener(new PushListener() {
                @Override
                public void onError(EMPushType pushType, long errorCode) {
                    EMLog.e("PushClient", "Push client occur a error: " + pushType + " - " + errorCode);
                    // TODO: 开发者会在这个回调中收到使用推送的相关错误信息，各推送类型的 error code 开发者可以自己去各推送平台官网查询错误原因。
                }

                @Override
                public boolean isSupportPush(EMPushType pushType, EMPushConfig pushConfig) {
                    return super.isSupportPush(pushType, pushConfig);
                    // TODO：开发者可以复写该方法控制设备是否支持某推送的判断。
                }
            });

            //注册对话类型
            EaseMessageTypeSetManager.getInstance()
                    .addMessageType(ChatRelationshipViewAdapterDelegate.class)
                    .addMessageType(InviteSendGiftViewAdapterDelegate.class)
                    .addMessageType(ChatGifEmojiNewAdapterDelegate.class)
                    .addMessageType(ChatEmojiNewAdapterDelegate.class)
                    .addMessageType(CenterTipsViewAdapterDelegate.class)
                    .addMessageType(InviteEnterGroupViewAdapterDelegate.class)
                    .addMessageType(ChatTextAdapterDelegate.class)             //文本
                    .addMessageType(ChatAlertNewAdapterDelegate.class)         //提示
                    .addMessageType(ChatAddFriendViewAdapterDelegate.class)    //申请好友
                    .addMessageType(ChatPhoneNewAdapterDelegate.class)         //通话
                    .addMessageType(ChatGiftNewAdapterDelegate.class)          //礼物
                    .addMessageType(ChatVideoAdapterDelegate.class)            //视频
                    .addMessageType(ChatImageAdapterDelegate.class)            //图片
                    .addMessageType(ChatVoiceAdapterDelegate.class)            //声音
                    .addMessageType(EaseFileAdapterDelegate.class)             //文件
                    .addMessageType(EaseLocationAdapterDelegate.class)         //定位
                    .addMessageType(EaseCustomAdapterDelegate.class)           //自定义消息
                    .setDefaultMessageType(ChatTextAdapterDelegate.class);     //文本
            if (EMClient.getInstance().isSdkInited()) {
                isInit = true;
                return true;

            }
        }
        return false;
    }


}
