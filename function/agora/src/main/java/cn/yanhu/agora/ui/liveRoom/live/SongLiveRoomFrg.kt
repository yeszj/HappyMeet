package cn.yanhu.agora.ui.liveRoom.live

import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import androidx.databinding.DataBindingUtil
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.liveRoom.LiveRoomRoseRankAdapter
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.AngleRankInfo
import cn.yanhu.agora.bean.LiveRoomSeatBean
import cn.yanhu.agora.bean.RoomOnlineResponse
import cn.yanhu.agora.bean.SongListResponse
import cn.yanhu.agora.bean.UserReceiveRoseInfo
import cn.yanhu.agora.databinding.ViewNineRoomRankViewBinding
import cn.yanhu.agora.databinding.ViewSevenRoomRankViewBinding
import cn.yanhu.agora.pop.CrownedUserListPop
import cn.yanhu.agora.pop.LiveRoomUserRoseRankPop
import cn.yanhu.agora.pop.RoomAngleRankPop
import cn.yanhu.agora.pop.song.ChooseSongPop
import cn.yanhu.agora.pop.song.SongListPop
import cn.yanhu.agora.ui.liveRoom.view.NineSongRoomSeatView
import cn.yanhu.agora.ui.liveRoom.view.OnClickSeatListener
import cn.yanhu.agora.ui.liveRoom.view.SevenSongRoomSeatView
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.bean.SeatUserInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.router.PageIntentUtil
import cn.yanhu.imchat.manager.EmMsgManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ThreadUtils
import com.hyphenate.chat.EMMessage
import com.opensource.svgaplayer.SVGACache
import java.util.Collections

/**
 * @author: zhengjun
 * created: 2025/1/15
 * desc:
 */
open class SongLiveRoomFrg : BaseLiveRoomFrg() {
    private val rankAdapter by lazy { LiveRoomRoseRankAdapter() }
    private var sevenSongRoomSeatView: SevenSongRoomSeatView? = null
    private var sevenSongRoomScaleView: SevenSongRoomSeatView? = null


    private var nineSongRoomSeatView: NineSongRoomSeatView? = null
    private var nineSongRoomScaleView: NineSongRoomSeatView? = null
    override fun initData() {
        super.initData()
        for (i in 0 until seatList.size) {
            val textureView = TextureView(mContext)
            surfaceViewList[i] = LiveRoomSeatBean(
                0,
                textureView)
        }
        if (hasExpand) {
            initSongScaleView()
        } else {
            initSongView()
        }
        if (isSevenSong()) {
            addRankView()
        } else {
            addNineRankView()
        }
        getRoseRankList()
    }


    override fun initListener() {
        super.initListener()
        mBinding.vgSong.setOnSingleClickListener {
            showChooseSongPop(roomSourceBean.ownerInfo!!.userId)
        }
    }

    private var chooseSongPop: ChooseSongPop? = null
    private fun showChooseSongPop(selectUserId: String) {
        request({ agoraRxApi.getSongGiftInfo(roomId) }, object : OnRequestResultListener<GiftInfo> {
            override fun onSuccess(data: BaseBean<GiftInfo>) {
                if (CommonUtils.isPopShow(chooseSongPop)) {
                    return
                }
                val giftInfo = data.data
                chooseSongPop = ChooseSongPop.showDialog(
                    mContext,
                    seatList,
                    roomSourceBean.ownerInfo!!.userId,
                    selectUserId, data.data, object : ChooseSongPop.OnClickSongListener {
                        override fun onClickSong(seatUserInfo: SeatUserInfo) {
                            clickSong(seatUserInfo, giftInfo)
                        }

                        override fun onShowSongList() {
                            showSongListPop()
                        }
                    }
                )
            }

        })

    }

    private var songListPop: SongListPop? = null
    private fun showSongListPop() {
        mViewModel.getSongList(roomId, object : OnRequestResultListener<SongListResponse> {
            override fun onSuccess(data: BaseBean<SongListResponse>) {
                if (CommonUtils.isPopShow(songListPop)) {
                    return
                }
                songListPop = SongListPop.showDialog(mContext, data.data!!)
            }
        })
    }

