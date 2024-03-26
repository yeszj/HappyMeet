package cn.yanhu.commonres.bean

import cn.yanhu.baselib.utils.DesensitizationUtils
import com.blankj.utilcode.util.RegexUtils

/**
 * @author: zhengjun
 * created: 2024/3/11
 * desc:
 */
data class AuthCenterInfo(
    val phone: String,
    val realName: String,
    val wxNickName: String,
    val wxAvatar: String,
    val aliAccount: String
){

    fun getHidePhone():String{
        return DesensitizationUtils.noPassByMobile(phone)
    }

    fun getHideRealName():String{
       return DesensitizationUtils.noPassByName(realName)
    }

    fun getHideAliAccount():String{
        if (RegexUtils.isEmail(aliAccount)){
            return DesensitizationUtils.noPassByEmail(aliAccount)
        }
        return aliAccount
    }
}