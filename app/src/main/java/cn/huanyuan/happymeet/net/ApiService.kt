package cn.huanyuan.happymeet.net

import cn.huanyuan.happymeet.bean.AppCheckItemInfo
import cn.huanyuan.happymeet.bean.ComplaintInfo
import cn.huanyuan.happymeet.bean.GuardRankResponse
import cn.huanyuan.happymeet.bean.InviteInfo
import cn.huanyuan.happymeet.bean.InviteRecordResponse
import cn.huanyuan.happymeet.bean.TabEntity
import cn.huanyuan.happymeet.bean.TaskResponse
import cn.huanyuan.happymeet.bean.UserLevelResponse
import cn.huanyuan.happymeet.bean.WalletInfo
import cn.huanyuan.happymeet.bean.WalletRecordResponse
import cn.yanhu.commonres.bean.AuthCenterInfo
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.request.DressBuyRequest
import cn.yanhu.commonres.bean.EditUserInfo
import cn.yanhu.commonres.bean.LoginSuccessInfo
import cn.yanhu.commonres.bean.MineMenuBean
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.bean.request.DressUpRequest
import cn.yanhu.commonres.bean.response.DressUpResponse
import cn.yanhu.commonres.bean.response.FriendsResponse
import cn.yanhu.commonres.bean.response.RoomListResponse
import cn.yanhu.commonres.bean.response.RoseExchangeResponse
import cn.yanhu.commonres.bean.response.RoseRechargeResponse
import cn.yanhu.commonres.bean.response.SameCityUserResponse
import cn.yanhu.commonres.bean.response.SeenMeHistoryResponse
import cn.yanhu.commonres.bean.response.WithdrawResponse
import cn.zj.netrequest.status.BaseBean
import okhttp3.RequestBody
import retrofit2.http.*


/**
 * @author: witness
 * created: 2021/9/17
 * desc:
 */
interface ApiService {
    @GET("app/v1/home/getNavigationBar")
    suspend fun getMainTabInfo(): BaseBean<MutableList<TabEntity>>

    @GET("app/v1/user/userInfo")
    suspend fun getMyPageInfo(): BaseBean<UserDetailInfo>


    @GET("app/v1/user/getUserInfoByUserId")
    suspend fun getUserInfoByUserId(@Query("userId") userId: String): BaseBean<UserDetailInfo>

    @GET("app/v1/user/myService")
    suspend fun getMyService(): BaseBean<MutableList<MineMenuBean>>


    @GET("app/v1/user/getEditUserInfo")
    suspend fun getEditInfo(): BaseBean<EditUserInfo>

    @GET("app/v1/user/getAuthCenterInfo")
    suspend fun getAuthCenterInfo(): BaseBean<AuthCenterInfo>

    @GET("app/v1/room/list")
    suspend fun getRoomList(
        @Query("type") type: Int,
        @Query("page") page: Int
    ): BaseBean<RoomListResponse>

    @GET("app/v1/rank/getGuardRankList")
    suspend fun getGuardRankList(
        @Query("userId") userId: String
    ): BaseBean<GuardRankResponse>

    @GET("app/v1/user/getUserLevelInfo")
    suspend fun getUserLevelInfo(
    ): BaseBean<UserLevelResponse>


    @GET("app/v1/samecity/list")
    suspend fun getSameCityUserList(
        @Query("ages") ages: String,
        @Query("province") province: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): BaseBean<SameCityUserResponse>

    @GET("app/v1/user/friendList")
    suspend fun getFriendList(@Query("page") page: Int): BaseBean<FriendsResponse>

    @GET("app/v1/user/getWalletInfo")
    suspend fun getWalletInfo(): BaseBean<WalletInfo>


    @FormUrlEncoded
    @POST("app/v1/user/sendCode")
    suspend fun sendVerifyCode(
        @Field("phone") phone: String
    ): BaseBean<String>

