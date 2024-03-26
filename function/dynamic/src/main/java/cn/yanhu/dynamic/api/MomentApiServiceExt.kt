package cn.yanhu.dynamic.api

import cn.zj.netrequest.RetrofitUtil


/**
 * @author: witness
 * created: 2021/9/17
 * desc:
 */
val momentRxApi: MomentApiService
    get() = RetrofitUtil.getInstance().retrofit?.create(MomentApiService::class.java)!!