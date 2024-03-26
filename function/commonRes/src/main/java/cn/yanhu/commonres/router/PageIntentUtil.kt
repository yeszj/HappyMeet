package cn.yanhu.commonres.router

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import cn.zj.netrequest.application.ApplicationProxy
import com.hjq.toast.ToastUtils
import org.json.JSONObject

/**
 * @author: witness
 * created: 2022/8/12
 * desc:
 */
object PageIntentUtil {

    private const val TYPE_CONTACT_UNION = "happyMeet/?page=1"

    @JvmStatic
    fun url2Page(mContext: Context, url: String?) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        if (url!!.startsWith("http") || url.startsWith("https")) {
            RouteIntent.lunchToWebView(url)
        }else if(url.startsWith("happyMeet/?page")){
            if (url == TYPE_CONTACT_UNION) {
                ApplicationProxy.instance.askCustomer()
            }
        }else {
            // "{clsPath:cn.gxgre.forlove.ui.activity.noble.NobleCenterActivity}"
            try {
                val jo = JSONObject(url)
                val className = jo.optString("clsPath")
                val it: Iterator<*> = jo.keys()
                val bundle = Bundle()
                while (it.hasNext()) {
                    val key = it.next().toString()
                    val value = jo.optString(key)
                    if ("true" == value || "false" == value) {
                        bundle.putBoolean(key, value.toBoolean())
                        continue
                    }
                    bundle.putString(key, value)
                }
                val intent = Intent()
                intent.setClassName(mContext.packageName, className)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (mContext is Activity) {
                    intent.putExtra(
                        "taskId",
                        mContext.taskId
                    )
                }
                intent.putExtras(bundle)
                mContext.startActivity(intent)
            } catch (e: Exception) {
                ToastUtils.show("请更新版本")
                e.printStackTrace()
            }
        }
    }
}