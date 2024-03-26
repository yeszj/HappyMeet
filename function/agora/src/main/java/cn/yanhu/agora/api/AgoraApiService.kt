package cn.yanhu.agora.api

import cn.yanhu.agora.bean.ConfigSdkVersion
import cn.yanhu.agora.bean.RoomConfigInfo
import cn.yanhu.agora.bean.request.CreateRoomRequest
import cn.zj.netrequest.status.BaseBean
import retrofit2.http.*

/**
 * @author: witness
 * created: 2022/5/20
 * desc:
 */
interface AgoraApiService {

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
    ): BaseBean<String>
}