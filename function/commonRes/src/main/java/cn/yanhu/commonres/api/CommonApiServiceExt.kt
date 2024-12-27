package cn.yanhu.commonres.api

import cn.zj.netrequest.RetrofitUtil


/**
 * @author: witness
 * created: 2021/9/17
 * desc:
 */
val commonRxApi: CommonApiService
    get() = RetrofitUtil.getInstance().retrofit?.create(CommonApiService::class.java)!!