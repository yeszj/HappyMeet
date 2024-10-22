package cn.yanhu.commonres.bean.response

import cn.yanhu.commonres.bean.RoomDetailInfo


/**
 * @author: zhengjun
 * created: 2024/2/19
 * desc:
 */
data class RoomListResponse(
    val roomList: MutableList<RoomDetailInfo>
)