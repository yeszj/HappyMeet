package cn.zj.netrequest

import android.text.TextUtils
import cn.zj.netrequest.encrypt.Md5utils.getMD5Str
import okhttp3.Request
import java.lang.StringBuilder
import java.util.*

/**
 * @author zhengjun
 * @desc
 * @create at 2021/6/27 08:31
 */
object SignUtils {
    private const val APPSECRET = "jkxvNK5X7xN918cYuVMX0AR6vAnecI3WKS4NAiEAye5JF1P8"

    //获取接口请求参数签名  对所有请求参数进行数典排序 拼接 进行MD5加密
    fun getMd5Signa(originRequest: Request, mBodyParamsMap: Map<String, String?>?): String {
        val url = originRequest.url
        val strings = url.queryParameterNames
        val signMap: MutableMap<String, String?> = HashMap()
        if (strings.isNotEmpty()) {
            for (key in strings) {
                val value = url.queryParameter(key)
                if (!TextUtils.isEmpty(value)) signMap[key] = value
            }
        }
        signMap.putAll(mBodyParamsMap!!)
        val map: Map<String, String?> = TreeMap(signMap)
        val sb = StringBuilder()
        for ((key, value) in map) {
            if (TextUtils.isEmpty(value)) {
                continue
            }
            if (sb.isNotEmpty()) {
                sb.append("&")
            }
            sb.append(key).append("=").append(value)
        }
        sb.append("&appsecret=$APPSECRET")
        return getMD5Str(sb.toString()).uppercase(Locale.getDefault())
    }
}