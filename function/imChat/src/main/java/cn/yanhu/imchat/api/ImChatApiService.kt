package cn.yanhu.imchat.api

import cn.yanhu.commonres.bean.ChatCallResponseInfo
import cn.yanhu.commonres.bean.ChatPriceItemInfo
import cn.yanhu.commonres.bean.SendGiftRequest
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.bean.response.GiftResponse
import cn.yanhu.imchat.bean.ChatCheckInfo
import cn.yanhu.imchat.bean.GroupBean
import cn.yanhu.imchat.bean.GroupChatPageInfo
import cn.yanhu.imchat.bean.GroupDetailInfo
import cn.yanhu.imchat.bean.GroupMemberData
import cn.yanhu.imchat.bean.GroupOnlineData
import cn.yanhu.imchat.bean.SystemMsgUnReadInfo
import cn.yanhu.imchat.bean.request.GetChatGroupRequest
import cn.yanhu.imchat.bean.request.GetChatUsersRequest
import cn.yanhu.imchat.bean.request.RewardRequest
import cn.zj.netrequest.status.BaseBean
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity
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

    /**
     * 赠送礼物
     */
    @POST("app/v1/room/gift/send")
    suspend fun sendGift(@Body sendGiftRequest: SendGiftRequest): BaseBean<String>

    @GET("app/v1/chat/check")
    suspend fun sendCheck(
        @Query("chatUserId") chatUserId: String,
        @Query("chatType") chatType: String,
        @Query("type") type: String
    ): BaseBean<ChatCheckInfo>

    @GET("app/v1/chat/sendMessageCheckOfShuMei")
    suspend fun sendMessageCheckOfShuMei(
        @Query("chatId") chatId: String?,
        @Query("content") content: String?,
        @Query("messageType") messageType: String?,
        @Query("source") source: Int?
    ): BaseBean<String>

    @GET("app/v1/room/getChatEmoji")
    suspend fun getChatEmoji(): BaseBean<MutableList<EaseEmojiconGroupEntity>>

    @POST("app/v1/chat/chatReward")
    suspend fun chatReward(@Body rewardRequest: RewardRequest): BaseBean<String>

    @POST("/app/v1/user/getUsersInfo")
    suspend fun getUserList(
        @Body request: GetChatUsersRequest
    ): BaseBean<MutableList<UserDetailInfo>>

    @GET("app/v1/groupChat/recommendGroupList")
    suspend fun recommendGroupList(
        @Query("page") content: Int
    ): BaseBean<MutableList<GroupBean>>


    @POST("app/v1/chat/getUsersInfo")
    suspend fun getUsersInfo(
        @Body request: GetChatUsersRequest
    ): BaseBean<MutableList<UserDetailInfo>>

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
        @Query("uId") userId: String
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


    @GET("app/v1/chat/getPriceConfig")
    suspend fun getPriceConfig(
        @Query("chatUserId") chatUserId: Int
    ): BaseBean<MutableList<ChatPriceItemInfo>>

    @FormUrlEncoded
    @POST("app/v1/chat/setPrice")
    suspend fun setPrice(
        @Field("chatUserId") chatUserId: String,
        @Field("type") type: String,
        @Field("id") id: Int
    ): BaseBean<Boolean>

    @GET("app/v1/message/index")
    suspend fun getSystemMsg(): BaseBean<SystemMsgUnReadInfo>

    @GET("app/v1/room/gift/list")
    suspend fun getGiftList(): BaseBean<GiftResponse>

    @FormUrlEncoded
    @POST("app/v1/operates/blockUser")
    suspend fun blockUser(@Field("blockUserId") blockUserId:String): BaseBean<String>

    @FormUrlEncoded
    @POST("app/v1/operates/cancelBlock")
    suspend fun cancelBlock(@Field("operatedUserId") operatedUserId:String): BaseBean<String>


    @FormUrlEncoded
    @POST("app/v1/friend/becomeFriend")
    suspend fun addFriend(
        @Field("addUserId") addUserId: String
    ): BaseBean<String>

    @FormUrlEncoded
    @POST("/app/v1/chat/call")
    suspend fun call(@FieldMap map: Map<String, String>): BaseBean<ChatCallResponseInfo>

}