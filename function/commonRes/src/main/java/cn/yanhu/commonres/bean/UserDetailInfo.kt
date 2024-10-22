package cn.yanhu.commonres.bean

import androidx.databinding.Bindable
import cn.yanhu.commonres.manager.RoomTypeManager
import cn.zj.netrequest.BR
import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2024/2/6
 * desc:
 */
open class UserDetailInfo : BaseUserInfo(),Serializable {
    var isMatchmaker = false ////是否是月老/红娘
    var banners: MutableList<BannerBean> = mutableListOf()
    var needUploadPortrait = false
    var isAuth = false
    var personInfo: MutableList<TagInfo> = mutableListOf()
    var friendCondition: MutableList<TagInfo> = mutableListOf()
    var guardInfo: GuardInfo? = null
    var roomId:Int = 0
    var roomType:Int = 0
    var isAdmin:Boolean = false
    var thumbnail: List<String> = mutableListOf()
    var basicTagInfo:MutableList<String> = mutableListOf()
    var isFriend:Boolean = false
    var needRoseNum:Int = 0
    var colseVideo:Boolean = false
    var colseMic:Boolean = false
    var sameSex:Boolean = false
    var seatNum:Int = 0
    var roseNum:String = "0"

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