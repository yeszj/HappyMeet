package com.pcl.sdklib.sdk.union

import android.text.TextUtils
import cn.yanhu.commonres.manager.AppCacheManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.GsonUtils
import com.github.gzuliyujiang.oaid.DeviceID
import com.qiyukf.unicorn.api.ConsultSource
import com.qiyukf.unicorn.api.Unicorn
import com.qiyukf.unicorn.api.YSFUserInfo

/**
 * @author: zhengjun
 * created: 2023/2/16
 * desc:
 */
object UnionServiceUtils  {

    fun askCustomer() {
        try {
            if (Unicorn.isInit()) {
                var clientId: String? = ""
                val userInfoA = YSFUserInfo()
                try {
                    clientId = DeviceID.getClientId()
                }catch (e :Exception){
                    e.printStackTrace()
                }
                val userId: String = AppCacheManager.userId
                userInfoA.userId = userId
                userInfoA.data = GsonUtils.toJson(userInfoData(if (TextUtils.isEmpty(userId)) clientId else userId))
                Unicorn.setUserInfo(userInfoA)
                val source = ConsultSource(
                    UnicornConfig.QIYU_SOURCEURL,
                    UnicornConfig.QIYU_SOURCE_TITLE,
                    UnicornConfig.QIYU_CUSTOM
                )
                Unicorn.setLocalLanguage("zh", "CN")
                val topActivity = ActivityUtils.getTopActivity()
                if (topActivity != null) {
                    Unicorn.openServiceActivity(
                        topActivity,
                        UnicornConfig.QIYU_SOURCE_TITLE,
                        source
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun userInfoData(mobile: String?): List<UnionInfo>{
        val unionInfo = UnionInfo(value = mobile)
        val list: MutableList<UnionInfo> = ArrayList()
        list.add(unionInfo)
        return list
    }
}