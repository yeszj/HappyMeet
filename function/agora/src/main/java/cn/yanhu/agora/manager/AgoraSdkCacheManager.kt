package cn.yanhu.agora.manager

import cn.yanhu.agora.bean.AgoraSdkCacheInfo
import org.litepal.LitePal

/**
 * @author: zhengjun
 * created: 2023/7/31
 * desc:
 */
object AgoraSdkCacheManager {
    fun getAgoraSdk(): AgoraSdkCacheInfo? {
        val pokeInfo =
            LitePal.limit(1).find(
                AgoraSdkCacheInfo::class.java
            )
        if (pokeInfo.size > 0) {
            return pokeInfo[0]
        }
        return null
    }

    fun hasLoadAgoraSdk(): Boolean {
        return getAgoraSdk() != null
    }
}