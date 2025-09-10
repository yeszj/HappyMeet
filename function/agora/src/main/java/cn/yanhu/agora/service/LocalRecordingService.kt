package cn.yanhu.agora.service

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
import cn.yanhu.commonres.R
import cn.yanhu.agora.ui.imphone.VideoPhoneActivity
import cn.yanhu.agora.ui.liveRoom.live.LiveRoomActivity
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import kotlin.jvm.java

/**
 * @author: zhengjun
 * created: 2025/7/11
 * desc:
 */
class LocalRecordingService : Service() {

    private var type = 1

    companion object {
        private const val NOTIFICATION_ID = 20250808
        private const val CHANNEL_ID = "im_call_audio"
    }

    override fun onCreate() {
        super.onCreate()
        // 立即启动前台服务，挂简易通知
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(NOTIFICATION_ID, buildSimpleNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, buildSimpleNotification())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent==null) return super.onStartCommand(intent, flags, startId)
        type = intent.getIntExtra("type", 1)

        // 异步更新最终通知
        Thread {
            try {
                val notification = buildFinalNotification()
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NOTIFICATION_ID, notification)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }.start()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /** 简易通知，快速挂在前台 */
    private fun buildSimpleNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                AppUtils.getAppName(),
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(AppUtils.getAppName())
            .setContentText("服务启动中…")
            .setSmallIcon(R.mipmap.icon_splash_logo)
            .setOngoing(true)
            .build()
    }

    /** 最终通知 */
    private fun buildFinalNotification(): Notification {
        var icon = applicationInfo.icon
        try {
            val bitmap = BitmapFactory.decodeResource(resources, icon)
            if (bitmap == null || bitmap.byteCount == 0) {
                icon = R.mipmap.icon_splash_logo
            }
        } catch (ex: Exception) {
            icon = R.mipmap.icon_splash_logo
        }

        val topActivity = ActivityUtils.getTopActivity()
        val intent = if (type == 4) {
            Intent(topActivity, VideoPhoneActivity::class.java)
        } else {
            Intent(topActivity, LiveRoomActivity::class.java)
        }.apply { addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(CHANNEL_ID, AppUtils.getAppName(), NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }

        builder.setContentTitle(if (type == 4) "正在通话中 ..." else "正在直播中 ...")
            .setContentText("点击返回")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .setSmallIcon(icon)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setWhen(System.currentTimeMillis())

        val action = Notification.Action.Builder(
            Icon.createWithResource(this, icon),
            "返回暖遇",
            pendingIntent
        ).build()
        builder.addAction(action)

        return builder.build()
    }
}
