package cn.huanyuan.sweetlove.net

import cn.huanyuan.sweetlove.bean.AppCheckItemInfo
import cn.huanyuan.sweetlove.bean.AppStartResponse
import cn.huanyuan.sweetlove.bean.AppVersionInfo
import cn.huanyuan.sweetlove.bean.ComplaintInfo
import cn.huanyuan.sweetlove.bean.GuardRankResponse
import cn.huanyuan.sweetlove.bean.InviteInfo
import cn.huanyuan.sweetlove.bean.InviteRecordResponse
import cn.huanyuan.sweetlove.bean.SecurityInfo
import cn.huanyuan.sweetlove.bean.SwitchConfigInfo
import cn.huanyuan.sweetlove.bean.TabEntity
import cn.huanyuan.sweetlove.bean.TaskResponse
import cn.huanyuan.sweetlove.bean.UserLevelResponse
import cn.huanyuan.sweetlove.bean.WalletInfo
import cn.huanyuan.sweetlove.bean.WalletRecordResponse
import cn.yanhu.commonres.bean.AuthCenterInfo
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.EditUserInfo
import cn.yanhu.commonres.bean.LoginSuccessInfo
import cn.yanhu.commonres.bean.MineMenuBean
import cn.yanhu.commonres.bean.ReportConfigInfo
import cn.yanhu.commonres.bean.SystemMessageInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.bean.request.DressBuyRequest
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
import com.pcl.sdklib.bean.CheckBaiduFaceResult
import com.pcl.sdklib.bean.PostBaiduAuthBean
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

    @GET("app/v1/userCenter/userInfo")
    suspend fun getMyPageInfo(): BaseBean<UserDetailInfo>


    @GET("app/v1/user/getUserInfoByUserId")
    suspend fun getUserInfoByUserId(@Query("uId") userId: String): BaseBean<UserDetailInfo>

    @GET("app/v1/userCenter/myServices")
    suspend fun getMyService(): BaseBean<MutableList<MineMenuBean>>


    @GET("app/v1/userCenter/getPersonalPage")
    suspend fun getEditInfo(): BaseBean<EditUserInfo>


    @FormUrlEncoded
    @POST("/app/v1/userCenter/updatePersonalPageSingle")
    suspend fun updatePersonalPageSingle(@Field("type") type:Int,@Field("content") content:String): BaseBean<String>


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

    @GET("app/v1/account/wallet")
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


    /**
     * 登录Api =====================================================================================================================
     */
    /**
     * 极光一键登录获取手机号
     */
    @FormUrlEncoded
    @POST("app/v1/jg/getPhone")
    suspend fun getPhone(@Field("loginToken") loginToken: String?): BaseBean<String>

    @FormUrlEncoded
    @POST("app/v1/user/insertBasicInfo")
    suspend fun insertBasicInfo(
        @Field("portrait") portrait: String?,
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


    @POST("app/v1/operates/reportUser")
    suspend fun complaintUser(
        @Body complaintInfo: ComplaintInfo?,
    ): BaseBean<Boolean>

    @GET("app/v1/operates/report/getConfigs")
    suspend fun getReportConfigs(): BaseBean<MutableList<ReportConfigInfo>>

    @GET("app/v1/auth/getSecurityCenter")
    suspend fun getSecurityInfo(): BaseBean<SecurityInfo>

    @GET("app/v1/user/getAppCheckInfo")
    suspend fun getAppCheckInfo(
        @Query("hasCameraPermission") hasCameraPermission: Boolean,
        @Query("hasMicrophonePermission") hasMicrophonePermission: Boolean
    ): BaseBean<MutableList<AppCheckItemInfo>>


    @GET("app/v1/user/getSwitchSetInfo")
    suspend fun getSwitchSetInfo(
    ): BaseBean<MutableList<SwitchConfigInfo>>

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

    @GET("app/v1/message/getSystemNotification")
    suspend fun getAllSystemMsg(): BaseBean<MutableList<SystemMessageInfo>>

    /**
     * 读取系统消息，设为已读
     */
    @POST("app/v1/message/updateSystemNoticeReadStatus")
    suspend fun readSystemMsg(): BaseBean<String>

    @GET("app/v1/operates/getBlockList")
    suspend fun getBlackUserList(): BaseBean<MutableList<BaseUserInfo>>

    /**
     * 实名认证
     */
    @FormUrlEncoded
    @POST("app/v1/auth/realNameAuth")
    suspend fun realNameProve(@Field("realName") realName:String,@Field("idCard") idCard:String): BaseBean<String>

    @GET("app/v1/auth/checkAuth")
    suspend fun checkBaiduFace():BaseBean<CheckBaiduFaceResult>

    /**
     * 查询是否可以百度人脸认证
     */
    @GET("app/v1/auth/ifCanBaiduFace")
    suspend fun ifCanBaiduFace(): BaseBean<Boolean>

    @POST("app/v1/auth/baiduFace")
    suspend fun submitBaiduFace(@Body baiduFaceRequest: PostBaiduAuthBean?): BaseBean<String>

    @GET("app/v1/version/check")
    suspend fun checkVersion(): BaseBean<AppVersionInfo?>

    /**
     * Api =====================================================================================================================
     */
    /**
     * 首页 app启动
     */
    @GET("app/v1/home/initialize")
    suspend fun appStart(): BaseBean<AppStartResponse>

    /**
     * 搜索页面 搜索用户列表
     */
    @GET("/app/v1/home/search")
    suspend  fun searchUserList(@Query("content") content: String?): BaseBean<MutableList<UserDetailInfo>>
}