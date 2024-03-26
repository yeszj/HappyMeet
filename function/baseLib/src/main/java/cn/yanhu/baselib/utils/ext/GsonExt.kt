package cn.yanhu.baselib.utils.ext

import com.google.gson.Gson

/**
 * @author: witness
 * created: 2021/9/24
 * desc:
 */
fun <T> gsonToJson(t: T): String {
    return Gson().toJson(t)
}

inline fun <reified T>jsonToEntity(string: String):T{
   return Gson().fromJson(string, T::class.java)
}