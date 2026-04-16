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
import cn.yanhu.agora.bean.LiveRoomSeatBean
import cn.yanhu.agora.databinding.AdapterLiveRoomUserSeatItemBinding
import cn.yanhu.agora.databinding.AdapterPkRoomBlueSeatItemBinding
import cn.yanhu.agora.databinding.ViewRoomPkBlueSeatBinding
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.ui.liveRoom.TextureViewPool
import cn.yanhu.agora.ui.liveRoom.live.MoreSeatLiveRoomFrg.Companion.surfaceViewList
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.SeatUserInfo

/**
 * @author: zhengjun
 * created: 2025/1/10
 * desc:
 */
@SuppressLint("ViewConstructor")
open class ThreeRoomPkSeatView(
    context: Context,
    var currentRoomId: String,
) :
    LinearLayout(context) {
    init {
        initView(context)
    }

    private var localUserId = AppCacheManager.userId

    private lateinit var mBinding: ViewRoomPkBlueSeatBinding
    private fun initView(context: Context) {
        mBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.view_room_pk_blue_seat, this, true
        )
        mBinding.seat1.ivRoomVoice.setOnSingleClickListener {
            onClickSeatListener?.onChildClickListener(mBinding.seat1.ivRoomVoice,0, seatInfoList[0])
        }
    }

     var seatInfoList: MutableList<RoomSeatInfo> = mutableListOf()
    fun setSeatList(seatList: MutableList<RoomSeatInfo>, isReload: Boolean = false) {
        logcom("addSurfaceView", "setSeatList")
        this.seatInfoList = seatList
        bindSeatInfo(isReload)
        setSeatStyle()
    }

    private fun bindSeatInfo(
        isReload: Boolean
    ) {
        for (i in 0 until seatInfoList.size) {
            val seatInfo = seatInfoList[i]
            val seatBinding = getSeatBinding(i)
            seatBinding?.apply {
                if (i==0){
                    this.isOwner = true
                }
                bindSeatInfo(i, seatInfo, isReload)
            }
        }
    }

    private fun setSeatStyle() {
        if (seatInfoList.isEmpty() || seatInfoList.size<3){
            return
        }
        if (seatInfoList[1].roomUserSeatInfo == null && seatInfoList[2].roomUserSeatInfo == null) {
            mBinding.vg2.visibility = View.GONE
        } else {
            mBinding.vg2.visibility = View.VISIBLE
        }
    }

    fun bindSeatByPosition(
        i: Int, seatInfo: RoomSeatInfo, isReload: Boolean = false
    ) {
        seatInfoList[i] = seatInfo
        val seatBinding = getSeatBinding(i)
        seatBinding?.apply {
            bindSeatInfo(i, seatInfo, isReload)
        }
        setSeatStyle()
    }

    private fun AdapterPkRoomBlueSeatItemBinding.bindSeatInfo(
        i: Int,
        seatInfo: RoomSeatInfo,
        isReload: Boolean = false
    ) {
        seatInfoList[i] = seatInfo
        val tag = this.vgParent.tag as RoomSeatInfo?
        val tagUserInfo = tag?.roomUserSeatInfo
        if (tag == null || tagUserInfo?.userId != seatInfo.roomUserSeatInfo?.userId || seatInfo.roomUserSeatInfo == null || this.itemVideoSf.childCount <= 0) {
            this.vgParent.tag = seatInfo
            bindItemInfo(seatInfo)
        } else {
            if (tagUserInfo?.roseNum != seatInfo.roomUserSeatInfo?.roseNum || tag.mikeUser != seatInfo.mikeUser) {
                this.seatInfo = seatInfo
            }
            if (isReload) {
                seatInfo.roomUserSeatInfo?.apply {
                    val liveRoomSeatBean = surfaceViewList[seatInfo.id - 1]
                    val surfaceView =
                        getOrCreateSurfaceView(liveRoomSeatBean,this)
                    AgoraManager.getInstance().setPkSeatVideo(
                        this.userId.toInt(), surfaceView,currentRoomId
                    )
                }
            }
        }
    }

    private fun AdapterPkRoomBlueSeatItemBinding.bindItemInfo(
        item: RoomSeatInfo?
    ) {
        item?.apply {
            seatInfo = item
            executePendingBindings()
            upDataSeatVideo(item)
        }
    }

    private fun getSeatBinding(i: Int): AdapterPkRoomBlueSeatItemBinding? {
        when (i) {
            0 -> {
                return mBinding.seat1
            }

            1 -> {
                return mBinding.seat2
            }

            2 -> {
                return mBinding.seat3
            }

            else -> {
                return null
            }
        }
    }

    private fun AdapterPkRoomBlueSeatItemBinding.upDataSeatVideo(
        dto: RoomSeatInfo
    ) {
        val seatInfo = dto.roomUserSeatInfo

        if (seatInfo != null) {
            this.vgParent.visibility = View.VISIBLE


            val liveRoomSeatBean = surfaceViewList[dto.id - 1]

            val surfaceView =
                getOrCreateSurfaceView(liveRoomSeatBean, seatInfo)
            //处理SurfaceView的添加
            addVideoSf(surfaceView, dto)
            itemVideoSf.tag = surfaceView
        } else {
            handleEmptySeat()
        }
    }

    /**
     * 获取或创建SurfaceView
     */
    private fun AdapterPkRoomBlueSeatItemBinding.getOrCreateSurfaceView(
        liveRoomSeatBean: LiveRoomSeatBean?, seatUserInfo: SeatUserInfo
    ): View {
        val cacheSurfaceView = liveRoomSeatBean?.surfaceView
        if (cacheSurfaceView != null
        ) {
            if (seatUserInfo.userId == liveRoomSeatBean.uid.toString()) {
                logcom("addSurfaceView", "使用缓存中的SurfaceView")
                return cacheSurfaceView
            } else {
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
        logcom("addSurfaceView", "新建SurfaceView")
        return surfaceView
    }


    /**
     * 处理空座位情况
     */
    private fun AdapterPkRoomBlueSeatItemBinding.handleEmptySeat() {
        this.itemVideoSf.removeAllViews()
        this.vgParent.visibility = View.INVISIBLE
    }

    private fun AdapterPkRoomBlueSeatItemBinding.addVideoSf(
        surfaceView: View,
        dto: RoomSeatInfo,
    ) {

        val userId = dto.roomUserSeatInfo!!.userId

        // 先移除父视图
        ViewUtils.removeViewFormParent(surfaceView)
        itemVideoSf.removeAllViews()
        itemVideoSf.addView(surfaceView)

        surfaceViewList[dto.id - 1] =
            LiveRoomSeatBean(userId.toInt(), surfaceView)

        if (!AgoraManager.getInstance().subScribeUserList.containsKey(userId.toInt())) {
            AgoraManager.getInstance().setPkSeatVideo(
                userId.toInt(), surfaceView,currentRoomId
            )
        }
        if (userId == localUserId) {
            AgoraManager.getInstance().muteLocalAudioStream(!dto.mikeUser)
        }
    }


    private var onClickSeatListener: OnClickSeatListener? = null
    fun setOnClickSeatListener(onClickSeatListener: OnClickSeatListener) {
        this.onClickSeatListener = onClickSeatListener
    }

}