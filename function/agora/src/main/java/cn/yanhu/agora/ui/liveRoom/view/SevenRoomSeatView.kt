package cn.yanhu.agora.ui.liveRoom.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import cn.yanhu.agora.databinding.ViewSevenSongRoomSeatBinding
import cn.yanhu.agora.R
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.LiveRoomSeatBean
import cn.yanhu.agora.bean.UserReceiveRoseInfo
import cn.yanhu.agora.databinding.AdapterLiveRoomUserSeatItemBinding
import cn.yanhu.agora.databinding.ViewNineSongRoomSeatBinding
import cn.yanhu.agora.databinding.ViewNineSongScaleRoomSeatBinding
import cn.yanhu.agora.databinding.ViewSevenSongRoomScaleSeatBinding
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.pop.LiveRoomUserRoseRankPop
import cn.yanhu.agora.ui.liveRoom.live.MoreSeatLiveRoomFrg
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.manager.AppCacheManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import androidx.core.view.isEmpty

/**
 * @author: zhengjun
 * created: 2025/1/10
 * desc:
 */
@SuppressLint("ViewConstructor")
open class SevenRoomSeatView(
    context: Context,
    private var isScaleStyle: Boolean,
    var roomType: Int,
    var currentRoomId: String,
    var isRoomOwner: Boolean
) :
    LinearLayout(context) {
    init {
        initView(context)
    }

    private lateinit var mBinding: ViewDataBinding
    private fun initView(context: Context) {
        mBinding = if (isScaleStyle) {
            DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.view_seven_song_room_scale_seat, this, true
            )
        } else {
            DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.view_seven_song_room_seat, this, true
            )
        }

    }

    private var seatInfoList: MutableList<RoomSeatInfo> = mutableListOf()
    fun setSeatList(seatList: MutableList<RoomSeatInfo>) {
        this.seatInfoList = seatList
        if (mBinding is ViewSevenSongRoomSeatBinding) {
            bindSongSeat()
        } else {
            bindSongScaleSeat()
        }
    }


    private fun bindSongScaleSeat(
    ) {
        for (i in 0 until seatInfoList.size) {
            val seatInfo = seatInfoList[i]
            bindScaleByPosition(i, seatInfo)
        }
    }

    private fun getSeatScaleBinding(i: Int): AdapterLiveRoomUserSeatItemBinding? {
        val songBinding = mBinding as ViewSevenSongRoomScaleSeatBinding
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

            else -> {
                return null
            }
        }
    }

    private fun getSeatBinding(i: Int): AdapterLiveRoomUserSeatItemBinding? {
        val songBinding = mBinding as ViewSevenSongRoomSeatBinding
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


            else -> {
                return null
            }
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
        val seatBinding = getSeatScaleBinding(i)
        seatBinding?.apply {
            this.seatInfo = seatInfo
        }
    }

    fun bindScaleByPosition(
        i: Int, seatInfo: RoomSeatInfo
    ) {
        val seatScaleBinding = getSeatScaleBinding(i)
        seatScaleBinding?.apply {
            bindItemInfo(seatInfo, i)
        }
    }

    private fun bindSongSeat(
    ) {
        for (i in 0 until seatInfoList.size) {
            val seatInfo = seatInfoList[i]
            bindSeatByPosition(i, seatInfo)
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


    private fun AdapterLiveRoomUserSeatItemBinding.bindItemInfo(
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
            tvOwner.visibility = VISIBLE
        } else {
            tvOwner.visibility = INVISIBLE
        }
        if (isScaleStyle) {
            if (position == 1) {
                ViewUtils.setViewHeight(
                    vgParent, CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_240)
                )
            } else {
                ViewUtils.setViewHeight(
                    vgParent, CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_120)
                )
            }

        } else {
            ViewUtils.setViewHeight(
                vgParent, CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_142)
            )
        }

        val isShowNoTopBg = position == 2 || position == 5
        if (item.id == 1) {
            vgParent.setBackgroundResource(R.drawable.bg_seat_no_stroke)
        } else {
            if (isShowNoTopBg) {
                vgParent.setBackgroundResource(R.drawable.bg_seat_no_top_stroke)
            } else {
                vgParent.setBackgroundResource(R.drawable.bg_seat_bottom_stroke)
            }
        }

        upDataSeats(item, position)
        item.apply {
            seatInfo = item
            if (item.roomUserSeatInfo == null) {
                setEmptySeatInfo()
            }
        }
        viewRank.setOnSingleClickListener {
            showUserReceiveRoseDetailPop(item)
        }
    }

    private fun AdapterLiveRoomUserSeatItemBinding.upDataSeats(dto: RoomSeatInfo, position: Int) {
        val seatInfo = dto.roomUserSeatInfo

        if (seatInfo != null) {
            this.isSelf = seatInfo.userId == AppCacheManager.userId

            val liveRoomSeatBean = MoreSeatLiveRoomFrg.surfaceViewList[position]
            val surfaceView =
                getOrCreateSurfaceView(liveRoomSeatBean, seatInfo.userId.toInt())

            //处理SurfaceView的添加
            setupSurfaceView(surfaceView)
            addVideoSf(surfaceView, dto, position)
            itemVideoSf.tag = surfaceView
        } else {
            handleEmptySeat(position)
        }
    }

    /**
     * 获取或创建SurfaceView
     */
    private fun AdapterLiveRoomUserSeatItemBinding.getOrCreateSurfaceView(
        liveRoomSeatBean: LiveRoomSeatBean?,
        userId: Int
    ): View {
        // 情况1: 没有SurfaceView或不可用
        if (liveRoomSeatBean?.surfaceView == null ||
            (liveRoomSeatBean.surfaceView as? TextureView)?.isAvailable == false
        ) {
            return TextureView(context)
        }

        // 情况2: 用户ID不匹配
        if (userId != liveRoomSeatBean.uid) {
            return liveRoomSeatBean.surfaceView?.takeIf {
                (it as? TextureView)?.isAvailable != false
            } ?: TextureView(context)
        }

        // 情况3: 用户ID匹配，检查可用性
        return liveRoomSeatBean.surfaceView?.takeIf {
            (it as? TextureView)?.isAvailable == true
        } ?: TextureView(context)
    }

    /**
     * 设置SurfaceView到容器中
     */
    private fun AdapterLiveRoomUserSeatItemBinding.setupSurfaceView(surfaceView: View) {
        // 如果容器中没有子视图或子视图不同，则重新添加
        if (itemVideoSf.isEmpty() || itemVideoSf.getChildAt(0) != surfaceView) {
            // 先移除父视图
            ViewUtils.removeViewFormParent(surfaceView)
            itemVideoSf.removeAllViews()
            itemVideoSf.addView(surfaceView)
        }
    }

    /**
     * 处理空座位情况
     */
    private fun AdapterLiveRoomUserSeatItemBinding.handleEmptySeat(position: Int) {
        this.isSelf = false

        // 更新座位信息
        MoreSeatLiveRoomFrg.surfaceViewList[position]?.uid = 0
        this.itemVideoSf.removeAllViews()
    }

    private fun addVideoSf(surfaceView: View, dto: RoomSeatInfo, position: Int) {

        val userId = dto.roomUserSeatInfo!!.userId
        if (AppCacheManager.userId == userId) {
            if (isScaleStyle) {
                if (dto.isExpand) {
                    AgoraManager.getInstance().setVideoEncoderConfiguration(720, 720)
                } else {
                    if (isRoomOwner) {
                        AgoraManager.getInstance().setVideoEncoderConfiguration(260, 330)
                    } else {
                        AgoraManager.getInstance().setVideoEncoderConfiguration(260, 260)
                    }
                }
            } else {
                AgoraManager.getInstance().setVideoEncoderConfiguration(330, 360)
            }

        }

        MoreSeatLiveRoomFrg.surfaceViewList[position] =
            LiveRoomSeatBean(userId.toInt(), surfaceView)
        AgoraManager.getInstance().setupVideo(
            userId.toInt(), userId == AppCacheManager.userId, surfaceView
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


}