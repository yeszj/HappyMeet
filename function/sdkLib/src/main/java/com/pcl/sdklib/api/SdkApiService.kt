package com.pcl.sdklib.api

import cn.zj.netrequest.status.BaseBean
import com.pcl.sdklib.bean.PayRequest
import com.pcl.sdklib.bean.WechatPayData
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


/**
 * @author: witness
 * created: 2022/5/20
 * desc:
 */
interface SdkApiService {
    //获取支付宝支付参数
    @POST("app/v1/pay/alipay")
    suspend fun alipay(
        @Body request: PayRequest
    ): BaseBean<String>

    @POST("app/v1/pay/wxpay")
    suspend fun wxpay(
        @Body request: PayRequest
    ): BaseBean<WechatPayData>


    @FormUrlEncoded
    @POST("app/v1/auth/wxAuth")
    suspend fun wxAuth(
        @Field("wxCode") wxCode: String?
    ): BaseBean<Boolean>
}