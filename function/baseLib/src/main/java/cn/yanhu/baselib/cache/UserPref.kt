package cn.yanhu.baselib.cache

import android.content.Context
import android.content.SharedPreferences
import cn.yanhu.baselib.config.ConfigConstant
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 *用户sp工具类(建议保存用户相关的数据，退出登录后清除)
 *@author woochen
 *@time 2019/3/26 2:27 PM
 *备注：使用前需要初始化!!!
 */
class UserPref<T>(val name: String, val default: T) : ReadWriteProperty<Any?, T> {
    companion object {
        lateinit var sharePreference: SharedPreferences
        fun init(context: Context) {
            sharePreference =
                context.getSharedPreferences(context.packageName + ConfigConstant.USER_PREF_NAME, Context.MODE_PRIVATE)
        }
        fun clear() {
            sharePreference.edit().clear().apply()
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        //重写get方法
        return getPropertyValue(name, default)
    }

    private fun getPropertyValue(name: String, value: T): T = with(sharePreference) {
        val res: Any = when (value) {
            is Boolean -> getBoolean(name, value)
            is String -> getString(name, value)?:""
            is Int -> getInt(name, value)
            is Long -> getLong(name, value)
            is Float -> getFloat(name, value)
            else -> throw IllegalArgumentException("This type can not be saved into Preferences")
        }
        res as T
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putPropertyValue(name, value)
    }

    private fun putPropertyValue(name: String, value: T) = with(sharePreference.edit()) {
        when (value) {
            is Boolean -> putBoolean(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Long -> putLong(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("This type can not be saved into Preferences")
        }.apply()
    }
}