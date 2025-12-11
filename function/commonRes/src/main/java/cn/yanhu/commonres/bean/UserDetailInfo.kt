package cn.yanhu.commonres.bean

import android.text.TextUtils
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
    var isMatchmaker = false ////是否是月老/主持
    var banners: MutableList<BannerBean> = mutableListOf()
    var needUploadPortrait = false
    var needEditNickName = false
    var isAuth = false
    var personInfo: MutableList<TagInfo> = mutableListOf()
    var roomId: Int = 0
    var roomType: Int = 0
    var isAdmin: Boolean = false
    var thumbnail: List<String> = mutableListOf()
    var basicTagInfo: MutableList<String> = mutableListOf()
    var isFriend: Boolean = false
    var needRoseNum: Int = 0
    var colseVideo: Boolean = false
    var colseMic: Boolean = false
    var seatNum: Int = 0
    var addFriendWay: Int = 0
    var seatId: Int = 0
    var guardNickName: String = ""
    var loverInfo: LoverInfo? = null
    var guardInfo: BaseUserInfo? = null
    var avatarFrameCover: String?=""

    fun getAvatarFramePic(): String?{
        if (TextUtils.isEmpty(avatarFrameCover)){
            return avatarFrame
        }
        return avatarFrameCover
    }

    @Bindable
    var ifMute: Boolean = false //true:禁言了 false:未禁言
        set(value) {
            field = value
            notifyPropertyChanged(BR.ifMute)
        }

    @Bindable
    var roomAdmin: Boolean = false //true:房间管理员 false:非管理员
        set(value) {
            field = value
            notifyPropertyChanged(BR.roomAdmin)
        }

    fun hideChatBtn(): Boolean {
        return userId == AppCacheManager.userId
    }

    fun hideRoomBtn(): Boolean {
        return roomId == 0 || userId == AppCacheManager.userId
    }

    fun isShowLoverInfo(): Boolean {
        return loverInfo == null || loverInfo?.ifHide == false || (loverInfo?.userId == AppCacheManager.userId || AppCacheManager.userId == userId)
    }

    fun isShowGuardInfo(): Boolean {
        return guardInfo == null || guardInfo?.isIfHide == false || (guardInfo?.userId == AppCacheManager.userId || AppCacheManager.userId == userId)
    }

    fun isShowMyGuardInfo(): Boolean {
        return myGuardedInfo == null || myGuardedInfo?.isIfHide == false || (myGuardedInfo?.userId == AppCacheManager.userId || AppCacheManager.userId == userId)
    }

    fun isSelf(): Boolean {
        return userId == AppCacheManager.userId
    }

    @Bindable
    var roseNum: String = "0"
        set(value) {
            field = value
            notifyPropertyChanged(BR.roseNum)
        }

    @Bindable
    var diffRoseNum: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR._all)
        }

    @Bindable
    var songStatus: Int = 0 //0:默认 1:即将演唱 2:插队 3:正在演唱
        set(value) {
            field = value
            notifyPropertyChanged(BR._all)
        }

    fun getRoomDesc(): String {
        return if (isPublicRoom()) {
            "交友中"
        } else if (isPrivateRoom()) {
            "专属中"
        } else {
            "交友中"
        }
    }

    fun getRecommendRoomDesc(): String {
        return if (isPublicRoom()) {
            "视频交友中"
        } else if (isPrivateRoom()) {
            "专属交友中"
        } else if (roomType == RoomListBean.TYPE_SEVEN_FRIEND) {
            "七人交友中"
        } else if (roomType == RoomListBean.TYPE_SEVEN_ANGLE) {
            "七人天使中"
        } else if (roomType == RoomListBean.TYPE_SEVEN_SONG) {
            "七人K歌中"
        } else if (roomType == RoomListBean.TYPE_NINE_FRIEND) {
            "九人交友中"
        } else if (roomType == RoomListBean.TYPE_NINE_ANGLE) {
            "九人天使中"
        } else if (roomType == RoomListBean.TYPE_NINE_SONG) {
            "九人K歌中"
        } else {
            "视频交友中"
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