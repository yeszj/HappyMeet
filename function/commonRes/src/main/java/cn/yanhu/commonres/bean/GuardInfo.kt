package cn.yanhu.commonres.bean

import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2024/3/5
 * desc:
 */
data class GuardInfo(
    val guardUserId: String,
    val guardUserPortrait: String,
    val guardNeedRoseNum: String,
    val guardFrame:String
): Serializable