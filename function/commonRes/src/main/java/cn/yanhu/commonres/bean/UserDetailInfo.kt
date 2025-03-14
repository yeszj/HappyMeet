package cn.yanhu.commonres.bean

import androidx.databinding.Bindable
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.RoomTypeManager
import cn.zj.netrequest.BR
import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2024/2/6
 * desc:
 */
open class UserDetailInfo : BaseUserInfo(), Serializable {
    var isMatchmaker = false ////是否是月老/红娘
    var banners: MutableList<BannerBean> = mutableListOf()
    var needUploadPortrait = false
    var needEditNickName = false
    var isAuth = false
    var personInfo: MutableList<TagInfo> = mutableListOf()
    var friendCondition: MutableList<TagInfo> = mutableListOf()
    var roomId: Int = 0
    var roomType: Int = 0
    var isAdmin: Boolean = false
    var thumbnail: List<String> = mutableListOf()
    var basicTagInfo: MutableList<String> = mutableListOf()
    var isFriend: Boolean = false
    var needRoseNum: Int = 0
    var colseVideo: Boolean = false
    var colseMic: Boolean = false
    var sameSex: Boolean = false
    var seatNum: Int = 0
    var addFriendWay:Int = 0
    var seatId:Int = 0
    var guardNickName:String = ""
    var loverInfo:LoverInfo?=null
    var guardInfo: BaseUserInfo? = null

    fun hideChatBtn():Boolean{
        return userId==AppCacheManager.userId || (sameSex && gender==1)
    }

    fun hideRoomBtn():Boolean{
        return roomId == 0 || userId==AppCacheManager.userId
    }

    fun isShowLoverInfo():Boolean{
        return loverInfo==null || loverInfo?.ifHide == false || (loverInfo?.userId == AppCacheManager.userId || AppCacheManager.userId == userId)
    }

    fun isShowGuardInfo():Boolean{
        return guardInfo==null || guardInfo?.isIfHide == false || (guardInfo?.userId == AppCacheManager.userId || AppCacheManager.userId == userId)
    }

    fun isShowMyGuardInfo():Boolean{
        return myGuardedInfo==null || myGuardedInfo?.isIfHide == false || (myGuardedInfo?.userId == AppCacheManager.userId || AppCacheManager.userId == userId)
    }
    @Bindable
    var roseNum: String = "0"
        set(value) {
            field = value
            notifyPropertyChanged(BR.roseNum)
        }

    fun getRoomDesc():String{
        return if (isPublicRoom()){
            "相亲中"
        }else if (isPrivateRoom()){
            "专属中"
        }else{
            "交友中"
        }
    }

    fun isPublicRoom(): Boolean {
        return RoomTypeManager.isPublicRoom(roomType)
    }

    fun isPrivateRoom(): Boolean {
        return RoomTypeManager.isPrivateRoom(roomType)
    }

    @Bindable
    var coverImg: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.coverImg)
        }


    fun isShowRedStyle(): Boolean {
        return gender == 2 || isMatchmaker
    }
}