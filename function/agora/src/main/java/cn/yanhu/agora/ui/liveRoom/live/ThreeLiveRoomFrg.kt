package cn.yanhu.agora.ui.liveRoom.live

import android.view.View
import cn.yanhu.agora.adapter.liveRoom.ThreeRoomSeatAdapter
import cn.yanhu.agora.miniwindow.LiveRoomVideoMiniManager
import cn.yanhu.agora.miniwindow.MiniWindowManager
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.manager.AppCacheManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.yhao.floatwindow.PermissionListener

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
class ThreeLiveRoomFrg : BaseLiveRoomFrg() {
    private val seatUserAdapter by lazy {
        ThreeRoomSeatAdapter(object : ThreeRoomSeatAdapter.OnRoomItemClickListener {
            override fun onClickRose(roomSeatInfo: RoomSeatInfo) {
                roomSeatInfo.roomUserSeatInfo?.apply {
                    showSendGiftPop(this)
                }
            }
        })
    }
    override fun initData() {
        super.initData()
        mBinding.rvSeat.adapter = seatUserAdapter
        val childItemClickListener =
            object : BaseQuickAdapter.OnItemChildClickListener<RoomSeatInfo> {
                override fun onItemClick(
                    adapter: BaseQuickAdapter<RoomSeatInfo, *>,
                    view: View,
                    position: Int
                ) {
                    val item = seatUserAdapter.getItem(position) ?: return

                    when (view.id) {
                        cn.yanhu.agora.R.id.iv_voiceStatus -> {
                            //开关麦
                            val roomUserSeatInfo = item.roomUserSeatInfo ?: return
                            if (localUserId.toString() == roomUserSeatInfo.userId) {
                                switchMikeAlert(!item.mikeUser, item.id)
                            }
                        }

                        cn.yanhu.agora.R.id.vg_parent, cn.yanhu.agora.R.id.anchorSeatInfo -> {
                            val roomUserSeatInfo = item.roomUserSeatInfo
                            if (roomUserSeatInfo == null) {
                                if (isOwner) {
                                    //邀请上麦弹框
                                    seatUserAdapter.showUserList(if (item.id == 2) "1" else "2")
                                } else {
                                    //上麦
                                    if (AppCacheManager.isMan()){
                                        if (position==1){
                                            userSetSeat(if (roomSourceBean.autoSeat) SEAT_TYPE_AUTO else SEAT_TYPE_APPLY)
                                        }else{
                                            showToast("该座位仅对女用户开放")
                                        }
                                    }else{
                                        if (position==2){
                                            userSetSeat(if (roomSourceBean.autoSeat) SEAT_TYPE_AUTO else SEAT_TYPE_APPLY)
                                        }else{
                                            showToast("该座位仅对男用户开放")
                                        }
                                    }
                                }
                            } else {
                                showUserPop(item)
                            }
                        }
                    }
                }

            }
        seatUserAdapter.addOnItemChildClickListener(
            cn.yanhu.agora.R.id.anchorSeatInfo,
            childItemClickListener
        )
        seatUserAdapter.addOnItemChildClickListener(
            cn.yanhu.agora.R.id.vg_parent,
            childItemClickListener
        )
        seatUserAdapter.addOnItemChildClickListener(
            cn.yanhu.agora.R.id.iv_voiceStatus,
            childItemClickListener
        )
    }


    override fun getRoomInfoSuccess() {
        super.getRoomInfoSuccess()
        seatUserAdapter.roomDetailInfo = roomSourceBean
        if (seatUserAdapter.roomDetailInfo == null) {
            seatUserAdapter.notifyItemChanged(0)
        } else {
            seatUserAdapter.notifyItemChanged(0, true)
        }
    }

    override fun getRoomSeatSuccess(seatList: MutableList<RoomSeatInfo>) {
        seatUserAdapter.submitList(seatList)
    }


    override fun addHostSurfaceView() {
        seatUserAdapter.notifyItemChanged(0)
    }


    override fun refreshSeatInfo(it: MutableList<RoomSeatInfo>, uid: Int) {
        //seatUserAdapter.submitList(it)
        for (i in 0 until it.size) {
            val seatInfo = it[i]
            if (seatInfo.roomUserSeatInfo?.userId?.toInt() == uid) {
                seatUserAdapter[i] = seatInfo
                seatUserAdapter.notifyItemChanged(i)
            }
        }
    }

    override fun refreshSeatMicStatus(seatPosition: Int, mickUser: Boolean) {
        seatUserAdapter.getItem(seatPosition)?.mikeUser = mickUser
    }



    override fun userLeaveChanged(uid: Int) {
        super.userLeaveChanged(uid)
        for (i in 0 until seatUserAdapter.items.size) {
            val item = seatUserAdapter.getItem(i) ?: break
            if (item.roomUserSeatInfo?.userId?.toInt() == uid) {
                if (roomSourceBean.ownerInfo?.userId != uid.toString()) {
                    item.roomUserSeatInfo = null
                }
                seatUserAdapter.notifyItemChanged(i)
            }
        }
    }

    override fun userVideoStatusChanged(uid: Int, isShowProload: Boolean) {
        super.userVideoStatusChanged(uid, isShowProload)
        seatUserAdapter.items.forEach {
            if (it.roomUserSeatInfo?.userId?.toInt() == uid) {
                it.ifLeave = isShowProload
            }
        }
    }

    override fun isInSeatByUserId(uid: Int): Boolean {
        for (j in seatUserAdapter.items.indices) {
            val roomSeatResListDTO =
                seatUserAdapter.getItem(j) ?: return false
            val roomUserSeatInfo =
                roomSeatResListDTO.roomUserSeatInfo
            if (roomUserSeatInfo != null) {
                val userId: Int = roomUserSeatInfo.userId.toInt()
                if (userId == uid) {
                    return true
                }
            }
        }
        return false
    }

    override fun doShowFloatWindow() {
        super.doShowFloatWindow()
        val findViewHolderForAdapterPosition =
            seatUserAdapter.recyclerView.findViewHolderForAdapterPosition(0) as ThreeRoomSeatAdapter.VH
        val surfaceView =
            findViewHolderForAdapterPosition.binding.anchorSeatInfo.itemVideoSf.tag as View?
        LiveRoomVideoMiniManager.getInstance()
            .show(mContext, 2, roomSourceBean.ownerInfo, object : PermissionListener {
                override fun onSuccess() {
                    MiniWindowManager.switchLiveToMiniFloat(mContext)
                }

                override fun onFail() {}
            }, surfaceView)
    }
}