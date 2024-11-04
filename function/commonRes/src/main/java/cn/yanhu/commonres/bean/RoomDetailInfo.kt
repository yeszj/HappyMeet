package cn.yanhu.commonres.bean

import androidx.databinding.Bindable
import androidx.databinding.Observable
import cn.zj.netrequest.BR
import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
class RoomDetailInfo : RoomListBean(), Serializable, Observable {
    var uuid: String = ""
    var uid: String = ""
    var agoraToken: String = ""

    @Bindable
    var autoSeat: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.autoSeat)
        }

    var welcomeMsg: String = ""
    var manApplyInfo: ApplyUserInfo = ApplyUserInfo()
    var womanApplyInfo: ApplyUserInfo = ApplyUserInfo()
    var admin: Int = 0
    var roomSeatResList: MutableList<RoomSeatInfo> = mutableListOf()

    class ApplyUserInfo : Serializable {
        var applyNum: Int = 0
        var onlineNum: Int = 1
    }


}