    private fun clickSong(seatUserInfo: SeatUserInfo, giftInfo: GiftInfo?) {
        mViewModel.clickSong(
            roomId,
            seatUserInfo.userId,
            giftInfo?.id.toString(),
            object : OnRequestResultListener<String> {
                override fun onSuccess(data: BaseBean<String>) {
                    chooseSongPop?.dismiss()
                    sendGiftSuccess(giftInfo!!, seatUserInfo)
                }
            })
    }


    private lateinit var nineRankViewBinding: ViewNineRoomRankViewBinding
    private fun addNineRankView() {
        val rankView =
            LayoutInflater.from(mContext).inflate(R.layout.view_nine_room_rank_view, null)
        nineRankViewBinding = DataBindingUtil.bind(rankView)!!
        nineRankViewBinding.apply {
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
            PageIntentUtil.url2Page(mContext, WebUrlManager.SONG_ROOM_RULE)
        }
        ivCrowned.setOnSingleClickListener {
            showCrownedListPop(CrownedUserListPop.TYPE_SONG)
        }
        vgGiftAudio.setOnSingleClickListener {
            if (AppCacheManager.isOpenGiftAudio) {
                ivAudio.setImageResource(R.drawable.svg_voice_off)
                AppCacheManager.isOpenGiftAudio = false
            } else {
                ivAudio.setImageResource(R.drawable.svg_voice_on)
                AppCacheManager.isOpenGiftAudio = true
            }
            SVGACache.clearCache()
        }
        mBinding.flTopView.addView(rankView)
    }

    private fun initSongView() {
        if (isSevenSong()) {
            sevenSongRoomSeatView = SevenSongRoomSeatView(mContext, false)
            mBinding.seatContainer.removeAllViews()
            mBinding.seatContainer.addView(sevenSongRoomSeatView)
            sevenSongRoomSeatView?.setRoomId(roomId, isOwner)
            sevenSongRoomSeatView?.setSeatList(seatList)
            sevenSongRoomSeatView?.setOnClickSeatListener(onClickSeatListener)
        } else {
            nineSongRoomSeatView = NineSongRoomSeatView(mContext, false)
            mBinding.seatContainer.removeAllViews()
            mBinding.seatContainer.addView(nineSongRoomSeatView)
            nineSongRoomSeatView?.setRoomId(roomId, isOwner)
            nineSongRoomSeatView?.setSeatList(seatList)
            nineSongRoomSeatView?.setOnClickSeatListener(onClickSeatListener)
        }
    }

    private fun initSongScaleView() {
        if (isSevenSong()) {
            sevenSongRoomScaleView = SevenSongRoomSeatView(mContext, true)
            mBinding.seatContainer.removeAllViews()
            mBinding.seatContainer.addView(sevenSongRoomScaleView)
            sevenSongRoomScaleView?.setRoomId(roomId, isOwner)
            sevenSongRoomScaleView?.setSeatList(seatList)
            sevenSongRoomScaleView?.setOnClickSeatListener(onClickSeatListener)
        } else {
            nineSongRoomScaleView = NineSongRoomSeatView(mContext, true)
            mBinding.seatContainer.removeAllViews()
            mBinding.seatContainer.addView(nineSongRoomScaleView)
            nineSongRoomScaleView?.setRoomId(roomId, isOwner)
            nineSongRoomScaleView?.setSeatList(seatList)
            nineSongRoomScaleView?.setOnClickSeatListener(onClickSeatListener)
        }


    }

    private fun isSevenSong(): Boolean {
        return roomType == RoomListBean.TYPE_SEVEN_SONG
    }

    override fun getRoomSeatSuccess(seatList: MutableList<RoomSeatInfo>) {
        if (isSevenSong()) {
            if (hasExpand) {
                sevenSongRoomScaleView?.setSeatList(seatList)
            } else {
                sevenSongRoomSeatView?.setSeatList(seatList)
            }
        } else {
            if (hasExpand) {
                nineSongRoomScaleView?.setSeatList(seatList)
            } else {
                nineSongRoomSeatView?.setSeatList(seatList)
            }
        }

    }

