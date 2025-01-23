package cn.yanhu.agora.ui.liveRoom.live

import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import cn.yanhu.agora.R
import cn.yanhu.agora.bean.RoomOnlineResponse
import cn.yanhu.agora.databinding.ViewNineRoomRankViewBinding
import cn.yanhu.agora.pop.CrownedUserListPop
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.manager.AppCacheManager
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
        rankViewBinding.apply {
            bindNineRankView(rankView)
        }

    }

    private fun ViewNineRoomRankViewBinding.bindNineRankView(rankView: View?) {
        toggleAutoSeat.setOnSingleClickListener {
            showSetAutoSeat()
        }
        if (AppCacheManager.isOpenGiftAudio) {
            ivAudio.setImageResource(R.drawable.svg_voice_on)
        } else {
            ivAudio.setImageResource(R.drawable.svg_voice_off)
        }
        roomInfo = roomSourceBean
        isAngle = roomType == RoomListBean.TYPE_NINE_ANGLE
        isSong = roomType == RoomListBean.TYPE_NINE_SONG
        this.isRoomOwner = isOwner
        rvRank.adapter = rankAdapter
        rankAdapter.setOnItemClickListener { _, _, _ ->
            userReceiveRoseInfo?.apply {
                showRankListPop()
            }
        }
        ivExit.setOnSingleClickListener {
            showFloatWindow(1)
        }
        tvOnlineNum.setOnSingleClickListener {
            showOnlineUserList()
        }
        ivRank.setOnSingleClickListener {
            showAngleRankPop()
        }
        ivRule.setOnSingleClickListener {
            PageIntentUtil.url2Page(mContext, WebUrlManager.ANGLE_ROOM_RULE)
        }
        ivCrowned.setOnSingleClickListener {
            showCrownedListPop(CrownedUserListPop.TYPE_ANGLE)
        }
        vgGiftAudio.setOnSingleClickListener {
            if (AppCacheManager.isOpenGiftAudio) {
                ivAudio.setImageResource(R.drawable.svg_voice_off)
                AppCacheManager.isOpenGiftAudio = false
            } else {
                ivAudio.setImageResource(R.drawable.svg_voice_on)
                AppCacheManager.isOpenGiftAudio = true
            }
        }
        mBinding.flTopView.addView(rankView)
    }

    override fun refreshAutoSeat() {
        rankViewBinding.roomInfo = roomSourceBean
    }

    override fun refreshOnlineUser(onlineResponse: RoomOnlineResponse) {
        rankViewBinding.tvOnlineNum.text = onlineResponse.onlineNum.toString()
    }
}