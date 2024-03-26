// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.
package cn.yanhu.imchat.config

import android.content.Context
import android.content.pm.PackageManager

object ImKeyUtils {
    private var appKey: String? = null

    /**
     * read appKey from manifest
     */
    fun readAppKey(context: Context?): String? {
        if (appKey != null) {
            return appKey
        }
        if (context != null) {
            try {
                val appInfo = context
                    .packageManager
                    .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                appKey = appInfo.metaData.getString(Constant.CONFIG_APPKEY_KEY)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return appKey
    }
}