package cn.yanhu.agora.ui.liveRoom.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import cn.yanhu.agora.R
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.LiveRoomSeatBean
import cn.yanhu.agora.bean.UserReceiveRoseInfo
import cn.yanhu.agora.databinding.AdapterLiveRoomUserSeatItemBinding
import cn.yanhu.agora.databinding.ViewNineSongRoomSeatBinding
import cn.yanhu.agora.databinding.ViewNineSongScaleRoomSeatBinding
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.pop.LiveRoomUserRoseRankPop
import cn.yanhu.agora.ui.liveRoom.TextureViewPool
import cn.yanhu.agora.ui.liveRoom.live.MoreSeatLiveRoomFrg
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.bean.SeatUserInfo
import cn.yanhu.commonres.manager.AppCacheManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean


/**
 * @author: zhengjun
 * created: 2025/1/10
 * desc:
 */
@SuppressLint("ViewConstructor")
open class NineRoomSeatView(
    context: Context,
    private var isScaleStyle: Boolean,
    var roomType: Int,
    var currentRoomId: String,
    var isRoomOwner: Boolean
) :
    LinearLayout(context) {

    private lateinit var mBinding: ViewDataBinding
    private var localUserId = AppCacheManager.userId
    private fun initView(context: Context) {
        mBinding = if (isScaleStyle) {
            DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.view_nine_song_scale_room_seat, this, true
            )
        } else {
            DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.view_nine_song_room_seat, this, true
            )
        }
    }


    var seatInfoList: MutableList<RoomSeatInfo> = mutableListOf()
    fun setSeatList(seatList: MutableList<RoomSeatInfo>, isReload: Boolean = false) {
        this.seatInfoList = seatList
        if (mBinding is ViewNineSongRoomSeatBinding) {
            bindSongSeat(isReload)
        } else {
            bindSongScaleSeat(isReload)
        }
    }

    fun setSeatHost(seatList: MutableList<RoomSeatInfo>) {
        this.seatInfoList = seatList

        if (mBinding is ViewNineSongRoomSeatBinding) {
            bindSeatByPosition(0, seatList[0])
        } else {
            bindScaleByPosition(0, seatList[0])
        }
    }

    private fun bindSongScaleSeat(
        isReload: Boolean
    ) {
        for (i in 0 until seatInfoList.size) {
            val seatInfo = seatInfoList[i]
            bindScaleByPosition(i, seatInfo, isReload)
        }
    }

    fun bindRoseInfo(
        i: Int, seatInfo: RoomSeatInfo
    ) {
        val seatBinding = getSeatBinding(i)
        seatBinding?.apply {
            this.seatInfo = seatInfo
        }
    }

    fun bindScaleRoseInfo(
        i: Int, seatInfo: RoomSeatInfo
    ) {
        val seatScaleBinding = getSeatScaleBinding(i)
        seatScaleBinding?.apply {
            this.seatInfo = seatInfo
        }
    }

    fun bindScaleByPosition(
        i: Int, seatInfo: RoomSeatInfo, isReload: Boolean = false
    ) {
        val seatScaleBinding = getSeatScaleBinding(i)
        seatScaleBinding?.apply {
            bindSeatInfo(i,seatInfo,isReload)
        }
    }

    private fun bindSongSeat(
        isReload: Boolean = false
    ) {
        for (i in 0 until seatInfoList.size) {
            val seatInfo = seatInfoList[i]
            bindSeatByPosition(i, seatInfo, isReload)
        }
    }


    private fun getSeatScaleBinding(i: Int): AdapterLiveRoomUserSeatItemBinding? {
        val songBinding = mBinding as ViewNineSongScaleRoomSeatBinding
        when (i) {
            0 -> {
                return songBinding.seat1
            }

            1 -> {
                return songBinding.seat2
            }

            2 -> {
                return songBinding.seat3
            }

            3 -> {
                return songBinding.seat4
            }

            4 -> {
                return songBinding.seat5
            }

            5 -> {
                return songBinding.seat6
            }

            6 -> {
                return songBinding.seat7
            }

            7 -> {
                return songBinding.seat8
            }

            8 -> {
                return songBinding.seat9
            }

            else -> {
                return null
            }
        }
    }

    private fun getSeatBinding(i: Int): AdapterLiveRoomUserSeatItemBinding? {
        val songBinding = mBinding as ViewNineSongRoomSeatBinding
        when (i) {
            0 -> {
                return songBinding.seat1
            }

            1 -> {
                return songBinding.seat2
            }

            2 -> {
                return songBinding.seat3
            }

            3 -> {
                return songBinding.seat4
            }

            4 -> {
                return songBinding.seat5
            }

            5 -> {
                return songBinding.seat6
            }

            6 -> {
                return songBinding.seat7
            }

            7 -> {
                return songBinding.seat8
            }

            8 -> {
                return songBinding.seat9
            }

            else -> {
                return null
            }
        }
    }


    fun bindSeatByPosition(
        i: Int, seatInfo: RoomSeatInfo, isReload: Boolean = false
    ) {
        val seatBinding = getSeatBinding(i)
        seatBinding?.apply {
            bindSeatInfo(i,seatInfo,isReload)
        }
    }
    private fun AdapterLiveRoomUserSeatItemBinding.bindSeatInfo(  i: Int, seatInfo: RoomSeatInfo, isReload: Boolean = false){
        seatInfoList[i] = seatInfo
        val tag = this.vgParent.tag as RoomSeatInfo?
        val tagUserInfo = tag?.roomUserSeatInfo
        if (tag == null || tagUserInfo?.userId != seatInfo.roomUserSeatInfo?.userId || seatInfo.roomUserSeatInfo == null || this.itemVideoSf.childCount <= 0) {
            this.vgParent.tag = seatInfo
            bindItemInfo(seatInfo, i)
        } else {
            if (tagUserInfo?.roseNum != seatInfo.roomUserSeatInfo?.roseNum || tag.mikeUser != seatInfo.mikeUser) {
                this.seatInfo = seatInfo
            }
            if (isReload) {
                upDataSeatVideo(seatInfo, i)
            }
        }
    }

    private fun AdapterLiveRoomUserSeatItemBinding.bindItemInfo(
        item: RoomSeatInfo?, position: Int
    ) {
        setItemListener(position)

        setItemStyle(position)

        item.apply {
            tvSeatIndex.text = (item!!.id-1).toString()
            seatInfo = item
            executePendingBindings()
            upDataSeatVideo(item, position)
        }
    }


    private fun AdapterLiveRoomUserSeatItemBinding.setItemStyle(
        position: Int
    ) {
        if (vgParent.getTag(cn.yanhu.commonres.R.id.tag_set_style) as Boolean? ==true){
            return
        }
        vgParent.setTag(cn.yanhu.commonres.R.id.tag_set_style,true)
        this.currentRoomType = roomType
        this.isOwner = isRoomOwner
        if (position == 0) {
            tvOwner.visibility = VISIBLE
        } else {
            tvOwner.visibility = INVISIBLE
        }
        if (isScaleStyle) {
            if (position == 4) {
                ViewUtils.setViewHeight(
                    vgParent,
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_320)
                )
            } else {
                ViewUtils.setViewHeight(
                    vgParent,
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_80)
                )
            }

        } else {
            ViewUtils.setViewHeight(
                vgParent,
                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_120)
            )
        }

        val isShowNoTopBg = if (!isScaleStyle) {
            position == 1 || position == 4 || position == 7
        } else {
            position == 0 || position == 2 || position == 5 || position == 6 || position == 7 || position == 8
        }

        if (isShowNoTopBg) {
            vgParent.setBackgroundResource(R.drawable.bg_seat_no_top_stroke)
        } else {
            vgParent.setBackgroundResource(R.drawable.bg_seat_bottom_stroke)
        }
    }

    private fun AdapterLiveRoomUserSeatItemBinding.setItemListener(position: Int) {
        if (ivChooseSong.tag != null && ivChooseSong.tag == true) {
            return
        }
        ivChooseSong.tag = true
        ivChooseSong.setOnSingleClickListener {
            onClickSeatListener?.onChildClickListener(ivChooseSong, position, this.seatInfo)
        }
        itemVideoSf.setOnSingleClickListener {
            onClickSeatListener?.onChildClickListener(itemVideoSf, position, this.seatInfo)
        }
        vgEmptySeat.setOnSingleClickListener {
            onClickSeatListener?.onChildClickListener(vgEmptySeat, position, this.seatInfo)
        }
        ivExpand.setOnSingleClickListener {
            if (isScaleStyle && position == 4) {
                this.seatInfo?.isExpand = true
            }
            onClickSeatListener?.onChildClickListener(ivExpand, position, this.seatInfo)
        }
        ivVoiceStatus.setOnSingleClickListener {
            onClickSeatListener?.onChildClickListener(ivVoiceStatus, position, this.seatInfo)
        }
        ivSendRose.setOnSingleClickListener {
            onClickSeatListener?.onChildClickListener(ivSendRose, position, this.seatInfo)
        }
        viewRank.setOnSingleClickListener {
            showUserReceiveRoseDetailPop(this.seatInfo)
        }
    }

    private fun AdapterLiveRoomUserSeatItemBinding.upDataSeatVideo(
        dto: RoomSeatInfo,
        position: Int
    ) {
        val seatInfo = dto.roomUserSeatInfo

        if (seatInfo != null) {
            this.isSelf = seatInfo.userId == localUserId

            val liveRoomSeatBean = MoreSeatLiveRoomFrg.surfaceViewList[dto.id - 1]

            val surfaceView =
                getOrCreateSurfaceView(liveRoomSeatBean,seatInfo)

            //处理SurfaceView的添加
            addVideoSf(surfaceView, dto, position)
            itemVideoSf.tag = surfaceView
        } else {
            handleEmptySeat(position)
        }
    }


    private fun AdapterLiveRoomUserSeatItemBinding.getOrCreateSurfaceView(
        liveRoomSeatBean: LiveRoomSeatBean?,seatUserInfo: SeatUserInfo
    ): View {
        val cacheSurfaceView = liveRoomSeatBean?.surfaceView
        if (cacheSurfaceView != null
        ) {
            if (seatUserInfo.userId == liveRoomSeatBean.uid.toString()){
                logcom("addSurfaceView","使用缓存中的SurfaceView")
                return cacheSurfaceView
            }else{
                TextureViewPool.recycleTextureView(cacheSurfaceView as TextureView)
                liveRoomSeatBean.surfaceView = null
            }
        }
        val surfaceView = TextureView(context)
        surfaceView.setLayoutParams(
            ViewGroup.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )
        logcom("addSurfaceView","新建SurfaceView")
        return surfaceView
    }


    /**
     * 处理空座位情况
     */
    private fun AdapterLiveRoomUserSeatItemBinding.handleEmptySeat(position: Int) {
        this.isSelf = false
        this.itemVideoSf.removeAllViews()
        setEmptySeatInfo()
    }

    private fun AdapterLiveRoomUserSeatItemBinding.addVideoSf(
        surfaceView: View,
        dto: RoomSeatInfo,
        position: Int
    ) {
        val userId = dto.roomUserSeatInfo!!.userId
        if (localUserId == userId) {
            if (isScaleStyle) {
                if (dto.isExpand) {
                    //270 320  560  640
                    AgoraManager.getInstance().setVideoEncoderConfiguration(560, 640)
                } else {
                    AgoraManager.getInstance().setVideoEncoderConfiguration(208, 208)
                }
            } else {
                AgoraManager.getInstance().setVideoEncoderConfiguration(320, 320)
            }
        }

        // 先移除父视图
        ViewUtils.removeViewFormParent(surfaceView)
        itemVideoSf.removeAllViews()
        itemVideoSf.addView(surfaceView)

        MoreSeatLiveRoomFrg.surfaceViewList[dto.id - 1] =
            LiveRoomSeatBean(userId.toInt(), surfaceView)

        if (!AgoraManager.getInstance().subScribeUserList.containsKey(userId.toInt())) {
            AgoraManager.getInstance().setupVideo(
                userId.toInt(),
                userId == localUserId,
                surfaceView
            )
        }
        if (userId == localUserId) {
            AgoraManager.getInstance().muteLocalAudioStream(!dto.mikeUser)
        }
    }


    private var liveRoomUserRoseDetailPop: LiveRoomUserRoseRankPop? = null
    private fun showUserReceiveRoseDetailPop(item: RoomSeatInfo?) {
        request({
            agoraRxApi.getRoomUserRoseList(
                currentRoomId, item?.roomUserSeatInfo?.userId
            )
        }, object : OnRequestResultListener<UserReceiveRoseInfo> {
            override fun onSuccess(data: BaseBean<UserReceiveRoseInfo>) {
                if (CommonUtils.isPopShow(liveRoomUserRoseDetailPop)) {
                    return
                }
                liveRoomUserRoseDetailPop = LiveRoomUserRoseRankPop.showDialog(context, data.data!!)
            }
        })
    }

    private fun AdapterLiveRoomUserSeatItemBinding.setEmptySeatInfo(
    ) {
        if (isRoomOwner) {
            //是房主
            tvJoinSeat.text = "邀请上麦"
        } else {
            tvJoinSeat.text = "申请上麦"
            tvJoinSeat.setBackgroundResource(R.drawable.bg_seat_invite)
        }

    }

    private var onClickSeatListener: OnClickSeatListener? = null
    fun setOnClickSeatListener(onClickSeatListener: OnClickSeatListener) {
        this.onClickSeatListener = onClickSeatListener
    }

    fun userVideoStatusChanged(uid: Int, showPreload: Boolean, networkType: Int) {
        seatInfoList.forEach {
            if (it.roomUserSeatInfo?.userId?.toInt() == uid) {
                if (showPreload) {
                    if (networkType == 1) {
                        it.ifLeave = true
                    }
                } else {
                    it.ifLeave = false
                }
                return
            }
        }
    }

    fun userNetChanged(uid: String, ifNetDisConnect: Boolean) {
        seatInfoList.forEach {
            if (it.roomUserSeatInfo?.userId == uid && it.ifNetDisConnect != ifNetDisConnect) {
                it.ifNetDisConnect = ifNetDisConnect
                return
            }
        }
    }

    init {
        initView(context)
    }
}