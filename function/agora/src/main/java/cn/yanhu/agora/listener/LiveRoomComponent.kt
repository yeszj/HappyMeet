package cn.yanhu.agora.listener

import cn.yanhu.commonres.bean.RoomDetailInfo

/**
 * @author: zhengjun
 * created: 2025/10/27
 * desc:
 */
interface LiveRoomComponent {
    fun initialize(roomInfo: RoomDetailInfo)
    fun onResume()
    fun onPause()
    fun onDestroy()
    fun release()
}