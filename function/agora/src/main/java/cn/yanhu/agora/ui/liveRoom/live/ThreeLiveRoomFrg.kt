package cn.yanhu.agora.ui.liveRoom.live

import cn.yanhu.agora.adapter.liveRoom.ThreeRoomSeatAdapter
import com.blankj.utilcode.util.ThreadUtils

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
class ThreeLiveRoomFrg : BaseLiveRoomFrg() {


    private val seatUserAdapter by lazy { ThreeRoomSeatAdapter() }
    override fun initData() {
        super.initData()
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
            //mBinding.giftAnimView.put()
        },1000)
    }
}