package cn.zj.netrequest.factory

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException


/**
 *
 * 类描述：实体类字段为null时赋初始值
 * 创建人：woochen
 * 创建时间：2020-03-13 18:17
 * 修改备注：
 **/
class NotNullAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType as Class<T>
        return if (rawType == String::class.java) {
            StringNullAdapter() as TypeAdapter<T>
        } else if (rawType == Int::class.java || rawType == Integer::class.java) {
            IntNullAdapter() as TypeAdapter<T>
        }else if (rawType == Boolean::class.java) {
            BooleanNullAdapter() as TypeAdapter<T>
        }else if (rawType == Long::class.java) {
            LongNullAdapter() as TypeAdapter<T>
        }else {
            null
        }
    }

    class StringNullAdapter : TypeAdapter<String>() {
        @Throws(IOException::class)
        override fun read(reader: JsonReader): String {
            if (reader.peek() === JsonToken.NULL) {
                reader.nextNull()
                return ""
            }
            return reader.nextString()
        }

        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: String?) {
            if (value == null) {
                writer.nullValue()
                return
            }
            writer.value(value)
        }
    }

    class IntNullAdapter : TypeAdapter<Int>() {
        @Throws(IOException::class)
        override fun read(reader: JsonReader): Int {
            if (reader.peek() === JsonToken.NULL) {
                reader.nextNull()
                return 0
            }
            return reader.nextInt()
        }

        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: Int?) {
            if (value == null) {
                writer.nullValue()
                return
            }
            writer.value(value)
        }
    }

    class BooleanNullAdapter : TypeAdapter<Boolean>() {
        @Throws(IOException::class)
        override fun read(reader: JsonReader): Boolean {
            if (reader.peek() === JsonToken.NULL) {
                reader.nextNull()
                return false
            }
            return reader.nextBoolean()
        }

        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: Boolean?) {
            if (value == null) {
                writer.nullValue()
                return
            }
            writer.value(value)
        }
    }

    class LongNullAdapter : TypeAdapter<Long>() {
        @Throws(IOException::class)
        override fun read(reader: JsonReader): Long {
            if (reader.peek() === JsonToken.NULL) {
                reader.nextNull()
                return 0
            }
            return reader.nextLong()
        }

        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: Long?) {
            if (value == null) {
                writer.nullValue()
                return
            }
            writer.value(value)
        }
    }

}