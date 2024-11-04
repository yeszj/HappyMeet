package cn.yanhu.agora.ui.liveRoom.live

import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import cn.yanhu.agora.adapter.liveRoom.ThreeRoomSeatAdapter
import cn.yanhu.agora.bean.RoomOnlineResponse
import cn.yanhu.agora.databinding.ViewThreeRoomTopViewBinding
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.imchat.manager.EmMsgManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.status.BaseBean
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
class ThreeLiveRoomFrg : BaseLiveRoomFrg() {
    override fun initData() {
        seatUserAdapter =
            ThreeRoomSeatAdapter()
        addTopTitleView()
        super.initData()
        mBinding.rvSeat.adapter = seatUserAdapter
        if (isOwner){
            topTitleBinding.tvOnlineNum.visibility = View.VISIBLE
        }else{
            topTitleBinding.tvOnlineNum.visibility = View.INVISIBLE
        }
    }

    private lateinit var topTitleBinding: ViewThreeRoomTopViewBinding
    private fun addTopTitleView(){
        val rankView =
            LayoutInflater.from(mContext).inflate(cn.yanhu.agora.R.layout.view_three_room_top_view, null)
        topTitleBinding = DataBindingUtil.bind(rankView)!!
        topTitleBinding.tvOnlineNum.setOnSingleClickListener {
            showOnlineUserList()
        }
        topTitleBinding.ivExit.setOnSingleClickListener {
            showFloatWindow(1)
        }
        mBinding.flTopView.addView(rankView)
    }

    override fun refreshOnlineUser(onlineResponse: RoomOnlineResponse) {
        super.refreshOnlineUser(onlineResponse)
        topTitleBinding.tvOnlineNum.text = onlineResponse.onlineNum.toString()
    }


    override fun getRoomInfoSuccess() {
        super.getRoomInfoSuccess()
        topTitleBinding.roomInfo = roomSourceBean
    }

    private val childItemClickListener =
        object : BaseQuickAdapter.OnItemChildClickListener<RoomSeatInfo> {
            override fun onItemClick(
                adapter: BaseQuickAdapter<RoomSeatInfo, *>, view: View, position: Int
            ) {
                val item = seatUserAdapter.getItem(position) ?: return

                when (view.id) {
                    cn.yanhu.agora.R.id.tv_switch -> {
                        //切换成专属房间/大厅
                        showSwitchRoomTypePop()
                    }

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
                                (seatUserAdapter as ThreeRoomSeatAdapter).showUserList(if (item.id == 2) "1" else "2")
                            } else {
                                //上麦
                                if (AppCacheManager.isMan()) {
                                    if (position == 1) {
                                        userSetSeat(if (roomSourceBean.autoSeat) SEAT_TYPE_AUTO else SEAT_TYPE_APPLY)
                                    } else {
                                        showToast("该座位仅对女用户开放")
                                    }
                                } else {
                                    if (position == 2) {
                                        userSetSeat(if (roomSourceBean.autoSeat) SEAT_TYPE_AUTO else SEAT_TYPE_APPLY)
                                    } else {
                                        showToast("该座位仅对男用户开放")
                                    }
                                }
                            }
                        } else {
                            if (roomUserSeatInfo.userId == AppCacheManager.userId){
                                showUserPop(roomUserSeatInfo.userId)
                            }else{
                                showSendGiftPop(roomUserSeatInfo)
                            }
                        }
                    }
                }
            }

        }

    override fun initListener() {
        super.initListener()
        seatUserAdapter.addOnItemChildClickListener(
            cn.yanhu.agora.R.id.anchorSeatInfo, childItemClickListener
        )
        seatUserAdapter.addOnItemChildClickListener(
            cn.yanhu.agora.R.id.vg_parent, childItemClickListener
        )
        seatUserAdapter.addOnItemChildClickListener(
            cn.yanhu.agora.R.id.iv_voiceStatus, childItemClickListener
        )
        seatUserAdapter.addOnItemChildClickListener(
            cn.yanhu.agora.R.id.tv_switch, childItemClickListener
        )
    }

    private fun showSwitchRoomTypePop() {
        val content = Spans.builder()
            .text(if (roomSourceBean.isPrivateRoom()) "确定要转为大厅直播吗？" else "转为专属房间后只保留麦上的男女嘉宾，确认要转换吗？\n\n")
            .text(if (roomSourceBean.isPrivateRoom()) "" else "温馨提示：专属房间需要付费，男嘉宾同意后才可转换成功")
            .color(
                CommonUtils.getColor(
                    R.color.colorMain
                )
            ).build()

        DialogUtils.showConfirmDialog(if (roomSourceBean.isPrivateRoom()) "转为大厅房间" else "转为专属房间",
            {
                switchRoomType()
            },
            {

            },
            content,
            cancelBg = R.drawable.shape_cancel_btn_r30
        )
    }

    private fun switchRoomType() {
        val roomType = if (roomSourceBean.isPrivateRoom()) 1 else 2
        mViewModel.switchRoomType(roomId,
            roomType.toString(),
            object : OnRequestResultListener<Boolean> {
                override fun onSuccess(data: BaseBean<Boolean>) {
                    if (roomType == 1) {
                        roomSourceBean.roomType = roomType
                        EmMsgManager.sendCmdMessageToChatRoom(
                            roomSourceBean.uid, "", ChatConstant.ACTION_MSG_SWITCH_TYPE_PLAZA
                        )
                        showToast("房间已切换为大厅")
                    } else {
                        showToast("已发送消息至男嘉宾，男嘉宾同意后可转至专属房间")
                    }
                }
            })
    }

}