    override fun refreshSeatInfo(it: MutableList<RoomSeatInfo>, uid: Int) {
        ThreadUtils.getMainHandler().post {
            for (i in 0 until it.size) {
                val seatInfo = it[i]
                if (seatInfo.roomUserSeatInfo?.userId?.toInt() == uid) {
                    if (isSevenSong()) {
                        if (hasExpand) {
                            sevenSongRoomScaleView?.bindScaleByPosition(i, seatInfo)
                        } else {
                            sevenSongRoomSeatView?.bindSeatByPosition(i, seatInfo)
                        }
                    } else {
                        if (hasExpand) {
                            nineSongRoomScaleView?.bindScaleByPosition(i, seatInfo)
                        } else {
                            nineSongRoomSeatView?.bindSeatByPosition(i, seatInfo)
                        }
                    }

                    return@post
                }
            }
        }
    }

    private val onClickSeatListener = object : OnClickSeatListener {
        override fun onChildClickListener(view: View, position: Int, item: RoomSeatInfo?) {
            if (item == null) {
                return
            }
            when (view.id) {
                R.id.iv_chooseSong -> {
                    item.roomUserSeatInfo?.apply {
                        showChooseSongPop(this.userId)
                    }
                }

                R.id.iv_voiceStatus -> {
                    //开关麦
                    val roomUserSeatInfo = item.roomUserSeatInfo ?: return
                    if (localUserId.toString() == roomUserSeatInfo.userId) {
                        switchMikeAlert(!item.mikeUser, item.id)
                    } else if (isOwner) {
                        ownerSwitchMikeAlert(!item.mikeUser, item.id, roomUserSeatInfo.userId)
                    }
                }

                R.id.item_video_sf -> {
                    val roomUserSeatInfo = item.roomUserSeatInfo
                    if (roomUserSeatInfo != null) {
                        if (roomUserSeatInfo.userId == AppCacheManager.userId) {
                            showUserPop(roomUserSeatInfo.userId)
                        } else {
                            showSendGiftPop(roomUserSeatInfo)
                        }
                    }
                }

                R.id.vg_emptySeat -> {
                    val roomUserSeatInfo = item.roomUserSeatInfo
                    if (roomUserSeatInfo == null) {
                        if (isOwner) {
                            //邀请上麦弹框
                            showSeatUserList()
                        } else {
                            //上麦
                            val seatNum = item.id.toString()
                            userSetSeat(
                                if (roomSourceBean.autoSeat) SEAT_TYPE_AUTO else SEAT_TYPE_APPLY,
                                seatNum
                            )
                        }
                    }
                }

                R.id.iv_expand -> {
                    expandSeat(position, item)
                    EmMsgManager.sendCmdMessageToChatRoom(
                        roomSourceBean.uid,
                        position.toString(),
                        ChatConstant.ACTION_EXPAND_SEAT_ITEM
                    )
                }
            }
        }
    }

    override fun userLeaveChanged(uid: Int) {
        getOnlineUser()
        ThreadUtils.getMainHandler().post {
            for (i in 0 until seatList.size) {
                val item = seatList[i]
                if (item.roomUserSeatInfo?.userId?.toInt() == uid) {
                    if (roomSourceBean.ownerInfo?.userId != uid.toString()) {
                        item.roomUserSeatInfo = null
                    }

                    if (isSevenSong()) {
                        if (hasExpand) {
                            sevenSongRoomScaleView?.bindScaleByPosition(i, item)
                        } else {
                            sevenSongRoomSeatView?.bindSeatByPosition(i, item)
                        }
                    } else {
                        if (hasExpand) {
                            nineSongRoomScaleView?.bindScaleByPosition(i, item)
                        } else {
                            nineSongRoomSeatView?.bindSeatByPosition(i, item)
                        }
                    }

                    if (item.isExpand) {
                        expandSeat(i, item)
                    }
                }
            }
        }
    }

    private fun expandSeat(position: Int, item: RoomSeatInfo) {
        if (isOwner) {
            request({ agoraRxApi.setExpand(roomId, item.id) },
                object : OnRequestResultListener<String> {
                    override fun onSuccess(data: BaseBean<String>) {
                        startExpandItem(item, position)
                    }
                })
        } else {
            startExpandItem(item, position)
        }
    }

    private fun getExpandPosition(): Int {
        return if (roomType == RoomListBean.TYPE_SEVEN_SONG) {
            1
        } else {
            4
        }
    }

