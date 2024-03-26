package com.pcl.sdklib.api

import cn.zj.netrequest.RetrofitUtil


/**
 * @author: witness
 * created: 2021/9/17
 * desc:
 */
val sdkRxApi: SdkApiService
    get() = RetrofitUtil.getInstance().retrofit?.create(SdkApiService::class.java)!!