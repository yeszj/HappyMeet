package cn.yanhu.commonres.bean

import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2024/2/29
 * desc:
 */
data class LoginSuccessInfo(
    val imToken: String,
    val token: String,
    val userId: String,
    val isRegister: Boolean,
    val maleNickNames:MutableList<String>?,
    val femaleNickNames:MutableList<String>?
):Serializable