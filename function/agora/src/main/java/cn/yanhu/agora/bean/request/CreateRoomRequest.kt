package cn.yanhu.agora.bean.request

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import cn.yanhu.agora.BR
import cn.yanhu.commonres.bean.RoomListBean

/**
 * @author: zhengjun
 * created: 2024/3/20
 * desc:
 */
class CreateRoomRequest : BaseObservable() {
    @Bindable
    var roomType: Int = RoomListBean.TYPE_PUBLIC
        set(value) {
            field = value
            notifyPropertyChanged(BR.roomType)
        }

    @Bindable
    var name: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.name)
        }

    @Bindable
    var welcomeMsg: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.welcomeMsg)
        }

}