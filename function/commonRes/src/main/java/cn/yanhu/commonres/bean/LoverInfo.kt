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
                "黄金CP"
            }
            2 -> {
                "水晶CP"
            }
            3 -> {
                "钻石CP"
            }
            else -> {
                "永久CP"
            }
        }
    }
}