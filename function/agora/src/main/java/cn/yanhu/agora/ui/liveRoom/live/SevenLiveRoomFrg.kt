package cn.yanhu.agora.ui.liveRoom.live

import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import cn.yanhu.agora.adapter.liveRoom.LiveRoomRoseRankAdapter
import cn.yanhu.agora.adapter.liveRoom.SevenRoomSeatAdapter
import cn.yanhu.agora.bean.UserReceiveRoseInfo
import cn.yanhu.agora.pop.LiveRoomUserRoseDetailPop
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.manager.AppCacheManager
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.layoutmanager.QuickGridLayoutManager
import cn.yanhu.agora.R
import cn.yanhu.agora.bean.RoomOnlineResponse
import cn.yanhu.agora.databinding.ViewSevenRoomRankViewBinding
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
class SevenLiveRoomFrg : BaseLiveRoomFrg() {

    private val rankAdapter by lazy { LiveRoomRoseRankAdapter() }
    override fun initData() {
        seatUserAdapter =
            SevenRoomSeatAdapter()
        addRankView()
        super.initData()
        val layoutManager = mBinding.rvSeat.layoutManager as QuickGridLayoutManager
        layoutManager.spanCount = 3
        mBinding.rvSeat.adapter = seatUserAdapter
        getRoseRankList()
        if (!isOwner) {
            rankViewBinding.tvOnlineNum.visibility = View.GONE
        }
    }

    private lateinit var rankViewBinding: ViewSevenRoomRankViewBinding
    private fun addRankView() {
        val rankView =
            LayoutInflater.from(mContext).inflate(R.layout.view_seven_room_rank_view, null)
        rankViewBinding = DataBindingUtil.bind(rankView)!!
        val rvRank = rankViewBinding.rvRank
        val tvOnlineNum = rankViewBinding.tvOnlineNum
        rvRank.adapter = rankAdapter
        rankAdapter.setOnItemClickListener { _, _, _ ->
            userReceiveRoseInfo?.apply {
                showRankListPop()
            }
        }
        tvOnlineNum.setOnSingleClickListener {
            showOnlineUserList()
        }
        mBinding.flCustomView.addView(rankView)
    }

    override fun refreshOnlineUser(onlineResponse: RoomOnlineResponse) {
        rankViewBinding.tvOnlineNum.text = onlineResponse.onlineNum.toString()
    }

    private var liveRoomUserRoseDetailPop: LiveRoomUserRoseDetailPop? = null

    /**
     * 显示排行榜单
     */
    private fun showRankListPop() {
        if (CommonUtils.isPopShow(liveRoomUserRoseDetailPop)) {
            return
        }
        liveRoomUserRoseDetailPop =
            LiveRoomUserRoseDetailPop.showDialog(mContext, userReceiveRoseInfo!!)
    }

    override fun getRoseRankList() {
        super.getRoseRankList()
        mViewModel.getRoomRoseList(roomId)
    }

    private var userReceiveRoseInfo: UserReceiveRoseInfo? = null
    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.roseRankListObservable.observe(this) { it ->
            parseState(it, {
                userReceiveRoseInfo = it
                val list = it.list
                val takeList = list.take(5)
                rankAdapter.submitList(takeList)
            })
        }
    }

    override fun initListener() {
        super.initListener()
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

    private val childItemClickListener =
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
                                showOnlineUserList()
                            } else {
                                //上麦
                                val seatNum = item.id.toString()
                                userSetSeat(
                                    if (roomSourceBean.autoSeat) SEAT_TYPE_AUTO else SEAT_TYPE_APPLY,
                                    seatNum
                                )
                            }
                        } else {
                            if (roomUserSeatInfo.userId == AppCacheManager.userId) {
                                showUserPop(roomUserSeatInfo.userId)
                            } else {
                                showSendGiftPop(roomUserSeatInfo)
                            }
                        }
                    }
                }
            }

        }

}