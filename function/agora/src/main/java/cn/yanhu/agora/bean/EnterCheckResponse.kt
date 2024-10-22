package cn.yanhu.agora.bean

import cn.yanhu.commonres.bean.RoomDetailInfo

/**
 * @author: zhengjun
 * created: 2024/10/16
 * desc:
 */
data class EnterCheckResponse (val pass:Boolean,val roomInfo:RoomDetailInfo,val info:InfoBean){
    data class InfoBean(val nickName:String,val seatNum:Int,val price:String,val roomUserId:String,val roseEnough:Boolean)
}

