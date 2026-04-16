package cn.yanhu.commonres.bean

import androidx.databinding.Bindable
import androidx.databinding.Observable
import cn.yanhu.commonres.bean.response.WishResponse
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
    var closeReasons: MutableList<String> = mutableListOf()

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

    @Bindable
    var roomAdmin: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.roomAdmin)
        }
    var roomSeatResList: MutableList<RoomSeatInfo> = mutableListOf()
    var seatRoseNum: Int = 0
    var ifClubMember: Boolean = false
    var queuePrice: String = "0"
    var wishInfo: WishResponse? = null
    var pkDetail: RoomPkInfo? = null
    var needComboCnt:Int = 0
    var exclusiveRoomPrice:Int = 0
    var exclusiveSeatPrice:Int = 0
    var foreverFaceEffect: String?=""
    val pkRoomDetail: RoomPkEnterInfo?=null
    val intervalSecond:Int =0

    @Bindable
    var pkSuccessCount: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.pkSuccessCount)
        }
    fun isAdmin(): Boolean {
        return admin == 1
    }

    class ApplyUserInfo : Serializable {
        var applyNum: Int = 0
        var onlineNum: Int = 1
    }


}