package cn.yanhu.agora.api

import cn.zj.netrequest.RetrofitUtil


/**
 * @author: witness
 * created: 2021/9/17
 * desc:
 */
val agoraRxApi: AgoraApiService
    get() = RetrofitUtil.getInstance().retrofit?.create(AgoraApiService::class.java)!!