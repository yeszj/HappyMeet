package cn.huanyuan.sweetlove.func.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import cn.yanhu.agora.ui.imphone.VideoPhoneActivity
import cn.yanhu.agora.ui.liveRoom.live.LiveRoomActivity
import com.blankj.utilcode.util.ActivityUtils
import kotlin.jvm.java

/**
 * @author: zhengjun
 * created: 2025/7/11
 * desc:
 */
class LocalRecordingService : Service() {
    private var type = 1

    override fun onCreate() {
        super.onCreate()

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        type = intent.getIntExtra("type", 1)
        val notification = this.defaultNotification
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                this.startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {
                this.startForeground(NOTIFICATION_ID, notification)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val defaultNotification: Notification
        get() {
            val appInfo = this.applicationContext.applicationInfo
            val name =
                this.applicationContext.packageManager.getApplicationLabel(appInfo)
                    .toString()
            var icon = appInfo.icon

            try {
                val iconBitMap =
                    BitmapFactory.decodeResource(this.applicationContext.resources, icon)
                if (iconBitMap == null || iconBitMap.getByteCount() == 0) {
                    icon = cn.huanyuan.sweetlove.R.mipmap.icon_splash_logo
                }
            } catch (ex: Exception) {
                icon = cn.huanyuan.sweetlove.R.mipmap.icon_splash_logo
                ex.printStackTrace()
            }
            var intent: Intent? = null
            val topActivity = ActivityUtils.getTopActivity()
            if (type == 4) {
                intent = Intent(
                    topActivity,
                    VideoPhoneActivity::class.java
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            } else {
                intent = Intent(
                    topActivity,
                    LiveRoomActivity::class.java
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }

            val requestCode = System.currentTimeMillis().toInt()
            val activityPendingIntent = PendingIntent.getActivity(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder: Notification.Builder?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(
                    CHANNEL_ID,
                    name,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val mNotificationManager =
                    this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                mNotificationManager.createNotificationChannel(mChannel)
                builder = Notification.Builder(
                    this,
                    CHANNEL_ID
                )
            } else {
                builder = Notification.Builder(this)
            }

            builder.setContentTitle("暖遇正在通话中 ...")
                .setContentText("点击返回暖遇")
                .setContentIntent(activityPendingIntent)
                .setAutoCancel(true)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(icon)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setWhen(System.currentTimeMillis())

            val iconObj =
                Icon.createWithResource(this, icon)
            val action =
                Notification.Action.Builder(iconObj, "返回暖遇", activityPendingIntent)
                    .build()
            builder.addAction(action)

            return builder.build()
        }

    companion object {
        private const val NOTIFICATION_ID = 20250808
        private const val CHANNEL_ID = "im_call_audio"
    }
}