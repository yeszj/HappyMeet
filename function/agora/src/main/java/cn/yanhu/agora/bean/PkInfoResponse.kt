package cn.yanhu.agora.bean


/**
 * @author: zhengjun
 * created: 2024/9/12
 * desc:
 */
data class PkInfoResponse(
    val roomId: String,
    val portrait: String,
    val invitePk : Int,//0=不接受邀请pk、1=接受
    val pkRoomPortraits: MutableList<String>,
    val recommendPkRooms: MutableList<PkUserInfo>,
    val recentlyPkRooms: MutableList<PkUserInfo>
)