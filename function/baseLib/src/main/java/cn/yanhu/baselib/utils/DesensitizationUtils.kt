package cn.yanhu.baselib.utils

import android.text.TextUtils
import com.blankj.utilcode.util.RegexUtils

/**
 * @author: zhengjun
 * created: 2024/3/11
 * desc:字符串脱敏处理工具类
 */
object DesensitizationUtils {
    fun noPassByMobile(phone: String): String { //中间四位脱敏
        return if (!TextUtils.isEmpty(phone)) {
            RegexUtils.getReplaceAll(phone,"(\\d{3})\\d{4}(\\d{4})", "$1****$2")
        } else {
            ""
        }
    }

    fun noPassByName(name: String): String { //3个字以内脱敏第一个字，4—6个字脱敏前两个字，大于6个字脱敏第3—6个字
        return if (!TextUtils.isEmpty(name)) {
            if (name.length == 2) {
                name.substring(0, 1) + "*"
            } else if (name.length >= 3) {
                var str = ""
                for (i in 0..name.length - 3) {
                    str += "*"
                }
                name.substring(0, 1) + str + name.substring(name.length - 1, name.length)
            } else {
                name
            }
        } else {
            ""
        }
    }

    fun noPassByIdCard(str: String): String {
        return if (!TextUtils.isEmpty(str)) {
            RegexUtils.getReplaceAll(str,"(\\d{4})\\d{${10}}(\\d{4})", "$1**********$2")
        } else {
            ""
        }
    }

    fun noPassByEmail(email: String): String {
        var newEmail = email
        if (email.indexOf('@') > 0) {
            val str = email.split('@')
            var value = ""
            if (str[0].length > 3) { //@前面多于3位
                for (i in 3..<str[0].length) {
                    value += '*';
                }
                newEmail = str[0].substring(0, 3) + value + "@" + str[1]
            } else { //@前面小于等于于3位
                for (i in 1..<str[0].length) {
                    value += '*';
                }
                newEmail = str[0].substring(0, 1) + value + "@" + str[1]
            }
        }
        return newEmail
    }

}