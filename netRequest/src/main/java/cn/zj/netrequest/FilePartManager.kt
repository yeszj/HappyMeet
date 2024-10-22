package cn.zj.netrequest

import android.text.TextUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * @author: witness
 * created: 2022/5/5
 * desc:
 */
object FilePartManager {
    @JvmStatic
    fun getParam(path: String, key: String, source: String = ""): RequestBody {
        val file = File(path)
        val requestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val addFormDataPart = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(key, file.name, requestBody)
        if (!TextUtils.isEmpty(source)) {
            addFormDataPart.addFormDataPart("source", source)
        }

        return addFormDataPart.build()
    }
}