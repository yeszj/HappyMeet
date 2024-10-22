package cn.yanhu.imchat.custom.message.push;

import android.content.Context;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.hyphenate.notification.EMNotificationMessage;
import com.hyphenate.notification.core.EMNotificationIntentReceiver;

import cn.yanhu.commonres.router.PageIntentUtil;

public class EasemobReceiver extends EMNotificationIntentReceiver {
    @Override
    public void onNotifyMessageArrived(Context context, EMNotificationMessage notificationMessage) {
        if (!notificationMessage.isNeedNotification()) {
            String params = notificationMessage.getExtras(); // 判断是透传消息，获取附加字段去处理。
        }
    }

    @Override
    public void onNotificationClick(Context context, EMNotificationMessage notificationMessage) {
        // 实现自己的通知点击跳转逻辑。
        int openType = notificationMessage.getOpenType();
        if (openType==2){
            ThreadUtils.getMainHandler().post(() -> {
                String openActivity = notificationMessage.getOpenActivity();
                PageIntentUtil.url2Page(ActivityUtils.getTopActivity(),openActivity);
            });
        }else  if (openType==1){
            ThreadUtils.getMainHandler().post(() -> {
                String openUrl = notificationMessage.getOpenUrl();
                PageIntentUtil.url2Page(ActivityUtils.getTopActivity(),openUrl);
            });
        }else{
            super.onNotificationClick(context, notificationMessage);
        }
    }
}
