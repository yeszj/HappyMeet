package cn.yanhu.agora.ui.liveRoom.live

import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import cn.yanhu.agora.adapter.liveRoom.LiveRoomRoseRankAdapter
import cn.yanhu.agora.adapter.liveRoom.MoreSeatRoomAdapter
import cn.yanhu.agora.bean.UserReceiveRoseInfo
import cn.yanhu.agora.pop.LiveRoomUserRoseRankPop
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.manager.AppCacheManager
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.layoutmanager.QuickGridLayoutManager
import cn.yanhu.agora.R
import cn.yanhu.agora.bean.AngleRankInfo
import cn.yanhu.agora.bean.RoomOnlineResponse
import cn.yanhu.agora.databinding.ViewSevenRoomRankViewBinding
import cn.yanhu.agora.pop.RoomAngleRankPop
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.router.PageIntentUtil
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.status.BaseBean

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
open class SevenLiveRoomFrg : BaseLiveRoomFrg() {

    protected val rankAdapter by lazy { LiveRoomRoseRankAdapter() }
    override fun initData() {
        roomSourceBean = requireArguments().getSerializable(IntentKeyConfig.DATA) as RoomDetailInfo
        seatUserAdapter =
            MoreSeatRoomAdapter(roomSourceBean.getFragmentType())
        super.initData()
        addRankView()
        val layoutManager = mBinding.rvSeat.layoutManager as QuickGridLayoutManager
        layoutManager.spanCount = 3
        mBinding.rvSeat.adapter = seatUserAdapter
        getRoseRankList()

    }

    private lateinit var rankViewBinding: ViewSevenRoomRankViewBinding
    protected open fun addRankView() {
        val rankView =
            LayoutInflater.from(mContext).inflate(R.layout.view_seven_room_rank_view, null)
        rankViewBinding = DataBindingUtil.bind(rankView)!!
        val rvRank = rankViewBinding.rvRank
        rankViewBinding.isAngle = roomType == RoomListBean.TYPE_SEVEN_ANGLE
        rvRank.adapter = rankAdapter
        rankAdapter.setOnItemClickListener { _, _, _ ->
            userReceiveRoseInfo?.apply {
                showRankListPop()
            }
        }
        rankViewBinding.ivExit.setOnSingleClickListener {
            showFloatWindow(1)
        }
        rankViewBinding.tvOnlineNum.setOnSingleClickListener {
            showOnlineUserList()
        }
        rankViewBinding.ivRank.setOnSingleClickListener {
            showAngleRankPop()
        }
        rankViewBinding.ivRule.setOnSingleClickListener {
            PageIntentUtil.url2Page(mContext, WebUrlManager.ANGLE_ROOM_RULE)
        }
        mBinding.flCustomView.addView(rankView)
        if (!isOwner) {
            rankViewBinding.tvOnlineNum.visibility = View.INVISIBLE
        }
    }

    override fun refreshOnlineUser(onlineResponse: RoomOnlineResponse) {
        rankViewBinding.tvOnlineNum.text = onlineResponse.onlineNum.toString()
    }

    private var liveRoomUserRoseDetailPop: LiveRoomUserRoseRankPop? = null

    /**
     * 显示排行榜单
     */
    protected fun showRankListPop() {
        if (CommonUtils.isPopShow(liveRoomUserRoseDetailPop)) {
            return
        }
        liveRoomUserRoseDetailPop =
            LiveRoomUserRoseRankPop.showDialog(mContext, userReceiveRoseInfo!!)
    }

    /**
     * 天使榜单
     */
    private var roomAngleRankPop: RoomAngleRankPop? = null
    protected fun showAngleRankPop() {
        if (CommonUtils.isPopShow(roomAngleRankPop)) {
            return
        }
        mViewModel.getRoomAngleRank(roomId, object : OnRequestResultListener<List<AngleRankInfo>> {
            override fun onSuccess(data: BaseBean<List<AngleRankInfo>>) {
                var rankList = data.data
                if (rankList == null) {
                    rankList = mutableListOf()
                }
                roomAngleRankPop = RoomAngleRankPop.showDialog(mContext, rankList)
            }
        })
    }

    override fun getRoseRankList() {
        super.getRoseRankList()
        mViewModel.getRoomRoseList(roomId)
    }

    protected var userReceiveRoseInfo: UserReceiveRoseInfo? = null
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