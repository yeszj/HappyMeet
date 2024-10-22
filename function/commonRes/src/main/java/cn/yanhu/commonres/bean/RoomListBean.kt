package cn.yanhu.commonres.bean

import android.text.TextUtils
import com.smart.adapter.interf.SmartFragmentTypeExEntity
import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2024/2/18
 * desc:
 */
open class RoomListBean : SmartFragmentTypeExEntity(), Serializable {
    var id = 0
    var coverUrl: String? = null
    var roomName:String = ""
    var roomType = 0 //1-视频交友-大厅 2-专属交友 0-用户 3-7人交友 4-7人天使 5-拍卖房
    var isFullSeat = false
    var ownerInfo: UserDetailInfo? = null
    var seatInfo:BaseUserInfo?=null
    var roomPortraitList: List<String> = mutableListOf()
    var roomId: String? = ""
    val banners: MutableList<BannerBean> = mutableListOf()

    fun getBaseInfo():String {
        return if (seatInfo!=null){
            "${seatInfo!!.age}岁 "+if (TextUtils.isEmpty(seatInfo!!.province)) "" else seatInfo!!.province
        }else{
            "${ownerInfo!!.age}岁 "+if (TextUtils.isEmpty(ownerInfo!!.province)) "" else ownerInfo!!.province
        }
    }

    override fun getFragmentType(): Int {
        if (roomType==1 || roomType == 2){
            return TYPE_THREE_ROOM
        }else if (roomType==3 || roomType == 4){
            return TYPE_SEVEN_ROOM
        }
        return TYPE_OTHER_ROOM
    }


    companion object{
        const val TYPE_THREE_ROOM = 1
        const val TYPE_SEVEN_ROOM = 2
        const val TYPE_OTHER_ROOM = 10
    }
}