package cn.yanhu.commonres.bean

import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2025/2/18
 * desc:
 */
data class LoverInfo(val userId: String, val nickName: String, val loversType: Int,val ifHide:Boolean):
     Serializable {
    fun loverDesc():String{
        return when (loversType) {
            1 -> {
                "黄金情侣"
            }
            2 -> {
                "水晶情侣"
            }
            3 -> {
                "钻石情侣"
            }
            else -> {
                "永久情侣"
            }
        }
    }
}