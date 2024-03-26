package cn.yanhu.agora.ui.liveRoom

import androidx.lifecycle.MutableLiveData
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.RoomConfigInfo
import cn.yanhu.agora.bean.request.CreateRoomRequest
import cn.zj.netrequest.BaseViewModel
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.ResultState

/**
 * @author: zhengjun
 * created: 2024/3/15
 * desc:
 */
class LiveRoomViewModel : BaseViewModel() {
    val roomConfigInfoLivedata = MutableLiveData<ResultState<RoomConfigInfo>>()
    val createRoomLivedata = MutableLiveData<ResultState<String>>()
    fun getRoomConfigInfo() {
        request({ agoraRxApi.getRoomConfigInfo() }, roomConfigInfoLivedata, true)
    }

    fun createRoom(roomRequest: CreateRoomRequest) {
        request({ agoraRxApi.createRoom(roomRequest) }, createRoomLivedata,
            isShowDialog = true,
            loadingHasContent = true
        )
    }
}