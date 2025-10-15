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
import cn.yanhu.agora.ui.liveRoom.live.MoreSeatLiveRoomFrg
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.manager.AppCacheManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import java.util.Collections

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
        i: Int, seatInfo: RoomSeatInfo
    ) {
        val seatBinding = getSeatScaleBinding(i)
        seatBinding?.apply {
            bindItemInfo(seatInfo, i)
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


    private fun getSeatScaleBinding(i: Int): AdapterSevenRoomUserSeatItemBinding? {
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

    private fun getSeatBinding(i: Int): AdapterSevenRoomUserSeatItemBinding? {
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
        i: Int, seatInfo: RoomSeatInfo
    ) {
        seatInfoList[i] = seatInfo
        val seatBinding = getSeatBinding(i)
        seatBinding?.apply {
            bindItemInfo(seatInfo, i)
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
        ivSendRose.setOnSingleClickListener {
            onClickSeatListener?.onChildClickListener(ivSendRose, position, item)
        }
        tvSeatIndex.text = (item!!.id - 1).toString()
        this.currentRoomType = roomType
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

            val liveRoomSeatBean: LiveRoomSeatBean? = MoreSeatLiveRoomFrg.surfaceViewList[position]
            var surfaceView: View?
            if (liveRoomSeatBean == null || liveRoomSeatBean.surfaceView == null || (liveRoomSeatBean.surfaceView as TextureView?)?.isAvailable == false) {
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
                } else {
                    if (this.itemVideoSf.childCount <= 0) {
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

            val liveRoomSeatBean = MoreSeatLiveRoomFrg.surfaceViewList[position]
            liveRoomSeatBean?.apply {
                this.uid = 0
                MoreSeatLiveRoomFrg.surfaceViewList[position] = this
            }
            this.itemVideoSf.removeAllViews()
        }
    }

    private fun addVideoSf(surfaceView: View, dto: RoomSeatInfo, position: Int) {
        val userId = dto.roomUserSeatInfo!!.userId
        if (AppCacheManager.userId == userId) {
            if (isScaleStyle) {
                if (dto.isExpand) {
                    AgoraManager.getInstance().setVideoEncoderConfiguration(720, 720)
                } else {
                    AgoraManager.getInstance().setVideoEncoderConfiguration(200, 200)
                }
            } else {
                AgoraManager.getInstance().setVideoEncoderConfiguration(320, 390)
            }
        }
        MoreSeatLiveRoomFrg.surfaceViewList[position] =
            LiveRoomSeatBean(userId.toInt(), surfaceView)
        AgoraManager.getInstance().setupVideo(
            userId.toInt(),
            userId == AppCacheManager.userId,
            surfaceView
        )
        if (userId == AppCacheManager.userId) {
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