// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.
package cn.yanhu.imchat.config

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Environment
import android.text.TextUtils
import cn.yanhu.imchat.push.PushUserInfoProvider
import com.netease.nimlib.sdk.NotificationFoldStyle
import com.netease.nimlib.sdk.SDKOptions
import com.netease.nimlib.sdk.StatusBarNotificationConfig
import com.netease.nimlib.sdk.StatusBarNotificationFilter
import com.netease.nimlib.sdk.mixpush.MixPushConfig
import com.netease.yunxin.kit.common.utils.ScreenUtils.getDisplayWidth
import java.io.IOException

/** Nim SDK config info  */
object NimSDKOptionConfig {
    const val NOTIFY_SOUND_KEY = "android.resource://com.netease.yunxin.app.im/raw/msg"
    const val LED_ON_MS = 1000
    const val LED_OFF_MS = 1500
    inline fun <reified T : Activity>getSDKOptions(context: Context, appKey: String?): SDKOptions {
        val options = SDKOptions()
        options.appKey = appKey
        initStatusBarNotificationConfig<T>(options)
        options.sdkStorageRootPath = getAppCacheDir(context)
        options.preloadAttach = true
        options.thumbnailSize = (222.0 / 375.0 * getDisplayWidth()).toInt()
        options.userInfoProvider = PushUserInfoProvider(context)
        options.sessionReadAck = true
        options.animatedImageThumbnailEnabled = true
        options.asyncInitSDK = true
        options.reducedIM = false
        options.checkManifestConfig = false
        options.enableTeamMsgAck = true
        options.enableFcs = false
        // 会话置顶是否漫游
        options.notifyStickTopSession = true
        options.mixPushConfig = buildMixPushConfig()
        // 打开消息撤回未读数-1的开关
        options.shouldConsiderRevokedMessageUnreadCount = true
        return options
    }

    inline fun <reified T : Activity>initStatusBarNotificationConfig(options: SDKOptions) {
        // load notification
        val config = loadStatusBarNotificationConfig()
        // load 用户的 StatusBarNotificationConfig 设置项
        // SDK statusBarNotificationConfig 生效
        config.notificationEntrance = T::class.java
        config.notificationFilter =
            StatusBarNotificationFilter { StatusBarNotificationFilter.FilterPolicy.PERMIT }
        options.statusBarNotificationConfig = config
    }

    // config StatusBarNotificationConfig
    fun loadStatusBarNotificationConfig(): StatusBarNotificationConfig {
        val config = StatusBarNotificationConfig()
        // load 用户的 StatusBarNotificationConfig 设置项
        // SDK statusBarNotificationConfig 生效
        config.notificationColor = Color.parseColor("#3a9efb")
        config.notificationSound = NOTIFY_SOUND_KEY
        config.notificationFoldStyle = NotificationFoldStyle.ALL
        config.downTimeEnableNotification = true
        config.ledARGB = Color.GREEN
        config.ledOnMs = LED_ON_MS
        config.ledOffMs = LED_OFF_MS
        config.showBadge = true
        return config
    }

    /** config app image/voice/file/log directory  */
    fun getAppCacheDir(context: Context): String? {
        var storageRootPath: String? = null
        try {
            if (context.externalCacheDir != null) {
                storageRootPath = context.externalCacheDir!!.canonicalPath
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (TextUtils.isEmpty(storageRootPath)) {
            storageRootPath =
                Environment.getExternalStorageDirectory().toString() + "/" + context.packageName
        }
        return storageRootPath
    }

    fun buildMixPushConfig(): MixPushConfig {
        // xiaomi
//        config.xmAppId = "xiao mi push app id"; //apply in xiaomi
//        config.xmAppKey = "xiao mi push app key";//apply in xiaomi
//        config.xmCertificateName = "Certificate Name";//config in yunxin platform

        // huawei
//        config.hwAppId = "huawei app id";//apply in huawei
//        config.hwCertificateName = "Certificate Name";//config in yunxin platform

        // meizu
//        config.mzAppId = "meizu push app id";//apply in meizu
//        config.mzAppKey = "meizu push app key";//apply in meizu
//        config.mzCertificateName = "Certificate Name";//config in yunxin platform

        // fcm
//        config.fcmCertificateName = "DEMO_FCM_PUSH";

        // vivo
//        config.vivoCertificateName = "Certificate Name";//config in yunxin platform

        // oppo
//        config.oppoAppId = "oppo push app id";//apply in oppo
//        config.oppoAppKey = "oppo push app key";//apply in oppo
//        config.oppoAppSercet = "oppo push app secret"; //apply in oppo
//        config.oppoCertificateName = "Certificate Name";//config in yunxin platform
        return MixPushConfig()
    }
}