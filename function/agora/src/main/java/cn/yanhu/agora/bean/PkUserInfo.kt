package cn.yanhu.agora.bean

import cn.yanhu.commonres.bean.BaseUserInfo
import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2024/9/10
 * desc:
 */
class PkUserInfo : BaseUserInfo(),Serializable {
    var peopleNum:Int = 0
    var pkCount:Int =0
    var roomId:String = ""
    var inviteStatus:Int = 0
}