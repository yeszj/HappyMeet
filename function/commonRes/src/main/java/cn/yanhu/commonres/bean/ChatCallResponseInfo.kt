package cn.yanhu.commonres.bean

import java.io.Serializable


/**
 * @author: zhengjun
 * created: 2024/10/14
 * desc:
 */
data class ChatCallResponseInfo(
    val id: Int,
    var uid: String,
    val notice: String,
    var agoraToken:String,
    val user: UserDetailInfo,
    var chatUser: UserDetailInfo,
    val price: Int,
    val spendUserId: Int,
    val rewardGold: Int,
    var chatType: Int,
    val videoSelfIntroduction: String
):Serializable