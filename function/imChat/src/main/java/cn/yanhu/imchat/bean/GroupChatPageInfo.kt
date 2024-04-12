package cn.yanhu.imchat.bean

import cn.yanhu.commonres.bean.RoomListBean

/**
 * @author: zhengjun
 * created: 2024/3/28
 * desc:
 */
data class GroupChatPageInfo(
    var needRoseNum: String,
    var groupUserRoomList: MutableList<RoomListBean>
) : GroupBean()