package cn.yanhu.agora.manager.dbCache

import android.text.TextUtils
import cn.yanhu.agora.bean.BeautyFileCacheInfo
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.commonres.manager.AppCacheManager
import com.blankj.utilcode.util.GsonUtils

/**
 * @author: zhengjun
 * created: 2024/3/13
 * desc:
 */
object BeautyFaceEffectCacheManager {
    fun getBeautyCache(): BeautyFileCacheInfo? {
        val beautySdkInfo = AppCacheManager.faceEffectSdkInfo
        if (!TextUtils.isEmpty(beautySdkInfo)){
            return GsonUtils.fromJson(beautySdkInfo,BeautyFileCacheInfo::class.java)
        }
        return null
    }

    fun saveBeautySdkInfo(beautyFileCacheInfo: BeautyFileCacheInfo){
        AppCacheManager.faceEffectSdkInfo = GsonUtils.toJson(beautyFileCacheInfo)
        logcom("美颜贴纸资源下载成功")
    }

    fun hasLoadBeautySdk():Boolean{
        return getBeautyCache() !=null
    }

}