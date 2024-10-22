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
    var province by UserPref(Constants.PROVINCE,"")
    var hasComplete by UserPref(Constants.HAS_COMPLETE, false)
    var phoneEndNum by UserPref(Constants.PHONE, "")
    var imToken by UserPref(Constants.USER_IM_TOKEN, "")
    var mToken by UserPref(Constants.USER_TOKEN, "")
    var userId by UserPref(Constants.USER_ID, "")
    var gender by UserPref(Constants.GENDER, 1)
    var userInfo by UserPref(Constants.USER_INFO, "")

    var isFirstOpenApp by ConfigPref(Constants.IS_FIRST_OPEN_APP, true)
    var isAdmin by ConfigPref(Constants.IS_ADMIN, false)
    var beautySdkInfo by ConfigPref(Constants.BEAUTYSDKINFO, "")
    var rechargeInfo by ConfigPref(Constants.RECHARGEINFO,"")
    var giftInfo by ConfigPref(Constants.GIFTINFO,"")
    var oaid by ConfigPref(Constants.OAID,"")

    @JvmStatic
    fun isWoman():Boolean{
        return gender == 2
    }

    @JvmStatic
    fun isMan():Boolean{
        return gender == 1
    }
}