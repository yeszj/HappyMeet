package cn.yanhu.commonres.manager

import cn.yanhu.baselib.cache.ConfigPref
import cn.yanhu.baselib.cache.UserPref
import cn.yanhu.baselib.config.Constants

/**
 * @author: witness
 * created: 2022/5/5
 * desc:
 */
object AppCacheManager {
    var imToken by UserPref(Constants.USER_IM_TOKEN, "")
    var mToken by UserPref(Constants.USER_TOKEN, "")
    var userId by UserPref(Constants.USER_ID, "")
    var gender by UserPref(Constants.GENDER, 1)
    var isFirstOpenApp by ConfigPref(Constants.IS_FIRST_OPEN_APP, true)
    var isAdmin by ConfigPref(Constants.IS_ADMIN, false)

    var beautySdkInfo by ConfigPref(Constants.BEAUTYSDKINFO, "")
}