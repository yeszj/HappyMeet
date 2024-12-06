package cn.yanhu.commonres.manager

import cn.yanhu.commonres.bean.RoomListBean

/**
 * @author: zhengjun
 * created: 2024/3/6
 * desc:
 */
object RoomTypeManager {

    fun isPublicRoom(roomType:Int):Boolean{
        return  roomType == RoomListBean.TYPE_PUBLIC
    }

    fun isPrivateRoom(roomType:Int):Boolean{
        return roomType == RoomListBean.TYPE_PRIVATE || roomType == RoomListBean.TYPE_ROBOT_ROOM
    }

    fun isSevenRoom(roomType:Int):Boolean{
        return roomType == RoomListBean.TYPE_SEVEN_ANGLE || roomType == RoomListBean.TYPE_SEVEN_FRIEND
    }

}