package cn.yanhu.imchat.custom.message.push.xiaomi;

import android.content.Context;

import com.hyphenate.push.platform.mi.EMMiMsgReceiver;
import com.hyphenate.util.EMLog;
import com.xiaomi.mipush.sdk.MiPushMessage;

import org.json.JSONObject;

import cn.yanhu.imchat.custom.message.push.PushManager;


/**
 * 获取有关小米音视频推送消息
 */
public class MiMsgReceiver extends EMMiMsgReceiver {

    static private String TAG = "MiMsgReceiver";

    public void onNotificationMessageClicked(Context context, MiPushMessage message) {
        EMLog.i(TAG, "onNotificationMessageClicked is called. " + message.toString());
        String extStr = message.getContent();
        EMLog.i(TAG, "onReceivePassThroughMessage get extras: " + extStr);
        try {
            JSONObject extras = new JSONObject(extStr);
            String sendUserId = extras.getString("f");
            String groupId = extras.getString("g");
            PushManager.clickOfflinePush(sendUserId,groupId);
        } catch (Exception e){
            e.getStackTrace();
        }
        super.onNotificationMessageClicked(context, message);
    }
}
