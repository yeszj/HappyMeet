package cn.yanhu.agora.manager.dbCache

import cn.yanhu.agora.bean.AgoraSdkCacheInfo
import org.litepal.LitePal

/**
 * @author: zhengjun
 * created: 2023/7/31
 * desc:
 */
object AgoraSdkCacheManager {
    fun getAgoraSdk(): AgoraSdkCacheInfo? {
        try {
            val pokeInfo =
                LitePal.limit(1).find(
                    AgoraSdkCacheInfo::class.java
                )
            if (pokeInfo.size > 0) {
                return pokeInfo[0]
            }
        }catch (e:Exception){
            return null
        }
        return null
    }

    fun hasLoadAgoraSdk(): Boolean {
        return getAgoraSdk() != null
    }
}