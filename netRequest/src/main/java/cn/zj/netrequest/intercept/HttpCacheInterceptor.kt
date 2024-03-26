package cn.zj.netrequest.intercept

import android.util.Log
import okhttp3.*
import okio.Buffer
import kotlin.Throws
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException

/**
 * @author zhengjun
 * @desc 拦截器  配置缓存
 * @created at 2018/4/10 17:52
 */
class HttpCacheInterceptor : Interceptor {
    //  配置缓存的拦截器
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val requestBody = request.body
        var body: String? = null
        if (requestBody != null) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            var charset = Charset.forName("utf8")
            val contentType = requestBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(Charset.forName("utf8"))
            }
            body = buffer.readString(charset!!)
            buffer.close()
        }
        val originalResponse: Response = chain.proceed(request)
        val responseBody = originalResponse.body
        val rBody: String
        val source = responseBody!!.source()
        source.request(Long.MAX_VALUE)
        val buffer = source.buffer
        var charset = Charset.forName("utf8")
        val contentType = responseBody.contentType()
        if (contentType != null) {
            try {
                charset = contentType.charset(Charset.forName("utf8"))
            } catch (e: UnsupportedCharsetException) {
                e.printStackTrace()
            }
        }
        rBody = buffer.clone().readString(charset!!)
        buffer.close()
        Log.d(
            "requestBody::", """
     收到响应: code:${originalResponse.code}
     请求url：${originalResponse.request.url}
     请求body：$body
     请求响应Response: $rBody
     """.trimIndent()
        )
        return originalResponse.newBuilder()
            .header("Cache-Control", "public, only-if-cached, max-stale=2419200")
            .removeHeader("Pragma")
            .build()
    }
}