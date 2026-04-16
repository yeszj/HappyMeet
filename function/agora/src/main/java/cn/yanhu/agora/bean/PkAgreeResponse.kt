package cn.yanhu.agora.bean

import cn.yanhu.commonres.bean.RoomSeatInfo

/**
 * @author: zhengjun
 * created: 2024/9/13
 * desc:
 */
data class PkAgreeResponse(
    var isInvite: Boolean,
    var pkId: Int,
    val roomId: String,
    val pkRoomId: String,
    val pkUid: String?,
    val agoraToken: String,
    var pkTime: Int,
    val seatList: MutableList<RoomSeatInfo>,
    val otherRoomPortrait: String,
    var countDownTime:Int = 0
)