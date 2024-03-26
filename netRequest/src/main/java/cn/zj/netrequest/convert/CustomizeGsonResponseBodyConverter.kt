package com.contract.commonlib.http.convert

import cn.zj.netrequest.status.CustomException
import cn.zj.netrequest.status.ErrorCode
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Converter
import java.io.IOException

/**
 * 自定义Gson转化类-响应
 * @author woochen
 * @time 2019/9/9 10:03
 */
class CustomizeGsonResponseBodyConverter<T>(private val gson: Gson, private val adapter: TypeAdapter<T>?) :
    Converter<ResponseBody, T> {


    @Throws(IOException::class)
    override fun convert(value: ResponseBody): T? {
        val response = value.string()
        //        logcom(response)
        try {
            val json = JSONObject(response)
            val code = json.optInt("code")
            val info = json.optString("msg")
            if (code != ErrorCode.SUCCESS) {
                throw CustomException(code, info)
            } else {
                return adapter?.fromJson(response)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } finally {
            value.close()
        }
        return null
    }


}
