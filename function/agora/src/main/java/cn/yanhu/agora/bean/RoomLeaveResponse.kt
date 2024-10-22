package cn.yanhu.agora.bean

import cn.yanhu.commonres.bean.BaseUserInfo

/**
 * @author: zhengjun
 * created: 2024/10/16
 * desc:
 */
data class RoomLeaveResponse (val ifOwner:Boolean,val ownerInfo : OwnerInfo){
    data class OwnerInfo(val duration:String,val peopleNum:Int,val roseNum:String):BaseUserInfo()
}