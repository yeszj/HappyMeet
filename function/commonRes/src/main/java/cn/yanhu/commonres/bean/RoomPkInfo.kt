package cn.yanhu.commonres.bean

/**
 * @author: zhengjun
 * created: 2025/9/4
 * desc:
 */
data class RoomPkInfo(
    val pkId: String,
    val countDown: Int,
    var status: Int,
    val pkType: Int,
    val redCnt: Int,
    val blueCnt: Int,
    val redMemberAvatarList: MutableList<String>,
    val blueMemberAvatarList: MutableList<String>,
    val redMemberSeatList: MutableList<PkUserInfo>,
    val blueMemberSeatList: MutableList<PkUserInfo>,
    val redMemberList: MutableList<String>,
    val blueMemberList: MutableList<String>,
    val showPkStart: String?,//0（展示）、1（不展示）
    val result: Int //0（蓝队胜利）、1（红队胜利）、2（平局），在状态为1时返回
){
    companion object{
        const val STATUS_PLAYING = 0
        const val STATUS_RESULT = 1
        const val STATUS_END = 2


        const val RESULT_BLUE_SUCCESS = 0
        const val RESULT_RED_SUCCESS = 1
        const val RESULT_DRAW = 2

    }
}