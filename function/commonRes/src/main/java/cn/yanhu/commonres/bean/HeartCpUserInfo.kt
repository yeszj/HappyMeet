package cn.yanhu.commonres.bean

/**
 * @author: zhengjun
 * created: 2026/3/25
 * desc:
 */
class HeartCpUserInfo : BaseUserInfo() {
    val cpUserId: String = ""
    val cpNickName: String = ""
    val cpPortrait: String = ""
    val intimacy: String = ""

    fun getNickAShot(): String{
        return nickName.take(5)
    }
    fun getNickBShot(): String{
        return cpNickName.take(5)
    }
}