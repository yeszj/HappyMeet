package cn.huanyuan.sweetlove.bean


/**
 * @author: zhengjun
 * created: 2025/1/20
 * desc:
 */
class HeartCpEventRankInfo{
    var userIdA: String = ""
    var portraitA: String = ""
    var nickNameA: String = ""
    var userIdB: String = ""
    var portraitB: String = ""
    var nickNameB: String = ""

    var rankNum:String=""
    var intimacy: String = ""
    var desc: String = ""

    fun getNickAShot(): String{
        return nickNameA.take(5)
    }
    fun getNickBShot(): String{
        return nickNameB.take(5)
    }

}