package cn.yanhu.commonres.manager

/**
 * @author: zhengjun
 * created: 2024/3/6
 * desc:
 */
object RoomTypeManager {
    const val TYPE_PUBLIC_BLIND = 1 //大厅交友房
    const val TYPE_PRIVATE_BLIND = 2//专属交友房
    const val TYPE_SEVEN_ROOM = 3 //七人交友房
    const val TYPE_SEVEN_ANGEL = 4 //七人天使房
    const val TYPE_AUCTION_ROOM = 5 //拍卖房

    fun isBlinding(roomType:Int):Boolean{
        return roomType == TYPE_PUBLIC_BLIND || roomType == TYPE_PRIVATE_BLIND
    }

    fun isSevenRoom(roomType:Int):Boolean{
        return roomType == TYPE_SEVEN_ROOM || roomType == TYPE_SEVEN_ANGEL
    }

    fun isAuctionRoom(roomType:Int):Boolean{
        return roomType == TYPE_AUCTION_ROOM
    }
}