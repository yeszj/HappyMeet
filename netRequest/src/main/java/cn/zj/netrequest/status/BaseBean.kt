package cn.zj.netrequest.status


/**
 *实体基类
 *@author woochen
 *@time 2019/4/25 18:12
 * 注意：需要这里的同时需要同步修改[com.contract.commonlib.http.convert.CustomizeGsonResponseBodyConverter]
 */
class BaseBean<T>{
    val code: Int = 0
    val msg: String = ""
    val data: T? = null
}