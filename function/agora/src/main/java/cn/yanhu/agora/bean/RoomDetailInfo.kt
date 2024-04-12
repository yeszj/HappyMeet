package cn.yanhu.agora.bean

import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.RoomListBean

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
class RoomDetailInfo : RoomListBean() {
    var agoraToken: String = ""
    var autoSeat: Boolean = false
    var roomBgUrl: String = ""
    var welcomeMsg: String = ""
    var manApplyInfo: ApplyUserInfo = ApplyUserInfo()
    var womanApplyInfo: ApplyUserInfo = ApplyUserInfo()
    var roomSeatResList:MutableList<RoomSeatInfo> = mutableListOf()

    class ApplyUserInfo {
        var applyNum: Int = 0
        var onlineNum: Int = 1
        var onlineUsers: MutableList<BaseUserInfo> = mutableListOf()
    }
}