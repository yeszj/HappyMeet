package cn.zj.netrequest.intercept

import android.util.Log
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.*

/**
 * @author: witness
 * created: 2021/9/17
 * desc:
 */
class HttpCommonInterceptor : Interceptor {

    companion object {
        val mBodyParamsMap: MutableMap<String, String> = mutableMapOf()
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val newBuilder = request.newBuilder()
        val method = request.method
        if (method=="GET"){
            val getBuilder = request.url.newBuilder();
            for ((key, value) in mBodyParamsMap) {
                getBuilder.addQueryParameter(key, value)
            }
            val modifiedUrl = getBuilder.build()
            val build = newBuilder.url(modifiedUrl).build()
            if (modifiedUrl.toUrl().query!=null){
                Log.d(
                    "requestBody::", modifiedUrl.toUrl().query
                )
            }
            return chain.proceed(build)
        }else{
            if (request.body is FormBody){
                val formBody = request.body as FormBody
                val formBodyParamMap: MutableMap<String, String> = HashMap()
                val bodySize = formBody.size
                for (i in 0 until bodySize) {
                    formBodyParamMap[formBody.name(i)] = formBody.value(i)
                }
                val newFormBodyParamMap = mBodyParamsMap
                formBodyParamMap.putAll(newFormBodyParamMap)
                val bodyBuilder = FormBody.Builder()
                for ((key, value) in formBodyParamMap) {
                    bodyBuilder.add(key, value)
                }
                newBuilder.method(request.method, bodyBuilder.build())
                return chain.proceed(newBuilder.build())
            }
            return chain.proceed(chain.request())
        }
    }
}