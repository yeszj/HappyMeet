package com.pcl.sdklib.api

import cn.zj.netrequest.status.BaseBean
import com.pcl.sdklib.bean.PayRequest
import com.pcl.sdklib.bean.PostBaiduAuthBean
import com.pcl.sdklib.bean.WechatPayData
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


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

    @GET("app/v1/auth/alipayAuth")
    suspend fun alipayAuth(
    ): BaseBean<String>

    /**
     * 授权成功调用 支付宝
     */
    @FormUrlEncoded
    @POST("/app/v1/auth/alipayAuthCallBack")
    suspend fun aliAuthSuccess(@Field("result") result: String?): BaseBean<String>

    @POST("app/v1/pay/wxpay")
    suspend fun wxpay(
        @Body request: PayRequest
    ): BaseBean<WechatPayData>


    @GET("app/v1/auth/wxAuth")
    suspend fun wxAuth(
        @Query("wxCode") wxCode: String?
    ): BaseBean<Boolean>

    @POST("/app/v1/auth/baiduFace")
    fun submitBaiduFace(@Body baiduFaceRequest: PostBaiduAuthBean?): BaseBean<String>

    @FormUrlEncoded
    @POST("/app/v1/userCenter/updatePersonalPageSingle")
    suspend fun updatePersonalPageSingle(@Field("type") type:Int,@Field("content") content:String): BaseBean<String>
}