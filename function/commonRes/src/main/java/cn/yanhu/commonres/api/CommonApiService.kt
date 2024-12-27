package cn.yanhu.commonres.api

import cn.zj.netrequest.status.BaseBean
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * @author: zhengjun
 * created: 2024/12/20
 * desc:
 */
interface CommonApiService {
    @FormUrlEncoded
    @POST("app/v1/friend/becomeFriend")
    suspend fun addFriend(
        @Field("addUserId") addUserId: String
    ): BaseBean<String>

    @FormUrlEncoded
    @POST("app/v1/friend/becomeFriendRose")
    suspend fun becomeFriendRose(
        @Field("addUserId") addUserId: String
    ): BaseBean<String>

    @GET("app/v1/config/{configKey}")
    suspend fun getConfigInfo(@Path("configKey") configKey:String): BaseBean<String>

    @FormUrlEncoded
    @POST("app/v1/user/setAppState")
    suspend fun setAppState(@Field("stateId") stateId:Int , @Field("npStatus") npStatus:Int ): BaseBean<String>
}