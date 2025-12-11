package cn.huanyuan.sweetlove.func.manager

import android.app.Activity
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.showToast
import com.blankj.utilcode.util.GsonUtils
import com.umeng.socialize.UMAuthListener
import com.umeng.socialize.UMShareAPI
import com.umeng.socialize.bean.SHARE_MEDIA

/**
 * @author: zhengjun
 * created: 2024/8/5
 * desc:
 */
object UMenLoginManager {
    fun startLogin(context: Activity,shareMedia: SHARE_MEDIA = SHARE_MEDIA.WEIXIN,onUMengLoginSuccessListener: OnUMengLoginSuccessListener){
        val umShareApi = UMShareAPI.get(context)
        umShareApi.getPlatformInfo(context, shareMedia,object : UMAuthListener{
            override fun onStart(p0: SHARE_MEDIA?) {
            }

            override fun onComplete(p0: SHARE_MEDIA?, p1: Int, p2: MutableMap<String, String>?) {
//                val nickName = p2?.get("name") //昵称
//                val portraitUrl = p2?.get("iconurl")//头像
                logcom(GsonUtils.toJson(p2))
                onUMengLoginSuccessListener.onAuthSuccess(p2)
            }
            override fun onError(p0: SHARE_MEDIA?, p1: Int, p2: Throwable?) {
                showToast(p2?.message)
            }
            override fun onCancel(p0: SHARE_MEDIA?, p1: Int) {
                showToast("已取消")
            }
        })
    }

    interface OnUMengLoginSuccessListener{
        fun onAuthSuccess(p2: MutableMap<String, String>?)
    }
}