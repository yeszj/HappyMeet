package cn.yanhu.agora.ui.liveRoom.live

import cn.yanhu.agora.adapter.liveRoom.SevenRoomSeatAdapter
import com.blankj.utilcode.util.ThreadUtils
import com.chad.library.adapter4.layoutmanager.QuickGridLayoutManager

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
class SevenLiveRoomFrg : BaseLiveRoomFrg() {

    private val seatUserAdapter by lazy { SevenRoomSeatAdapter() }
    override fun initData() {
        super.initData()
        val layoutManager = mBinding.rvSeat.layoutManager as QuickGridLayoutManager
        layoutManager.spanCount = 3
        mBinding.rvSeat.adapter = seatUserAdapter
    }

    override fun preJoinRoom() {
        super.preJoinRoom()
    }

    override fun destroyRoom() {
        super.destroyRoom()
    }

    override fun getRoomInfoSuccess() {
        super.getRoomInfoSuccess()
        seatUserAdapter.submitList(roomDetailInfo?.roomSeatResList)
        seatUserAdapter.roomDetailInfo = roomDetailInfo
        ThreadUtils.getMainHandler().postDelayed({
            seatUserAdapter.getItem(0)?.ifLeave = true
        },1000)
    }
}