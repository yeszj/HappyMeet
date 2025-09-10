package cn.yanhu.commonres.manager

import cn.yanhu.commonres.bean.RoomSwitchCache
import org.litepal.LitePal

/**
 * @author: zhengjun
 * created: 2023/7/31
 * desc:
 */
object RoomSwitchCacheManager {

    fun isOpenGiftVoice(roomId: String): Boolean{
        val roomSwitchInfo = getRoomSwitchInfo(roomId)
        return roomSwitchInfo.giftVoiceOpen
    }

    fun isOpenEnterAnim(roomId: String): Boolean{
        val roomSwitchInfo = getRoomSwitchInfo(roomId)
        return roomSwitchInfo.enterAnimOpen
    }

    fun getRoomSwitchInfo(roomId: String): RoomSwitchCache {
        val find = LitePal.where("roomId = ?", roomId).limit(1).find(
            RoomSwitchCache::class.java
        )
        if (find.isEmpty()) {
            val roomInfo = RoomSwitchCache()
            roomInfo.roomId = roomId
            return roomInfo
        }
        return find[0]
    }

    fun saveRoomSwitchInfo(roomSwitchCache: RoomSwitchCache) {
        val currentCacheInfo = getRoomSwitchInfo(roomSwitchCache.roomId)
        if (currentCacheInfo.id <=0) {
            roomSwitchCache.save()
        } else {
            if (roomSwitchCache.giftVoiceOpen) {
                roomSwitchCache.setToDefault("giftVoiceOpen")
            }
            if (roomSwitchCache.enterAnimOpen) {
                roomSwitchCache.setToDefault("enterAnimOpen")
            }
            roomSwitchCache.update(currentCacheInfo.id)
        }
    }

    fun clearRoomSwitchCache(){
        LitePal.deleteAll(RoomSwitchCache::class.java)
    }
}