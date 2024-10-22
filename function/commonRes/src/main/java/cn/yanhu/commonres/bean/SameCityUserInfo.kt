package cn.yanhu.commonres.bean


/**
 * @author: zhengjun
 * created: 2024/2/21
 * desc:
 */
class SameCityUserInfo:UserDetailInfo() {
     val ifFollow = false
     val ifVideo = false
     val nobleLevel = 0
     val dynamics: List<String>? = null
}