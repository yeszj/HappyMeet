package cn.yanhu.agora.bean

import cn.yanhu.commonres.bean.UserDetailInfo

/**
 * @author: zhengjun
 * created: 2025/3/11
 * desc:
 */
data class RoomGroupMemberRes(val count:Int,val nickName:String,val list: MutableList<UserDetailInfo>)