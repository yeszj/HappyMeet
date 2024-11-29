package cn.yanhu.agora.manager.dbCache

import android.text.TextUtils
import cn.yanhu.agora.bean.BeautyFileCacheInfo
import cn.yanhu.commonres.manager.AppCacheManager
import com.blankj.utilcode.util.GsonUtils

/**
 * @author: zhengjun
 * created: 2024/3/13
 * desc:
 */
object BeautyCacheManager {
    fun getBeautyCache(): BeautyFileCacheInfo? {
        val beautySdkInfo = AppCacheManager.beautySdkInfo
        if (!TextUtils.isEmpty(beautySdkInfo)){
            return GsonUtils.fromJson(beautySdkInfo,BeautyFileCacheInfo::class.java)
        }
        return null
    }

    fun saveBeautySdkInfo(beautyFileCacheInfo: BeautyFileCacheInfo){
        AppCacheManager.beautySdkInfo = GsonUtils.toJson(beautyFileCacheInfo)
    }

    fun hasLoadBeautySdk():Boolean{
        return getBeautyCache()!=null
    }

}