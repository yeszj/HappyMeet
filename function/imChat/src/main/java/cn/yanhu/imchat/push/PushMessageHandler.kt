// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.
package cn.yanhu.imchat.push

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.netease.nimlib.sdk.NimIntent
import com.netease.nimlib.sdk.mixpush.MixPushMessageHandler
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage

class PushMessageHandler : MixPushMessageHandler {
    override fun onNotificationClicked(context: Context, payload: Map<String, String>): Boolean {
        val sessionId = payload[PAYLOAD_SESSION_ID]
        val type = payload[PAYLOAD_SESSION_TYPE]
        //
        return if (sessionId != null && type != null) {
            val typeValue = Integer.valueOf(type)
            val imMessages = ArrayList<IMMessage>()
            val imMessage =
                MessageBuilder.createEmptyMessage(
                    sessionId,
                    SessionTypeEnum.typeOfValue(typeValue),
                    0
                )
            imMessages.add(imMessage)
            val notifyIntent = Intent()
            notifyIntent.component = initLaunchComponent(context)
            notifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            notifyIntent.action = Intent.ACTION_VIEW
            notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // 必须
            notifyIntent.putExtra(NimIntent.EXTRA_NOTIFY_CONTENT, imMessages)
            context.startActivity(notifyIntent)
            true
        } else {
            false
        }
    }

    private fun initLaunchComponent(context: Context): ComponentName? {
        return context
            .packageManager
            .getLaunchIntentForPackage(context.packageName)!!
            .component
    }

    override fun cleanMixPushNotifications(pushType: Int): Boolean {
        return false
    }

    companion object {
        const val PAYLOAD_SESSION_ID = "sessionID"
        const val PAYLOAD_SESSION_TYPE = "sessionType"
    }
}