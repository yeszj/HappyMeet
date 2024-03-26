package cn.yanhu.dynamic.api

import cn.yanhu.dynamic.bean.DiscussInfo
import cn.yanhu.dynamic.bean.DynamicInfo
import cn.yanhu.dynamic.bean.response.DynamicDetailInfo
import cn.zj.netrequest.status.BaseBean
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


/**
 * @author: witness
 * created: 2022/5/20
 * desc:
 */
interface MomentApiService {

    @FormUrlEncoded
    @POST("app/v1/dynamic/pubDynamic")
    suspend fun pubDynamic(
        @Field("city") city: String?,
        @Field("info") content: String?,
        @Field("images") images: String?,
    ): BaseBean<Boolean>


    @GET("app/v1/dynamic/list")
    suspend fun getDynamicList(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): BaseBean<MutableList<DynamicInfo>>


    @GET("app/v1/dynamic/getDynamicByUserId")
    suspend fun getDynamicByUserId(
        @Query("page") page: Int,
        @Query("userId") userId: String
    ): BaseBean<MutableList<DynamicInfo>>

    @FormUrlEncoded
    @POST("app/v1/dynamic/removeTrends")
    suspend fun removeTrends(@Field("dynamicId") trendsId: String): BaseBean<Any>


    @FormUrlEncoded
    @POST("app/v1/dynamic/userRemoveComment")
    suspend fun userRemoveComment(@Field("commentId") commentId: String): BaseBean<Boolean>


    @FormUrlEncoded
    @POST("app/v1/dynamic/adminOperateTrends")
    suspend fun adminOperateTrends(
        @Field("operateType") operateType: Int,
        @Field("operateId") operateId: String
    ): BaseBean<Any>

    @FormUrlEncoded
    @POST("app/v1/dynamic/likeTrends")
    suspend fun praiseMoment(
        @Field("dynamicId") trendsId: String,
        @Field("type") type: Int,
    ): BaseBean<Any>


    @FormUrlEncoded
    @POST("app/v1/dynamic/likeTrends")
    suspend fun praiseDiscuss(
        @Field("dynamicId") trendsId: String,
        @Field("commentId") commentId: String,
        @Field("type") type: Int,
    ): BaseBean<Any>

    @GET("app/v1/dynamic/dynamicDetail")
    suspend fun getDynamicDetail(
        @Query("dynamicId") trendsId: String,
        @Query("page") page: Int,
    ): BaseBean<DynamicDetailInfo>


    @GET("app/v1/dynamic/getCommentReplay")
    suspend fun getCommentReplay(
        @Query("dynamicId") trendsId: String,
        @Query("commentId") commentId: String,
        @Query("page") page: Int,
    ): BaseBean<MutableList<DiscussInfo>>

    @FormUrlEncoded
    @POST("app/v1/dynamic/releaseComment")
    suspend fun releaseComment(
        @FieldMap map: MutableMap<String, String>
    ): BaseBean<DiscussInfo>




//
//    @GET("app/v1/dynamic/getTrendsNotice")
//    suspend fun getTrendsNotice(
//        @Query("page") page: Int,
//    ): BaseBean<MomentNotifyResponse>

}