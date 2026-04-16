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
class RoomSeatInfo : BaseObservable(), Serializable, CloneableItem {
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
            notifyPropertyChanged(BR.ifLeave)
        }

    @Bindable
    var ifNetDisConnect: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.ifNetDisConnect)
        }

    @Bindable
    var pkStatus: Int = 0 //0不展示 1（胜利）、2（失败）、3（平局）
        set(value) {
            field = value
            notifyPropertyChanged(BR._all)
        }


    var seatRoseNum: Int = 0
    var seatUserRose: Boolean = false

    var roomUserSeatInfo: SeatUserInfo? = null


    @Bindable
    var isExpand: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.isExpand)
        }


    override fun deepCopy(): CloneableItem {
        val roomSeatInfo = RoomSeatInfo()
        roomSeatInfo.id = id
        roomSeatInfo.mikeUser = mikeUser
        roomSeatInfo.videoUser = videoUser
        roomSeatInfo.ifApply = ifApply
        roomSeatInfo.ifLeave = ifLeave
        roomSeatInfo.ifNetDisConnect = ifNetDisConnect
        roomSeatInfo.seatRoseNum = seatRoseNum
        roomSeatInfo.seatUserRose = seatUserRose
        roomSeatInfo.roomUserSeatInfo = roomUserSeatInfo
        roomSeatInfo.isExpand = isExpand

        return roomSeatInfo
    }
}

interface CloneableItem {
    fun deepCopy(): CloneableItem
}