    private var scalePosition: Int = -1
    private fun startExpandItem(
        item: RoomSeatInfo,
        position: Int
    ) {
        val expandPosition = getExpandPosition()
        if (item.isExpand) {
            scalePosition = -1
            seatList[position].isExpand = false
            if (position != item.id - 1) {
                Collections.swap(seatList, position, item.id - 1)
            }
            showSeatView()
        } else {
            hasExpand = true
            val expandItem = seatList[expandPosition]
            var originPosition = -1
            if (expandItem.isExpand) {
                seatList[position].isExpand = true
                originPosition = expandItem.id - 1
                expandItem.isExpand = false
                if (expandPosition != expandItem.id - 1) {
                    Collections.swap(seatList, expandPosition, originPosition)
                }
            }else{
                seatList[position].isExpand = true
            }

            var currentExpandItem: RoomSeatInfo? = null
            if (originPosition != -1) {
                currentExpandItem = seatList[expandPosition]
            }
            if (position != expandPosition && currentExpandItem?.id != item.id) {
                Collections.swap(seatList, position, expandPosition)
            }
            showScaleSeatView()
            scalePosition = position
        }
        changeRvChatScroll()
    }

    private fun showSeatView() {
        hasExpand = false
        clearSurfaceView()
        if (isSevenSong()) {
            if (sevenSongRoomSeatView == null) {
                initSongView()
            } else {
                sevenSongRoomSeatView?.setSeatList(seatList)
                ViewUtils.removeViewFormParent(sevenSongRoomSeatView)
                mBinding.seatContainer.removeAllViews()
                mBinding.seatContainer.addView(sevenSongRoomSeatView)
            }
        } else {
            if (nineSongRoomSeatView == null) {
                initSongView()
            } else {
                nineSongRoomSeatView?.setSeatList(seatList)
                ViewUtils.removeViewFormParent(nineSongRoomSeatView)
                mBinding.seatContainer.removeAllViews()
                mBinding.seatContainer.addView(nineSongRoomSeatView)
            }
        }


    }

    override fun updateSeatRoseInfo() {
        for (i in 0 until seatList.size) {
            val seatInfo = seatList[i]
            val roomUserSeatInfo = seatInfo.roomUserSeatInfo
            if (roomUserSeatInfo != null) {
                if (isSevenSong()) {
                    if (hasExpand) {
                        sevenSongRoomScaleView?.bindScaleRoseInfo(i, seatInfo)
                    } else {
                        sevenSongRoomSeatView?.bindRoseInfo(i, seatInfo)
                    }
                } else {
                    if (hasExpand) {
                        nineSongRoomScaleView?.bindScaleRoseInfo(i, seatInfo)
                    } else {
                        nineSongRoomSeatView?.bindRoseInfo(i, seatInfo)
                    }
                }
            }
        }
    }

    override fun onReceiveCmdMsg(it: EMMessage) {
        val source = it.getIntAttribute("source", -1)
        if (source == ChatConstant.ACTION_EXPAND_SEAT_ITEM) {
            val position = it.getStringAttribute(ChatConstant.CUSTOM_DATA).toInt()
            val get = seatList[position]
            startExpandItem(get, position)
        }
    }

    override fun addHostSurfaceView() {
        if (isSevenSong()) {
            if (hasExpand) {
                sevenSongRoomScaleView?.setSeatList(seatList)
            } else {
                sevenSongRoomSeatView?.setSeatList(seatList)
            }
        } else {
            if (hasExpand) {
                nineSongRoomScaleView?.setSeatList(seatList)
            } else {
                nineSongRoomSeatView?.setSeatList(seatList)
            }
        }

    }

    private fun clearSurfaceView(){
        for (i in 0 until surfaceViewList.size){
            val liveRoomSeatBean = surfaceViewList[i]
            liveRoomSeatBean?.apply {
                this.uid = 0
                surfaceViewList[i] = this
                ViewUtils.removeViewFormParent(this.surfaceView)
            }
        }
    }

