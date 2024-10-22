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
    var configs:ConfigsBean?=null
    @Bindable
    var description: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR._all)
        }
    val basicInfo: List<UserInfoItem> = mutableListOf()
    val detailInfo: List<UserInfoItem> = mutableListOf()
    val friendInfo: List<UserInfoItem> = mutableListOf()
    var editPhotoInfo: EditPhotoInfo? = null
    fun getPortraitEditInfo(): EditPhotoInfo {
        if (editPhotoInfo == null) {
            editPhotoInfo = EditPhotoInfo(portrait)
        }
        return editPhotoInfo!!
    }

    data class ConfigsBean(
        val income: MutableList<String>,//收入
        val education:MutableList<String>,//学历
        val marriage: MutableList<String>,//
        val professIdentity:MutableList<String>,//职业
        val friendIncome: MutableList<String>,
        val friendHeight: MutableList<String>,
        val friendEducation: MutableList<String>
    )
}