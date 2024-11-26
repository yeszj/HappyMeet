package cn.yanhu.agora.api

import cn.yanhu.agora.bean.AngleRankInfo
import cn.yanhu.agora.bean.AngleRoomResultInfo
import cn.yanhu.agora.bean.CheckCallBalanceRes
import cn.yanhu.agora.bean.ConfigSdkVersion
import cn.yanhu.agora.bean.EnterCheckResponse
import cn.yanhu.agora.bean.RoomConfigInfo
import cn.yanhu.agora.bean.RoomLeaveResponse
import cn.yanhu.agora.bean.RoomOnlineResponse
import cn.yanhu.agora.bean.UserReceiveRoseInfo
import cn.yanhu.agora.bean.request.CreateRoomRequest
import cn.yanhu.commonres.bean.ChatCallResponseInfo
import cn.yanhu.commonres.bean.ExpressionInfo
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.bean.SendGiftRequest
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.bean.response.FriendsResponse
import cn.yanhu.commonres.bean.response.RoomListResponse
import cn.zj.netrequest.status.BaseBean
import retrofit2.http.*

/**
 * @author: witness
 * created: 2022/5/20
 * desc:
 */
interface AgoraApiService {

    @GET("app/v1/room/list")
    suspend fun getRoomList(
        @Query("type") type: Int, @Query("page") page: Int
    ): BaseBean<RoomListResponse>

    @GET("app/v1/agora/getAgoraToken")
    suspend fun getAgoraToken(
        @Query("roomId") roomId: String,
    ): BaseBean<String>

    @GET("app/v1/agora/getBeautySdkInfo")
    suspend fun assertConfigInf(
        @Query("version") version: Int,
    ): BaseBean<ConfigSdkVersion>

    @GET("app/v1/room/getRoomConfig")
    suspend fun getRoomConfigInfo(
    ): BaseBean<RoomConfigInfo>

    @POST("app/v1/room/create")
    suspend fun createRoom(
        @Body roomRequest: CreateRoomRequest
    ): BaseBean<RoomDetailInfo>

    @GET("app/v1/room/getRoomDetail")
    suspend fun getRoomDetail(
        @Query("roomId") roomId: String, @Query("roomType") roomType: Int
    ): BaseBean<RoomDetailInfo>

    @GET("app/v1/room/getSeatList")
    suspend fun getSeatList(
        @Query("roomId") roomId: String
    ): BaseBean<MutableList<RoomSeatInfo>>

    @GET("app/v1/room/getExpression")
    suspend fun getExpression(
    ): BaseBean<List<ExpressionInfo>>


    @GET("app/v1/config/agoraConfigInf")
    suspend fun agoraConfigInf(
        @Query("type") type: Int, @Query("version") version: Int
    ): BaseBean<ConfigSdkVersion>

    @GET("app/v1/chat/call/checkCallBalance")
    suspend fun checkCallBalance(@Query("callId") callId: String?): BaseBean<CheckCallBalanceRes>

    @FormUrlEncoded
    @POST("app/v1/chat/call/deduction")
    suspend fun deduction(@Field("callId") callId: String?): BaseBean<String>


    /**
     * 一对一通话
     * chatType：0:语音通话 1:视频通话
     * status：0:发起申请 1:拒绝接通 2:接通 3:结束 (0的时候,用信息返回,其他时候没有)
     */
    @FormUrlEncoded
    @POST("app/v1/chat/call")
    suspend fun call(@FieldMap map: Map<String, String>): BaseBean<ChatCallResponseInfo>


    /**
     * 赠送礼物
     */
    @POST("app/v1/room/gift/send")
    suspend fun sendGift(@Body sendGiftRequest: SendGiftRequest): BaseBean<String>

    /**
     * 嘉宾离线超时2分钟，强制踢出房间
     */
    @POST("/app/v1/room/operateLeave")
    suspend fun operateLeave(
        @Query("roomId") roomId: String, @Query("operatedUserId") operatedUserId: String
    ): BaseBean<String>

