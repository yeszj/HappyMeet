package cn.yanhu.imchat.custom.message.push.vivo;

import android.content.Context;

import com.hyphenate.push.platform.vivo.EMVivoMsgReceiver;
import com.vivo.push.model.UPSNotificationMessage;

import java.util.Map;

import cn.yanhu.imchat.custom.message.push.PushManager;

public class MyVivoMsgReceiver extends EMVivoMsgReceiver {
    @Override
    public void onNotificationMessageClicked(Context context, UPSNotificationMessage upsNotificationMessage) {
        Map<String, String> map = upsNotificationMessage.getParams();
        if (!map.isEmpty()) {
            String sendUserId = map.get("f");
            String groupId = map.get("g");

            PushManager.clickOfflinePush(sendUserId,groupId);
        }
    }
}
