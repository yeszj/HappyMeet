package cn.yanhu.commonres.bean

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import cn.zj.netrequest.BR

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:
 */
class DressUpInfo : BaseObservable() {
    val id: Int = 0
    val name: String = ""
    val cover: String = ""
    var svgUrl: String? = ""
    val tag: String = ""
    val showPrice: String = ""
    var isHave: Boolean? = false
    val priceList: MutableList<StorePriceInfo> = mutableListOf()
    var sourceDesc: String? = ""
    var restTime: String? = ""

    @Bindable
    var isWear: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.isWear)
        }
    val isTimeOut: Boolean = false
}