    private fun showScaleSeatView() {
        if (isSevenSong()) {
            if (sevenSongRoomScaleView == null) {
                clearSurfaceView()
                initSongScaleView()
            } else {
                if (scalePosition == -1) {
                    clearSurfaceView()
                    mBinding.seatContainer.removeAllViews()
                    ViewUtils.removeViewFormParent(sevenSongRoomScaleView)
                    mBinding.seatContainer.addView(sevenSongRoomScaleView)
                }
                sevenSongRoomScaleView?.setSeatList(seatList)
            }
        } else {
            if (nineSongRoomScaleView == null) {
                clearSurfaceView()
                initSongScaleView()
            } else {
                if (scalePosition == -1) {
                    clearSurfaceView()
                    mBinding.seatContainer.removeAllViews()
                    ViewUtils.removeViewFormParent(nineSongRoomScaleView)
                    mBinding.seatContainer.addView(nineSongRoomScaleView)
                }
                nineSongRoomScaleView?.setSeatList(seatList)
            }
        }

    }

    override fun refreshAutoSeat() {
        if (isSevenSong()) {
            rankViewBinding.roomInfo = roomSourceBean
        } else {
            nineRankViewBinding.roomInfo = roomSourceBean
        }
    }

    private lateinit var rankViewBinding: ViewSevenRoomRankViewBinding
    private fun addRankView() {
        val rankView =
            LayoutInflater.from(mContext).inflate(R.layout.view_seven_room_rank_view, null)
        rankViewBinding = DataBindingUtil.bind(rankView)!!
        rankViewBinding.apply {
            bindSevenRankView(rankView)
        }

    }

    private fun ViewSevenRoomRankViewBinding.bindSevenRankView(rankView: View?) {
        roomInfo = roomSourceBean
        this.isRoomOwner = isOwner
        toggleAutoSeat.setOnSingleClickListener {
            showSetAutoSeat()
        }
        isAngle = roomType == RoomListBean.TYPE_SEVEN_ANGLE
        isSong = roomType == RoomListBean.TYPE_SEVEN_SONG
        rvRank.adapter = rankAdapter
        if (AppCacheManager.isOpenGiftAudio) {
            ivAudio.setImageResource(R.drawable.svg_voice_on)
        } else {
            ivAudio.setImageResource(R.drawable.svg_voice_off)
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
            PageIntentUtil.url2Page(mContext, WebUrlManager.SONG_ROOM_RULE)
        }
        ivCrowned.setOnSingleClickListener {
            showCrownedListPop(CrownedUserListPop.TYPE_SONG)
        }
        vgGiftAudio.setOnSingleClickListener {
            if (AppCacheManager.isOpenGiftAudio) {
                ivAudio.setImageResource(R.drawable.svg_voice_off)
                AppCacheManager.isOpenGiftAudio = false
            } else {
                ivAudio.setImageResource(R.drawable.svg_voice_on)
                AppCacheManager.isOpenGiftAudio = true
            }
            SVGACache.clearCache()
        }
        mBinding.flCustomView.addView(rankView)
    }

    override fun refreshOnlineUser(onlineResponse: RoomOnlineResponse) {
        if (isSevenSong()) {
            rankViewBinding.tvOnlineNum.text = onlineResponse.onlineNum.toString()
        } else {
            nineRankViewBinding.tvOnlineNum.text = onlineResponse.onlineNum.toString()
        }
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
                roomAngleRankPop =
                    RoomAngleRankPop.showDialog(mContext, rankList, RoomAngleRankPop.TYPE_SONG)
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

    override fun refreshSeatMicStatus(seatPosition: Int, mickUser: Boolean) {
        seatList[seatPosition].mikeUser = mickUser
        if (isSevenSong()) {
            if (hasExpand) {
                sevenSongRoomScaleView?.setSeatList(seatList)
            } else {
                sevenSongRoomSeatView?.setSeatList(seatList)
            }
        } else {
            if (hasExpand) {
                nineSongRoomScaleView?.setSeatList(seatList)
            } else {
                nineSongRoomSeatView?.setSeatList(seatList)
            }
        }
    }

    companion object {
        var surfaceViewList = mutableMapOf<Int, LiveRoomSeatBean>()
        fun getOwnerSurfaceView(owenId: String): View? {
            var surfaceView: View? = null
            surfaceViewList.forEach { (t, u) ->
                if (u.uid.toString() == owenId) {
                    surfaceView = u.surfaceView
                    return@forEach
                }
            }
            return surfaceView
        }
    }
}