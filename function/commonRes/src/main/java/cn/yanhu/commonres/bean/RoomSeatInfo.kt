package cn.yanhu.commonres.bean

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import cn.zj.netrequest.BR
import java.io.Serializable


/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
class RoomSeatInfo : BaseObservable(), Serializable {
    var id: Int = 1

    @Bindable
    var mikeUser: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.mikeUser)
        }

    @Bindable
    var videoUser: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.videoUser)
        }

    @Bindable
    var ifApply: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.ifApply)
        }

    @Bindable
    var ifLeave: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR._all)
        }
    var seatRoseNum: Int = 0
    var seatUserRose:Boolean = false
    var roomUserSeatInfo: UserDetailInfo? = null
}