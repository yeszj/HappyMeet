package cn.yanhu.agora.bean

import cn.yanhu.commonres.bean.UserDetailInfo

/**
 * @author: zhengjun
 * created: 2024/10/31
 * desc:
 */
data class RoomOnlineResponse (val onlineNum:Int,val onlineUsers:MutableList<UserDetailInfo>)