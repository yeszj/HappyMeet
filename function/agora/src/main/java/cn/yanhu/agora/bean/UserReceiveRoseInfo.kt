package cn.yanhu.agora.bean

import cn.yanhu.commonres.bean.UserDetailInfo

/**
 * @author: zhengjun
 * created: 2024/10/17
 * desc:
 */
data class UserReceiveRoseInfo(val nickName:String,val roseNum:String,val list:MutableList<UserDetailInfo>)