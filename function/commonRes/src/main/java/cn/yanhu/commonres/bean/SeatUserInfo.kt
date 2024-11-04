package cn.yanhu.commonres.bean

import androidx.databinding.Bindable
import cn.yanhu.commonres.BR

/**
 * @author: zhengjun
 * created: 2024/10/30
 * desc:
 */
class SeatUserInfo : UserDetailInfo() {
    @Bindable
    var userList: List<BaseUserInfo> = mutableListOf()
        set(value) {
            field = value
            notifyPropertyChanged(BR._all)
        }
}