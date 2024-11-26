package cn.yanhu.agora.ui.liveRoom.live

import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import cn.yanhu.agora.R
import cn.yanhu.agora.bean.RoomOnlineResponse
import cn.yanhu.agora.databinding.ViewNineRoomRankViewBinding
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.router.PageIntentUtil

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
class NineLiveRoomFrg : SevenLiveRoomFrg() {


    private lateinit var rankViewBinding: ViewNineRoomRankViewBinding
    override fun addRankView() {
        val rankView =
            LayoutInflater.from(mContext).inflate(R.layout.view_nine_room_rank_view, null)
        rankViewBinding = DataBindingUtil.bind(rankView)!!
        val rvRank = rankViewBinding.rvRank
        rankViewBinding.isAngle = roomType == RoomListBean.TYPE_NINE_ANGLE
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
        mBinding.flTopView.addView(rankView)
        if (!isOwner) {
            rankViewBinding.tvOnlineNum.visibility = View.INVISIBLE
        }
    }

    override fun refreshOnlineUser(onlineResponse: RoomOnlineResponse) {
        rankViewBinding.tvOnlineNum.text = onlineResponse.onlineNum.toString()
    }
}