package cn.yanhu.agora.service

import android.content.Context
import android.content.Intent
import android.os.Build
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.commonres.utils.PermissionXUtils

/**
 * @author: zhengjun
 * created: 2025/9/10
 * desc:
 */
object LocalServiceManager {
    fun startLocalService(context: Context) {
        if (PermissionXUtils.hasLocalServicePermission(context)) {
            val intent = Intent(context, LocalRecordingService::class.java)
            intent.putExtra("type", AgoraManager.callType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    fun stopLocalService(context: Context) {
        if (PermissionXUtils.hasLocalServicePermission(context)){
            val intent = Intent(context, LocalRecordingService::class.java)
            context.stopService(intent)
        }
    }
}