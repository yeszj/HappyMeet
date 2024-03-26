package cn.huanyuan.happymeet.bean


/**
 * @author: zhengjun
 * created: 2024/3/13
 * desc:
 */
data class AppCheckItemInfo(
    var id:Int,
    var iconUrl:String,
    var checkName: String,
    var passDesc:String,
    var noPassDesc:String,
    var btnValue: String,
    var hasCheck: Boolean
){
    companion object{
        const val TYPE_VERSION = 1
        const val TYPE_CAMERA  =2
        const val TYPE_MICROPHONE = 3
        const val TYPE_PHONE = 4
        const val TYPE_REAL_NAME = 5
        const val TYPE_WX_BIND = 6
    }
}