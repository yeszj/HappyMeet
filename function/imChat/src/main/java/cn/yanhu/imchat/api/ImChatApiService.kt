package cn.yanhu.imchat.api

import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.imchat.bean.GetChatGroupRequest
import cn.yanhu.imchat.bean.GetChatUsersRequest
import cn.yanhu.imchat.bean.GroupBean
import cn.yanhu.imchat.bean.GroupChatPageInfo
import cn.yanhu.imchat.bean.GroupDetailInfo
import cn.yanhu.imchat.bean.GroupMemberData
import cn.yanhu.imchat.bean.GroupOnlineData
import cn.zj.netrequest.status.BaseBean
import com.netease.yunxin.kit.corekit.im.model.UserInfo
import retrofit2.http.Body
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
interface ImChatApiService {

    @GET("app/v1/groupChat/recommendGroupList")
    suspend fun recommendGroupList(
        @Query("page") content: Int
    ): BaseBean<MutableList<GroupBean>>


    @POST("app/v1/chat/getUsersInfo")
    suspend fun getUsersInfo(
        @Body request: GetChatUsersRequest
    ): BaseBean<MutableList<UserInfo>>

    @GET("app/v1/chat/getIntimateUsers")
    suspend fun getIntimateUsers(
    ): BaseBean<MutableList<String>>

    @POST("app/v1/groupChat/getImGroupList")
    suspend fun getGroupsInfo(
        @Body request: GetChatGroupRequest
    ): BaseBean<MutableList<GroupBean>>

    @GET("app/v1/groupChat/groupDetail")
    suspend fun getGroupDetail(
        @Query("groupId") groupId: String
    ): BaseBean<GroupDetailInfo>

    @GET("app/v1/user/getUserInfoByUserId")
    suspend fun getUserInfoByUserId(
        @Query("userId") userId: String
    ): BaseBean<UserDetailInfo>

    @GET("app/v1/groupChat/getGroupMember")
    suspend fun getGroupMember(
        @Query("groupId") groupId: String
    ): BaseBean<GroupMemberData>

    @GET("app/v1/groupChat/getGroupOnlineUser")
    suspend fun getOnlineUser(
        @Query("groupId") groupId: String
    ): BaseBean<GroupOnlineData>

    @GET("app/v1/groupChat/getImGroupInfo")
    suspend fun getImGroupInfo(
        @Query("groupId") groupId: String
    ): BaseBean<GroupChatPageInfo>

    @FormUrlEncoded
    @POST("app/v1/groupChat/setAdmin")
    suspend fun setAdmin(
        @Field("groupId") groupId: String,
        @Field("type") type: Int,
        @Field("userId") userId: String
    ): BaseBean<Boolean>

    @FormUrlEncoded
    @POST("app/v1/groupChat/skipUser")
    suspend fun skipUser(
        @Field("groupId") groupId: String,
        @Field("userId") userId: String
    ): BaseBean<Boolean>


    @FormUrlEncoded
    @POST("app/v1/groupChat/createGroup")
    suspend fun createGroup(
        @FieldMap map: MutableMap<String, String>
    ): BaseBean<Boolean>


    @FormUrlEncoded
    @POST("app/v1/groupChat/setUserLimit")
    suspend fun setUserLimit(
        @Field("groupId") groupId: String,
        @Field("visitorEnterRule") visitorEnterRule: Int
    ): BaseBean<Boolean>

    @FormUrlEncoded
    @POST("app/v1/groupChat/exitGroup")
    suspend fun exitGroup(
        @Field("groupId") groupId: String,
        @Field("type") type: Int
    ): BaseBean<Boolean>
}