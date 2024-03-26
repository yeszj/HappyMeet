// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.
package cn.yanhu.imchat.push

import com.huawei.hms.push.RemoteMessage
import com.netease.nimlib.sdk.mixpush.HWPushMessageService
import com.netease.yunxin.kit.alog.ALog

class HWPushMessageService : HWPushMessageService() {
    override fun onNewToken(token: String) {
        ALog.i(TAG, " onNewToken, token=$token")
    }

    /**
     * 透传消息， 需要用户自己弹出通知
     *
     * @param remoteMessage
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        ALog.i(TAG, " onMessageReceived")
    }

    override fun onMessageSent(s: String) {
        ALog.i(TAG, " onMessageSent")
    }

    override fun onDeletedMessages() {
        ALog.i(TAG, " onDeletedMessages")
    }

    override fun onSendError(var1: String, var2: Exception) {
        ALog.e(TAG, " onSendError, $var1", var2)
    }

    companion object {
        private const val TAG = "HWPushMessageService"
    }
}