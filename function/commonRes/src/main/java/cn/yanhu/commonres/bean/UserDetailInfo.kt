package cn.yanhu.commonres.bean

import androidx.databinding.Bindable
import cn.yanhu.commonres.manager.RoomTypeManager
import cn.zj.netrequest.BR

/**
 * @author: zhengjun
 * created: 2024/2/6
 * desc:
 */
class UserDetailInfo : BaseUserInfo() {
    var isMatchmaker = false ////是否是月老/红娘
    var banners: MutableList<BannerBean> = mutableListOf()
    var beautifulIdImg: String? = ""
    val needUploadPortrait = false
    val isFirstRecharge = false //是否首次充值
    var isAuth = false
    var personInfo: MutableList<TagInfo> = mutableListOf()
    var friendCondition: MutableList<TagInfo> = mutableListOf()
    var guardInfo: GuardInfo? = null
    var roomId:Int = 0
    var roomType:Int = 0
    var isAdmin:Boolean = false
    var thumbnail: List<String> = mutableListOf()
    fun isBlinding():Boolean{
        return RoomTypeManager.isBlinding(roomType)
    }

    @Bindable
    var coverImg: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR._all)
        }



    fun isShowRedStyle(): Boolean {
        return gender == 2 || isMatchmaker
    }
}