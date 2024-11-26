package cn.yanhu.commonres.bean.response

import cn.yanhu.commonres.bean.DressUpInfo

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:
 */
data class DressUpResponse (val roseBalance:String,var list: MutableList<DressUpInfo>?){
    fun getGoodsList():MutableList<DressUpInfo>{
        return list ?: mutableListOf()
    }
}