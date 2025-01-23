package cn.yanhu.agora.ui.liveRoom.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import cn.yanhu.agora.R
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.LiveRoomSeatBean
import cn.yanhu.agora.bean.UserReceiveRoseInfo
import cn.yanhu.agora.databinding.AdapterSevenRoomUserSeatItemBinding
import cn.yanhu.agora.databinding.ViewNineSongRoomSeatBinding
import cn.yanhu.agora.databinding.ViewNineSongScaleRoomSeatBinding
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.pop.LiveRoomUserRoseRankPop
import cn.yanhu.agora.ui.liveRoom.live.SongLiveRoomFrg
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.bean.RoomSeatInfo
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
open class NineSongRoomSeatView(context: Context, private var isScaleStyle: Boolean) :
    LinearLayout(context) {

    private lateinit var mBinding: ViewDataBinding
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


    private var currentRoomId: String = ""

    fun setRoomId(roomId: String, isOwner: Boolean) {
        this.isRoomOwner = isOwner
        currentRoomId = roomId
    }

    private var isRoomOwner = false
    var seatInfoList: MutableList<RoomSeatInfo> = mutableListOf()
    fun setSeatList(seatList: MutableList<RoomSeatInfo>) {
        this.seatInfoList = seatList
        if (mBinding is ViewNineSongRoomSeatBinding) {
            bindSongSeat(seatList)
        } else {
            bindSongScaleSeat(seatList)
        }
    }


    private fun bindSongScaleSeat(
        seatList: MutableList<RoomSeatInfo>,
    ) {
        for (i in 0 until seatList.size) {
            val seatInfo = seatList[i]
            bindScaleByPosition(i, seatInfo)
        }
    }

    fun bindRoseInfo(
        i: Int, seatInfo: RoomSeatInfo
    ) {
        val songBinding = mBinding as ViewNineSongRoomSeatBinding
        when (i) {
            0 -> {
                songBinding.seat1.apply {
                    this.seatInfo = seatInfo
                }
            }

            1 -> {
                songBinding.seat2.apply {
                    this.seatInfo = seatInfo
                }
            }

            2 -> {
                songBinding.seat3.apply {
                    this.seatInfo = seatInfo
                }
            }

            3 -> {
                songBinding.seat4.apply {
                    this.seatInfo = seatInfo
                }
            }

            4 -> {
                songBinding.seat5.apply {
                    this.seatInfo = seatInfo
                }
            }

            5 -> {
                songBinding.seat6.apply {
                    this.seatInfo = seatInfo
                }
            }

            6 -> {
                songBinding.seat7.apply {
                    this.seatInfo = seatInfo
                }
            }

            7 -> {
                songBinding.seat8.apply { this.seatInfo = seatInfo }
            }

            8 -> {
                songBinding.seat9.apply { this.seatInfo = seatInfo }
            }
        }
    }

    fun bindScaleRoseInfo(
        i: Int, seatInfo: RoomSeatInfo
    ) {
        val songBinding = mBinding as ViewNineSongScaleRoomSeatBinding
        when (i) {
            0 -> {
                songBinding.seat1.apply {
                    this.seatInfo = seatInfo
                }
            }

            1 -> {
                songBinding.seat2.apply {
                    this.seatInfo = seatInfo
                }
            }

            2 -> {
                songBinding.seat3.apply {
                    this.seatInfo = seatInfo
                }
            }

            3 -> {
                songBinding.seat4.apply {
                    this.seatInfo = seatInfo
                }
            }

            4 -> {
                songBinding.seat5.apply {
                    this.seatInfo = seatInfo
                }
            }

            5 -> {
                songBinding.seat6.apply {
                    this.seatInfo = seatInfo
                }
            }

            6 -> {
                songBinding.seat7.apply {
                    this.seatInfo = seatInfo
                }
            }

            7 -> {
                songBinding.seat8.apply {
                    this.seatInfo = seatInfo
                }
            }

            8 -> {
                songBinding.seat9.apply {
                    this.seatInfo = seatInfo
                }
            }
        }
    }

    fun bindScaleByPosition(
        i: Int, seatInfo: RoomSeatInfo
    ) {
        val songBinding = mBinding as ViewNineSongScaleRoomSeatBinding
        when (i) {
            0 -> {
                songBinding.seat1.apply {
                    bindItemInfo(seatInfo, i)
                }
            }

            1 -> {
                songBinding.seat2.apply { bindItemInfo(seatInfo, i) }
            }

            2 -> {
                songBinding.seat3.apply { bindItemInfo(seatInfo, i) }
            }

            3 -> {
                songBinding.seat4.apply { bindItemInfo(seatInfo, i) }
            }

            4 -> {
                songBinding.seat5.apply { bindItemInfo(seatInfo, i) }
            }

            5 -> {
                songBinding.seat6.apply { bindItemInfo(seatInfo, i) }
            }

            6 -> {
                songBinding.seat7.apply { bindItemInfo(seatInfo, i) }
            }

            7 -> {
                songBinding.seat8.apply { bindItemInfo(seatInfo, i) }
            }

            8 -> {
                songBinding.seat9.apply { bindItemInfo(seatInfo, i) }
            }
        }
    }

    private fun bindSongSeat(
        seatList: MutableList<RoomSeatInfo>,
    ) {
        for (i in 0 until seatList.size) {
            val seatInfo = seatList[i]
            bindSeatByPosition(i, seatInfo)
        }
    }

    fun bindSeatByPosition(
        i: Int, seatInfo: RoomSeatInfo
    ) {
        seatInfoList[i] = seatInfo
        val songBinding = mBinding as ViewNineSongRoomSeatBinding
        when (i) {
            0 -> {
                songBinding.seat1.apply {
                    bindItemInfo(seatInfo, i)
                }
            }

            1 -> {
                songBinding.seat2.apply { bindItemInfo(seatInfo, i) }
            }

            2 -> {
                songBinding.seat3.apply { bindItemInfo(seatInfo, i) }
            }

            3 -> {
                songBinding.seat4.apply { bindItemInfo(seatInfo, i) }
            }

            4 -> {
                songBinding.seat5.apply { bindItemInfo(seatInfo, i) }
            }

            5 -> {
                songBinding.seat6.apply { bindItemInfo(seatInfo, i) }
            }

            6 -> {
                songBinding.seat7.apply { bindItemInfo(seatInfo, i) }
            }

            7 -> {
                songBinding.seat8.apply { bindItemInfo(seatInfo, i) }
            }

            8 -> {
                songBinding.seat9.apply { bindItemInfo(seatInfo, i) }
            }
        }
    }


    private fun AdapterSevenRoomUserSeatItemBinding.bindItemInfo(
        item: RoomSeatInfo?, position: Int
    ) {
        ivChooseSong.setOnSingleClickListener {
            onClickSeatListener?.onChildClickListener(ivChooseSong, position, item)
        }
        itemVideoSf.setOnSingleClickListener {
            onClickSeatListener?.onChildClickListener(itemVideoSf, position, item)
        }
        vgEmptySeat.setOnSingleClickListener {
            onClickSeatListener?.onChildClickListener(vgEmptySeat, position, item)
        }
        ivExpand.setOnSingleClickListener {
            onClickSeatListener?.onChildClickListener(ivExpand, position, item)
        }
        ivVoiceStatus.setOnSingleClickListener {
            onClickSeatListener?.onChildClickListener(ivVoiceStatus, position, item)
        }
        tvSeatIndex.text = (item!!.id - 1).toString()
        this.currentRoomType = RoomListBean.TYPE_SEVEN_SONG
        this.isOwner = isRoomOwner
        if (item.id == 1) {
            tvOwner.visibility = View.VISIBLE
        } else {
            tvOwner.visibility = View.INVISIBLE
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
        item.apply {
            seatInfo = item
            if (item.roomUserSeatInfo == null) {
                setEmptySeatInfo()
            }
            executePendingBindings()
        }
        upDataSeats(item, position)

        viewRank.setOnSingleClickListener {
            showUserReceiveRoseDetailPop(item)
        }
    }

    //更新座位状态
    private fun AdapterSevenRoomUserSeatItemBinding.upDataSeats(dto: RoomSeatInfo, position: Int) {

        if (dto.roomUserSeatInfo != null) {
            this.isSelf = dto.roomUserSeatInfo!!.userId == AppCacheManager.userId

            val liveRoomSeatBean: LiveRoomSeatBean? = SongLiveRoomFrg.surfaceViewList[position]
            var surfaceView: View?
            if (liveRoomSeatBean == null|| liveRoomSeatBean.surfaceView ==null || (liveRoomSeatBean.surfaceView as TextureView?)?.isAvailable == false) {
                surfaceView = TextureView(context)
                this.itemVideoSf.removeAllViews()
                this.itemVideoSf.addView(surfaceView)
            } else if (dto.roomUserSeatInfo!!.userId.toInt() != liveRoomSeatBean.uid) {
                surfaceView = liveRoomSeatBean.surfaceView
                if (surfaceView == null || (liveRoomSeatBean.surfaceView as TextureView?)?.isAvailable == false) {
                    surfaceView = TextureView(context)
                }
                ViewUtils.removeViewFormParent(surfaceView)
                this.itemVideoSf.removeAllViews()
                this.itemVideoSf.addView(surfaceView)

            } else {
                surfaceView = liveRoomSeatBean.surfaceView
                if (surfaceView == null || (liveRoomSeatBean.surfaceView as TextureView?)?.isAvailable == false) {
                    surfaceView = TextureView(context)
                    ViewUtils.removeViewFormParent(surfaceView)
                    this.itemVideoSf.removeAllViews()
                    this.itemVideoSf.addView(surfaceView)
                }else{
                    if(this.itemVideoSf.childCount<=0){
                        ViewUtils.removeViewFormParent(surfaceView)
                        this.itemVideoSf.addView(surfaceView)
                    }
                }
            }

            //ViewUtils.setViewHeight(surfaceView,CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_240))
            addVideoSf(surfaceView, dto, position)
            itemVideoSf.tag = surfaceView
        } else {
            this.isSelf = false

            val liveRoomSeatBean = SongLiveRoomFrg.surfaceViewList[position]
            liveRoomSeatBean?.apply {
                this.uid = 0
                SongLiveRoomFrg.surfaceViewList[position] = this
            }
            this.itemVideoSf.removeAllViews()
        }
    }

    private fun addVideoSf(surfaceView: View, dto: RoomSeatInfo, position: Int) {
        SongLiveRoomFrg.surfaceViewList[position] =
            LiveRoomSeatBean(dto.roomUserSeatInfo!!.userId.toInt(), surfaceView)
        AgoraManager.getInstance().setupVideo(
            dto.roomUserSeatInfo!!.userId.toInt(),
            dto.roomUserSeatInfo!!.userId == AppCacheManager.userId,
            surfaceView
        )
        if (dto.roomUserSeatInfo!!.userId == AppCacheManager.userId) {
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

    private fun AdapterSevenRoomUserSeatItemBinding.setEmptySeatInfo(
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

    init {
        initView(context)
    }
}