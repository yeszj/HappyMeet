package cn.yanhu.commonres.bean

import android.text.TextUtils
import java.io.Serializable

/**
 * @author: zhengjun
 * created: 2024/2/18
 * desc:
 */
class RoomListBean : Serializable {
    var id = 0
    var coverUrl: String? = null
    var roomType = 0 //1-视频相亲-大厅 2-专属相亲 0-用户 3-7人交友 4-7人天使 5-拍卖房
    var isFullSeat = false
    var ownerInfo: BaseUserInfo? = null
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
}