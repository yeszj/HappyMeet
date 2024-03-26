// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.
package cn.yanhu.imchat.push

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.text.TextUtils
import androidx.core.content.ContextCompat
import cn.yanhu.imchat.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.nos.NosService
import com.netease.nimlib.sdk.uinfo.UserInfoProvider
import com.netease.nimlib.sdk.uinfo.model.UserInfo
import com.netease.yunxin.kit.corekit.im.provider.TeamProvider.getTeamById
import com.netease.yunxin.kit.corekit.im.provider.UserInfoProvider.getUserInfoLocal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PushUserInfoProvider(private val context: Context) : UserInfoProvider {
    override fun getUserInfo(account: String): UserInfo {
        return getUserInfoLocal(account)!!
    }

    override fun getDisplayNameForMessageNotifier(
        account: String, sessionId: String, sessionType: SessionTypeEnum
    ): String? {
        return null
    }

    override fun getAvatarForMessageNotifier(
        sessionType: SessionTypeEnum,
        sessionId: String
    ): Bitmap {
        /*
     * get from cache
     */
        var bm: Bitmap? = null
        var defResId = R.mipmap.ic_notification_avatar_default
        val countDownLatch = CountDownLatch(1)
        val originUrl = arrayOfNulls<String>(1)
        if (SessionTypeEnum.P2P == sessionType) {
            val user = getUserInfo(sessionId)
            originUrl[0] = if (user != null) user.avatar else null
        } else if (SessionTypeEnum.Team == sessionType) {
            val team = getTeamById(sessionId)
            originUrl[0] = team?.icon
        }
        NIMClient.getService<NosService>(NosService::class.java)
            .getOriginUrlFromShortUrl(originUrl[0])
            .setCallback(
                object : RequestCallbackWrapper<String?>() {
                    override fun onResult(code: Int, result: String?, exception: Throwable) {
                        originUrl[0] = result
                        countDownLatch.countDown()
                    }
                })
        try {
            countDownLatch.await(200, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        if (!TextUtils.isEmpty(originUrl[0])) {
            bm = getNotificationBitmapFromCache(originUrl[0])
        }
        if (bm == null) {
            if (SessionTypeEnum.Team == sessionType || SessionTypeEnum.SUPER_TEAM == sessionType) {
                defResId = R.mipmap.ic_notification_avatar_group
            }
            val drawable = ContextCompat.getDrawable(
                context, defResId
            )
            if (drawable is BitmapDrawable) {
                bm = drawable.bitmap
            }
        }
        return bm!!
    }

    override fun getDisplayTitleForMessageNotifier(message: IMMessage): String? {
        return null
    }

    fun getNotificationBitmapFromCache(url: String?): Bitmap? {
        if (TextUtils.isEmpty(url)) {
            return null
        }
        val imageSize = context.resources.getDimension(com.zj.dimens.R.dimen.dp_48).toInt()
        var cachedBitmap: Bitmap? = null
        try {
            cachedBitmap = Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(RequestOptions().centerCrop().override(imageSize, imageSize))
                .submit()[200, TimeUnit.MILLISECONDS] // 最大等待200ms
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cachedBitmap
    }
}