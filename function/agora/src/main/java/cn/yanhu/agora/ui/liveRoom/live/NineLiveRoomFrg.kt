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
 * desc:九人房
 */
class NineLiveRoomFrg : SevenLiveRoomFrg() {


    private lateinit var rankViewBinding: ViewNineRoomRankViewBinding

    override fun initRankView() {
        val rankView =
            LayoutInflater.from(mContext).inflate(R.layout.view_nine_room_rank_view, null)
        rankViewBinding = DataBindingUtil.bind(rankView)!!
        mBinding.flTopView.addView(rankView)
    }
    override fun addRankView() {
        rankViewBinding.apply {
            bindNineRankView()
        }
    }

    /**
     * 上麦成功
     */
    override fun setHasSeatUpStatus() {
        super.setHasSeatUpStatus()
        showAnimSwitch()
    }

    override fun setSeatOutSuccess() {
        super.setSeatOutSuccess()
        rankViewBinding.vgEnterAnim.visibility = View.GONE
    }

    private fun NineLiveRoomFrg.showAnimSwitch() {
        rankViewBinding.vgEnterAnim.visibility = View.VISIBLE
        changeEnterAnimStatus(rankViewBinding.iconEnter)
    }

    private fun ViewNineRoomRankViewBinding.bindNineRankView() {
        toggleAutoSeat.setOnSingleClickListener {
            showSetAutoSeat()
        }
        changeGiftAudioStatus(ivAudio)

        roomInfo = roomSourceBean
        isAngle = roomType == RoomListBean.TYPE_NINE_ANGLE
        isSong = roomType == RoomListBean.TYPE_NINE_SONG
        this.isRoomOwner = isOwner
        rvRank.adapter = rankAdapter
        if (isOwner){
            showAnimSwitch()
        }
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
            changeGiftAudioStatus(ivAudio,true)

        }
        vgEnterAnim.setOnSingleClickListener {
            changeEnterAnimStatus(iconEnter,true)
        }
    }



    override fun refreshAutoSeat() {
        rankViewBinding.roomInfo = roomSourceBean
    }

    override fun refreshOnlineUser(onlineResponse: RoomOnlineResponse) {
        rankViewBinding.tvOnlineNum.text = onlineResponse.onlineNum.toString()
    }
}