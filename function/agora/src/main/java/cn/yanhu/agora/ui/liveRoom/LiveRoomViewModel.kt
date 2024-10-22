package cn.yanhu.agora.ui.liveRoom

import androidx.lifecycle.MutableLiveData
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.RoomConfigInfo
import cn.yanhu.agora.bean.RoomLeaveResponse
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.agora.bean.request.CreateRoomRequest
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.bean.response.RoomListResponse
import cn.yanhu.commonres.manager.AppCacheManager
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/3/15
 * desc:
 */
class LiveRoomViewModel : BaseViewModel() {
    val roomConfigInfoLivedata = MutableLiveData<ResultState<RoomConfigInfo>>()
    val createRoomLivedata = MutableLiveData<ResultState<RoomDetailInfo>>()
    val roomDetailLivedata = MutableLiveData<ResultState<RoomDetailInfo>>()
    val roomListObservable = MutableLiveData<ResultState<RoomListResponse>>()
    fun userSetSeat(
        roomId: String,
        operate: String,
        seatNum: String,
        onRequestResultListener: OnRequestResultListener<String>
    ) {
        request(
            { agoraRxApi.userSetSeat(roomId, operate, seatNum, AppCacheManager.userId) },
            onRequestResultListener
        )
    }


    fun autoSeat(roomId: String, status: String,onRequestResultListener: OnRequestResultListener<String>) {
        request({ agoraRxApi.autoSeat(roomId, status) }, onRequestResultListener)
    }



    fun switchMike(
        roomId: Int,
        roomType: Int,
        seatNum: Int,
        operatedUserId: Int,
        onRequestResultListener: OnRequestResultListener<String>
    ) {
        request(
            { agoraRxApi.switchMike(roomId, roomType, seatNum, operatedUserId) },
            onRequestResultListener
        )
    }

    val getTokenObserver = MutableLiveData<ResultState<String>>()

    fun getAgoraToken(callId: String) {
        request({ agoraRxApi.getAgoraToken(callId) }, getTokenObserver)
    }

    fun getRoomConfigInfo() {
        request({ agoraRxApi.getRoomConfigInfo() }, roomConfigInfoLivedata, true)
    }

    fun getRoomDetail(roomId: String, roomType: Int) {
        request({ agoraRxApi.getRoomDetail(roomId, roomType) }, roomDetailLivedata, false)
    }

    fun roomLeave(roomId: String, uuid: String,onRequestResultListener: OnRequestResultListener<RoomLeaveResponse>) {
        request({ agoraRxApi.roomLeave(roomId, uuid) }, onRequestResultListener,  isShowToast = false)
    }

    val closeRoomObserver = MutableLiveData<ResultState<String>>()
    fun closeRoom(roomId: String,reason:String, uuid: String) {
        request({ agoraRxApi.closeRoom(roomId.toInt(),reason, uuid) }, closeRoomObserver, false, isShowToast = false)
    }

    fun getSeatList(
        roomId: String,
        obRequestResultListener: OnRequestResultListener<MutableList<RoomSeatInfo>>
    ) {
        request({ agoraRxApi.getSeatList(roomId) }, obRequestResultListener, false)
    }

    fun getRoomList(page: Int) {
        request({ agoraRxApi.getRoomList(1, page) }, roomListObservable, false)
    }

    fun createRoom(roomRequest: CreateRoomRequest) {
        request(
            { agoraRxApi.createRoom(roomRequest) }, createRoomLivedata,
            isShowDialog = true,
            loadingHasContent = true
        )
    }

    fun operateLeave(roomId: String, operateUserId: String,onRequestResultListener: OnRequestResultListener<String>) {
        request({ agoraRxApi.operateLeave(roomId, operateUserId) }, onRequestResultListener, false)
    }
}