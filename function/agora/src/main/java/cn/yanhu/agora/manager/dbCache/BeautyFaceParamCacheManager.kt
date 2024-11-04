package cn.yanhu.agora.manager.dbCache

import cn.yanhu.agora.bean.BeautyBean
import org.litepal.LitePal

/**
 * @author: zhengjun
 * created: 2023/7/31
 * desc:
 */
object BeautyFaceParamCacheManager {
    fun getBeautyParamsByType(type: Int): MutableList<BeautyBean>? {
        return LitePal.where("type = ?", type.toString()).find(
            BeautyBean::class.java
        )
    }

    private fun getBeautyBean(name:String):BeautyBean?{
        val find = LitePal.where("name = ?", name).limit(1).find(
            BeautyBean::class.java
        )
        if(find.size>0){
            return find[0]
        }
        return null
    }

    fun saveParams(beautyBean: BeautyBean?) {
        if (beautyBean==null){
            return
        }
        val currentCacheInfo = getBeautyBean(beautyBean.name)
        if (currentCacheInfo == null) {
            beautyBean.save()
        } else {
            beautyBean.update(currentCacheInfo.id)
        }
    }
}