    @FormUrlEncoded
    @POST("app/v1/user/login")
    suspend fun login(
        @Field("phone") phone: String,
        @Field("source") source: Int,
        @Field("code") code: String
    ): BaseBean<LoginSuccessInfo>


    @FormUrlEncoded
    @POST("app/v1/user/bindInviteCode")
    suspend fun bindInviteCode(
        @Field("inviteCode") inviteCode: String,
    ): BaseBean<Boolean>


    @FormUrlEncoded
    @POST("app/v1/user/insertBasicInfo")
    suspend fun insertBasicInfo(
        @Field("nickName") nickName: String?,
        @Field("gender") gender: Int,
        @Field("age") age: Int,
        @Field("province") province: String?,
        @Field("city") city: String?
    ): BaseBean<Boolean>

    @GET("app/v1/user/getHistoryViewPageInfo")
    suspend fun getHistoryViewPageInfo(
        @Query("page") page: Int
    ): BaseBean<SeenMeHistoryResponse>


    @GET("app/v1/account/getRechargeInfo")
    suspend fun getRechargeInfo(
    ): BaseBean<RoseRechargeResponse>

    @GET("app/v1/account/getWithdrawalInfo")
    suspend fun getWithdrawalInfo(
    ): BaseBean<WithdrawResponse>

    @FormUrlEncoded
    @POST("app/v1/account/withdrawal")
    suspend fun withdrawal(
        @Field("type") type: Int?,
        @Field("withdrawalId") withdrawalId: Int
    ): BaseBean<Boolean>

    @FormUrlEncoded
    @POST("app/v1/account/roseExchange")
    suspend fun roseExchange(@Field("id") id: Int): BaseBean<String>


    @GET("app/v1/account/getRoseExchangeInfo")
    suspend fun getRoseExchangeInfo(
    ): BaseBean<RoseExchangeResponse>


    @GET("app/v1/account/getWalletRecord")
    suspend fun getWalletRecord(
        @Query("filterId") filterId: String, @Query("type") type: Int, @Query("page") page: Int
    ): BaseBean<WalletRecordResponse>


    @POST("app/v1/user/complaintUser")
    suspend fun complaintUser(
        @Body complaintInfo: ComplaintInfo?,
    ): BaseBean<Boolean>

    @GET("app/v1/user/getAppCheckInfo")
    suspend fun getAppCheckInfo(
        @Query("hasCameraPermission") hasCameraPermission: Boolean,
        @Query("hasMicrophonePermission") hasMicrophonePermission: Boolean
    ): BaseBean<MutableList<AppCheckItemInfo>>


    @GET("app/v1/user/getInviteMyUser")
    suspend fun getInviteMyUser(
    ): BaseBean<BaseUserInfo>


    @GET("app/v1/user/getMyInviteUser")
    suspend fun getMyInviteUser(
        @Query("page") page: Int
    ): BaseBean<InviteRecordResponse>

    @GET("app/v1/user/getInviteInfo")
    suspend fun getInviteInfo(
    ): BaseBean<InviteInfo>

    @GET("app/v1/userTask/getTaskList")
    suspend fun getTaskList(
    ): BaseBean<TaskResponse>


    @GET("app/v1/commodity/getDressUpInfo")
    suspend fun getDressUpInfo(
        @Query("type") type: Int
    ): BaseBean<DressUpResponse>

    @GET("app/v1/commodity/getMyDressUpInfo")
    suspend fun getMyDressUpInfo(
        @Query("type") type: Int
    ): BaseBean<DressUpResponse>

    @POST("app/v1/commodity/userBuyCommodity")
    suspend fun userBuyCommodity(
        @Body request: DressBuyRequest,
    ): BaseBean<String>

    @POST("app/v1/commodity/dressUpCommodity")
    suspend fun dressUpCommodity(@Body request: DressUpRequest): BaseBean<Boolean>


    //文件上传
    @POST("file/uploadImg")
    suspend fun uploadFile(
        @Body file: RequestBody,
    ): BaseBean<String>

}