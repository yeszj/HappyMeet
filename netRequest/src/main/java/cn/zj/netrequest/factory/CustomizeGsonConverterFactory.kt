package cn.zj.netrequest.factory

import cn.zj.netrequest.convert.CustomGsonRequestBodyConverter
import com.contract.commonlib.http.convert.CustomizeGsonResponseBodyConverter
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import com.google.gson.Gson
import okhttp3.RequestBody


/**
 * 自定义Gson转化类
 * @author woochen
 * @time 2019/9/9 10:04
 */
class CustomizeGsonConverterFactory private constructor(private var gson: Gson? = null) :
    Converter.Factory() {

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        val adapter = gson?.getAdapter(TypeToken.get(type))
        return CustomGsonRequestBodyConverter(gson!!, adapter)
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val adapter = gson?.getAdapter(TypeToken.get(type))
        return CustomizeGsonResponseBodyConverter(gson!!, adapter)
    }

    companion object {
        @JvmOverloads
        fun create(): CustomizeGsonConverterFactory {
            return CustomizeGsonConverterFactory(Gson())
        }
    }
}
