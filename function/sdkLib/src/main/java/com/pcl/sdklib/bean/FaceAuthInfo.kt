package com.pcl.sdklib.bean

import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2025/2/20
 * desc:
 */
data class FaceAuthInfo(
    val sessionId: String,
    val bizToken: String,
    val requestId: String,
    val realName: String,
    val needConfirm: Boolean
):Serializable