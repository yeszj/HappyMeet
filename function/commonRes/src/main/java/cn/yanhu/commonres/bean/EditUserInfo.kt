package cn.yanhu.commonres.bean

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import cn.zj.netrequest.BR

/**
 * @author: zhengjun
 * created: 2024/3/11
 * desc:
 */
class EditUserInfo : BaseObservable() {
    var portrait: String = ""
    var thumbnail: List<String> = mutableListOf()

    @Bindable
    var description: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR._all)
        }
    val basicInfo: List<UserInfoItem> = mutableListOf()
    val detailInfo: List<UserInfoItem> = mutableListOf()
    val friendInfo: List<UserInfoItem> = mutableListOf()
    var editPhotoInfo:EditPhotoInfo?=null
    fun getPortraitEditInfo():EditPhotoInfo{
        if (editPhotoInfo==null){
            editPhotoInfo = EditPhotoInfo(portrait)
        }
        return editPhotoInfo!!
    }
}