    /**
     * 离开房间
     */
    @POST("/app/v1/room/leave")
    suspend fun roomLeave(
        @Query("roomId") roomId: String, @Query("uuid") uuid: String
    ): BaseBean<RoomLeaveResponse>

    /**
     * 用户上麦/下麦
     */
    @POST("/app/v1/room/userSetSeat")
    suspend fun userSetSeat(
        @Query("roomId") roomId: String,
        @Query("operate") roomType: String,
        @Query("seatNum") seatNum: String,
        @Query("operatedUserId") operatedUserId: String
    ): BaseBean<String>

    /**
     * 判断是否可以直接进入直播间
     */
    @GET("/app/v1/room/enterCheck")
    suspend fun enterCheck(@Query("roomId") roomId: String): BaseBean<EnterCheckResponse>

    @GET("/app/v1/room/getRoomCloseReason")
    suspend fun getRoomCloseReason(@Query("roomId") roomId: String): BaseBean<String?>

    @POST("/app/v1/room/autoSeat")
    suspend fun autoSeat(
        @Query("roomId") roomId: String, @Query("status") status: String
    ): BaseBean<String>

    /**
     * 打开/关闭麦克风
     */
    @POST("/app/v1/room/switchMike")
    suspend fun switchMike(
        @Query("roomId") roomId: Int,
        @Query("operate") roomType: Int,
        @Query("seatNum") seatNum: Int,
        @Query("operatedUserId") operatedUserId: Int
    ): BaseBean<String>

    @GET("app/v1/user/getUserInfoByUserId")
    suspend fun getUserInfoByUserId(@Query("uId") userId: String): BaseBean<UserDetailInfo>

    @FormUrlEncoded
    @POST("app/v1/friend/becomeFriend")
    suspend fun addFriend(
        @Field("addUserId") addUserId: String
    ): BaseBean<String>

    @GET("app/v1/room/getInviteList")
    suspend fun getInviteList(
        @Query("roomId") roomId: String?,
        @Query("gender") gender: String?,
        @Query("type") type: String?
    ): BaseBean<MutableList<UserDetailInfo>>

    @GET("app/v1/room/onlineUsers")
    suspend fun getOnlineUserList(
        @Query("roomId") roomId: String?, @Query("page") page: Int
    ): BaseBean<RoomOnlineResponse>

    @GET("app/v1/room/getRoomUserRoseList")
    suspend fun getRoomUserRoseList(
        @Query("roomId") roomId: String?,
        @Query("seatUserId") operatedUserId: String?,
    ): BaseBean<UserReceiveRoseInfo>

    @GET("app/v1/room/getRoomRoseList")
    suspend fun getRoomRoseList(
        @Query("roomId") roomId: String
    ): BaseBean<UserReceiveRoseInfo>

    @GET("app/v1/room/getAngelRankList")
    suspend fun getRoomAngleRank(
        @Query("roomId") roomId: String
    ): BaseBean<List<AngleRankInfo>>

    @GET("app/v1/room/getAngelWinner")
    suspend fun getAngelWinner(
        @Query("roomId") roomId: String
    ): BaseBean<AngleRoomResultInfo>

    /**
     * 管理员关闭房间
     */
    @POST("/app/v1/room/admin/closeRoom")
    suspend fun closeRoom(
        @Query("roomId") roomId: Int, @Query("reason") reason: String?, @Query("uuid") uuid: String?
    ): BaseBean<String>

    /**
     * 切换房间类型
     */
    @POST("/app/v1/room/switchType")
    suspend fun switchRoomType(
        @Query("roomId") roomId: String, @Query("roomType") roomType: String
    ): BaseBean<Boolean>

    @POST("/app/v1/room/switchTypeConfirm")
    suspend fun switchTypeConfirm(@Query("roomId") roomId: String): BaseBean<Boolean>

    @GET("app/v1/user/friendList")
    suspend fun getFriendList(@Query("page") page: Int): BaseBean<FriendsResponse>
}