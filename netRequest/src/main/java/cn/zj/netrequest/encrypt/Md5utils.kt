package cn.zj.netrequest.encrypt

import java.lang.Exception
import java.security.MessageDigest

/**
 * @author zhengjun
 * @desc md5加密
 * @create at 2018/6/22 15:09
 */
object Md5utils {
    fun getMD5Str(str: String): String {
        val messageDigest: MessageDigest?
        try {
            messageDigest = MessageDigest.getInstance("MD5")
            messageDigest.reset()
            messageDigest.update(str.toByteArray(charset("UTF-8")))
        } catch (e: Exception) {
            return "md5 加密异常"
        }
        val byteArray = messageDigest.digest()
        val md5StrBuff = StringBuffer()
        for (i in byteArray.indices) {
            if (Integer.toHexString(0xFF and byteArray[i].toInt()).length == 1) md5StrBuff.append("0")
                .append(
                    Integer.toHexString(0xFF and byteArray[i].toInt())
                ) else {
                md5StrBuff.append(Integer.toHexString(0xFF and byteArray[i].toInt()))
            }
        }
        return md5StrBuff.toString()
    }
}