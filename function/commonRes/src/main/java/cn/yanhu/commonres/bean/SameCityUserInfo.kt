package cn.yanhu.commonres.bean

import cn.yanhu.commonres.manager.RoomTypeManager

/**
 * @author: zhengjun
 * created: 2024/2/21
 * desc:
 */
class SameCityUserInfo:BaseUserInfo() {
     val roomId = 0
     val roomType = 0
     val ifFollow = false
     val ifVideo = false
     val isAuth = false
     val nobleLevel = 0
     val dynamics: List<String>? = null
     fun isBlinding():Boolean{
          return RoomTypeManager.isBlinding(roomType)
     }
}