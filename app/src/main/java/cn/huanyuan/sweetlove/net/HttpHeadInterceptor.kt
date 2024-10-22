package cn.huanyuan.sweetlove.net

import android.text.TextUtils
import cn.yanhu.baselib.utils.ext.logcom
import okhttp3.Interceptor
import okhttp3.Response

/**
 *url拦截器
 *@author woochen
 *@time 2019/4/26 10:29
 */
class HttpHeadInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        //构建新的请求
        val newRequest = request.newBuilder()
           // .header("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Accept-Encoding", "identity")
            .addHeader("Connection","close")
           // .addHeader("transfer-encoding","chunked")
        val header = HttpHeadConfig.getHeader()

        for ((key,value) in header){
            logcom("okhttp.OkHttpClient", "$key:$value")
            if (!TextUtils.isEmpty(value)){
                newRequest.addHeader(key,value!!)
            }
        }
        return chain.proceed(newRequest.build())
    }

}