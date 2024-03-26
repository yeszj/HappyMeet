package cn.yanhu.commonres.bean.response

import cn.yanhu.commonres.bean.SameCityUserInfo

/**
 * @author: zhengjun
 * created: 2024/2/21
 * desc:
 */
data class SameCityUserResponse (val tcListRes:MutableList<SameCityUserInfo>,val ifBaseInfoFinished:Boolean,val isNewUser:Boolean)