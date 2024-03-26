package cn.yanhu.imchat.api

import cn.zj.netrequest.RetrofitUtil


/**
 * @author: witness
 * created: 2021/9/17
 * desc:
 */
val imChatRxApi: ImChatApiService
    get() = RetrofitUtil.getInstance().retrofit?.create(ImChatApiService::class.java)!!