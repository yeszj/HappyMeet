package cn.huanyuan.sweetlove.bean

import cn.yanhu.commonres.bean.UserDetailInfo

/**
 * @author: zhengjun
 * created: 2025/7/17
 * desc:
 */
data class RecommendRoomResponse (val friends: MutableList<UserDetailInfo>,val recommends: MutableList<UserDetailInfo>)