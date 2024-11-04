package cn.huanyuan.sweetlove.bean

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import cn.zj.netrequest.BR

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:投诉实体
 */
class ComplaintInfo : BaseObservable() {
    @Bindable
    var typeIds: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR._all)
        }

    @Bindable
    var userId: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR._all)
        }

    @Bindable
    var description: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.description)
        }

    @Bindable
    var images: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR._all)
        }
}