package cn.huanyuan.sweetlove.bean

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import cn.zj.netrequest.BR

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:投诉实体
 */
class ComplaintInfo : BaseObservable(){
    @Bindable
    var complaintType:String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.complaintType)
        }

    @Bindable
    var complaintUserId:String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.complaintUserId)
        }

    @Bindable
    var complaintReason:String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.complaintReason)
        }

    @Bindable
    var picList:MutableList<String> = mutableListOf()
        set(value) {
            field = value
            notifyPropertyChanged(BR.picList)
        }
}