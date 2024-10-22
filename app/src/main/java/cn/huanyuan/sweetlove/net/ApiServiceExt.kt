package cn.huanyuan.sweetlove.net

import cn.zj.netrequest.RetrofitUtil


/**
 * @author: witness
 * created: 2021/9/17
 * desc:
 */
val rxApi: ApiService
    get() = RetrofitUtil.getInstance().retrofit?.create(ApiService::class.java)!!