package cn.yanhu.commonres.bean.response

import cn.yanhu.commonres.bean.WishInfo

/**
 * @author: zhengjun
 * created: 2025/3/27
 * desc:
 */
data class WishResponse(val status:Int,val list: MutableList<WishInfo>){
    fun getStatusDesc():String{
        return when(status){
            0->{
                "设置心愿"
            }

            1->{
                "未完成"
            }

            else->{
                "已完成"
            }
        }
    }
}