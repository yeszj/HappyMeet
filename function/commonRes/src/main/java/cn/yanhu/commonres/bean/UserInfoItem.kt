package cn.yanhu.commonres.bean

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

/**
 * @author: zhengjun
 * created: 2024/3/11
 * desc:
 */
data class UserInfoItem(
    @Bindable
    var key: String,
    var value: String,
    val type: Int,
    val list: MutableList<String>
) : BaseObservable(){

}