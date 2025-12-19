package cn.yanhu.agora.ui.liveRoom.live

import android.view.LayoutInflater
import android.view.SurfaceView
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
import cn.yanhu.agora.pop.song.ChangeClickSongGiftPop
import cn.yanhu.agora.pop.song.ChooseSongPop
import cn.yanhu.agora.pop.song.ModifyInsertQueueRosePop
import cn.yanhu.agora.pop.song.SongListPop
import cn.yanhu.agora.ui.liveRoom.TextureViewPool
import cn.yanhu.agora.ui.liveRoom.view.NineRoomSeatView
import cn.yanhu.agora.ui.liveRoom.view.OnClickSeatListener
import cn.yanhu.agora.ui.liveRoom.view.SevenRoomSeatView
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.logComToFile
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.GiftInfo
import cn.yanhu.commonres.bean.OperateInfo
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.bean.RoomPkInfo
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.commonres.bean.SeatUserInfo
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.pop.CommonOperatePop
import cn.yanhu.commonres.router.PageIntentUtil
import cn.yanhu.imchat.manager.EmMsgManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.blankj.utilcode.util.ThreadUtils
import com.efs.sdk.memleaksdk.monitor.internal.be
import com.hyphenate.chat.EMMessage
import com.opensource.svgaplayer.SVGACache
import java.util.Collections

/**
 * @author: zhengjun
 * created: 2025/1/15
 * desc:7人和9人k歌房
 */
open class MoreSeatLiveRoomFrg : BaseLiveRoomFrg() {
    private val rankAdapter by lazy { LiveRoomRoseRankAdapter() }
    private var sevenSongRoomSeatView: SevenRoomSeatView? = null
    private var sevenSongRoomScaleView: SevenRoomSeatView? = null
    private var nineSongRoomSeatView: NineRoomSeatView? = null
    private var nineSongRoomScaleView: NineRoomSeatView? = null
    override fun initData() {
        roomSourceBean = requireArguments().getSerializable(IntentKeyConfig.DATA) as RoomDetailInfo
        roomType = roomSourceBean.roomType
        initRankView()
        super.initData()
        for (i in 0 until seatList.size) {
            surfaceViewList[i] = LiveRoomSeatBean(
                0, null
            )
        }
        if (hasExpand) {
            initSongScaleView(false)
        } else {
            initSongView(false)
        }
        addRankView()
        getRoseRankList()
        if (isOwner) {
            showAnimSwitch()
        }
    }

    override fun initListener() {
        super.initListener()
        mBinding.vgSong.setOnSingleClickListener {
            showChooseSongPop(roomSourceBean.ownerInfo!!.userId)
        }
        mBinding.ivSongSet.setOnSingleClickListener {
            showSongSetPop()
        }
    }

    private fun showSongSetPop(): CommonOperatePop {
        val list = mutableListOf<OperateInfo>()
        list.add(OperateInfo("更换点歌礼物", cn.yanhu.commonres.R.color.cl_common, 1))
        list.add(OperateInfo("重置插队玫瑰数", cn.yanhu.commonres.R.color.cl_common, 2))
        return CommonOperatePop.showDialog(
            mContext, list, object : CommonOperatePop.OnClickItemListener {
                override fun onClickItem(operateInfo: OperateInfo) {
                    if (operateInfo.type == 1) {
                        //更换点歌礼物
                        ChangeClickSongGiftPop.showDialog(mContext, roomId)
                    } else if (operateInfo.type == 2) {
                        //重置插队玫瑰数
                        ModifyInsertQueueRosePop.showDialog(
                            mContext, roomId, roomSourceBean.queuePrice
                        )
                    }
                }
            })
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
                    selectUserId,
                    data.data,
                    object : ChooseSongPop.OnClickSongListener {
                        override fun onClickSong(seatUserInfo: SeatUserInfo) {
                            clickSong(seatUserInfo, giftInfo)
                        }

                        override fun onShowSongList() {
                            showSongListPop()
                        }
                    })
            }

        })

    }

    private var songListPop: SongListPop? = null
    private fun showSongListPop() {
        mViewModel.getSongList(roomId, object : OnRequestResultListener<SongListResponse> {
            override fun onSuccess(data: BaseBean<SongListResponse>) {
                val response = data.data ?: return
                response.roomId = roomId
                if (CommonUtils.isPopShow(songListPop)) {
                    songListPop?.refreshData(response)
                    return
                }
                songListPop = SongListPop.showDialog(
                    mContext, response, isOwner, object : SongListPop.OnRefreshSeatListener {
                        override fun onClearSongRoseSuccess() {
                            EmMsgManager.sendCmdMessageToChatRoom(
                                roomSourceBean.uid, "", ChatConstant.REFRESH_SEAT_ROSE
                            )
                            refreshSeatRoseInfo()
                        }

                        override fun onSetSongUserSuccess() {
                            EmMsgManager.sendCmdMessageToChatRoom(
                                roomSourceBean.uid, "", ChatConstant.REFRESH_SEAT_ROSE
                            )
                            refreshSeatRoseInfo()
                            showSongListPop()
                        }
                    })
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

    override fun setHasSeatUpStatus() {
        super.setHasSeatUpStatus()
        showAnimSwitch()
    }

    override fun setSeatOutSuccess() {
        super.setSeatOutSuccess()
        if (isSevenRoom()) {
            rankViewBinding.vgEnterAnim.visibility = View.GONE
        } else {
            nineRankViewBinding.vgEnterAnim.visibility = View.GONE
        }

    }

    private fun showAnimSwitch() {
        if (isSevenRoom()) {
            rankViewBinding.vgEnterAnim.visibility = View.VISIBLE
            changeEnterAnimStatus(rankViewBinding.iconEnter)
        } else {
            nineRankViewBinding.vgEnterAnim.visibility = View.VISIBLE
            changeEnterAnimStatus(nineRankViewBinding.iconEnter)
        }
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
            if (roomSourceBean.isSongRoom()) {
                PageIntentUtil.url2Page(mContext, WebUrlManager.SONG_ROOM_RULE)
            } else {
                PageIntentUtil.url2Page(mContext, WebUrlManager.ANGLE_ROOM_RULE)
            }
        }
        ivCrowned.setOnSingleClickListener {
            if (roomSourceBean.isSongRoom()) {
                showCrownedListPop(CrownedUserListPop.TYPE_SONG)
            } else {
                showCrownedListPop(CrownedUserListPop.TYPE_ANGLE)
            }
        }
        vgGiftAudio.setOnSingleClickListener {
            changeGiftAudioStatus(ivAudio, true)
            SVGACache.clearCache()
        }
        vgEnterAnim.setOnSingleClickListener {
            changeEnterAnimStatus(iconEnter, true)
        }
    }


    private fun initSongView(loadVideo: Boolean = true) {
        if (isSevenRoom()) {
            sevenSongRoomSeatView = SevenRoomSeatView(mContext, false, roomType, roomId, isOwner,roomSourceBean.roomAdmin)
            mBinding.seatContainer.removeAllViews()
            mBinding.seatContainer.addView(sevenSongRoomSeatView)
            sevenSongRoomSeatView?.setSeatList(seatList)
            sevenSongRoomSeatView?.setOnClickSeatListener(onClickSeatListener)
        } else {
            nineSongRoomSeatView = NineRoomSeatView(mContext, false, roomType, roomId, isOwner,roomSourceBean.roomAdmin)
            mBinding.seatContainer.removeAllViews()
            mBinding.seatContainer.addView(nineSongRoomSeatView)
            nineSongRoomSeatView?.setSeatList(seatList)
            nineSongRoomSeatView?.setOnClickSeatListener(onClickSeatListener)
        }
    }

    private fun initSongScaleView(loadVideo: Boolean = true) {
        if (isSevenRoom()) {
            sevenSongRoomScaleView = SevenRoomSeatView(mContext, true, roomType, roomId, isOwner,roomSourceBean.roomAdmin)
            mBinding.seatContainer.removeAllViews()
            mBinding.seatContainer.addView(sevenSongRoomScaleView)
            sevenSongRoomScaleView?.setSeatList(seatList)
            sevenSongRoomScaleView?.setOnClickSeatListener(onClickSeatListener)
        } else {
            nineSongRoomScaleView = NineRoomSeatView(mContext, true, roomType, roomId, isOwner,roomSourceBean.roomAdmin)
            mBinding.seatContainer.removeAllViews()
            mBinding.seatContainer.addView(nineSongRoomScaleView)
            nineSongRoomScaleView?.setSeatList(seatList)
            nineSongRoomScaleView?.setOnClickSeatListener(onClickSeatListener)
        }
    }

    override fun userRoomAdminChanger(isRoomAdmin: Boolean) {
        //TODO 管理员可以放大麦位和闭麦开麦功能 此功能暂时不做 先注释
//        if (isSevenRoom()){
//            sevenSongRoomSeatView?.updateRoomAdmin(isRoomAdmin)
//            sevenSongRoomScaleView?.updateRoomAdmin(isRoomAdmin)
//        }else{
//            nineSongRoomSeatView?.updateRoomAdmin(isRoomAdmin)
//            nineSongRoomScaleView?.updateRoomAdmin(isRoomAdmin)
//        }
    }


    override fun userVideoStatusChanged(uid: Int, isShowPreload: Boolean) {
        if (isSevenRoom()) {
            if (hasExpand) {
                sevenSongRoomScaleView?.userVideoStatusChanged(uid, isShowPreload, networkType)
            } else {
                sevenSongRoomSeatView?.userVideoStatusChanged(uid, isShowPreload, networkType)
            }
        } else {
            if (hasExpand) {
                nineSongRoomScaleView?.userVideoStatusChanged(uid, isShowPreload, networkType)
            } else {
                nineSongRoomSeatView?.userVideoStatusChanged(uid, isShowPreload, networkType)
            }
        }
    }

    override fun userNetChanged(uid: String, ifNetDisConnect: Boolean) {
        if (isSevenRoom()) {
            if (hasExpand) {
                sevenSongRoomScaleView?.userNetChanged(uid, ifNetDisConnect)
            } else {
                sevenSongRoomSeatView?.userNetChanged(uid, ifNetDisConnect)
            }
        } else {
            if (hasExpand) {
                nineSongRoomScaleView?.userNetChanged(uid, ifNetDisConnect)
            } else {
                nineSongRoomSeatView?.userNetChanged(uid, ifNetDisConnect)
            }
        }
    }

    private fun isSevenRoom(): Boolean {
        return roomType == RoomListBean.TYPE_SEVEN_SONG || roomType == RoomListBean.TYPE_SEVEN_FRIEND || roomType == RoomListBean.TYPE_SEVEN_ANGLE
    }

    override fun getRoomSeatSuccess(seatList: MutableList<RoomSeatInfo>) {
        if (isSevenRoom()) {
            if (hasExpand) {
                sevenSongRoomScaleView?.setSeatList(seatList, true)
            } else {
                sevenSongRoomSeatView?.setSeatList(seatList, true)
            }
        } else {
            if (hasExpand) {
                nineSongRoomScaleView?.setSeatList(seatList, true)
            } else {
                nineSongRoomSeatView?.setSeatList(seatList, true)
            }
        }

    }


    override fun refreshSeatInfo(it: MutableList<RoomSeatInfo>, uid: Int) {
        ThreadUtils.getMainHandler().post {
            for (i in 0 until it.size) {
                val seatInfo = it[i]
                if (seatInfo.roomUserSeatInfo?.userId?.toInt() == uid) {
                    logComToFile(TAG, "更新麦位userId=$uid,seatIndex = ${seatInfo.id-1}")
                    if (isSevenRoom()) {
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
                    expandSeat(position, item,true)
                }

                R.id.iv_sendRose -> {
                    val roomUserSeatInfo = item.roomUserSeatInfo ?: return
                    checkSendGift(roomUserSeatInfo, roseGiftInfo!!)
                }
            }
        }
    }

    override fun userLeaveChanged(uid: Int) {
        ThreadUtils.getMainHandler().post {
            for (i in 0 until seatList.size) {
                val item = seatList[i]
                if (item.roomUserSeatInfo?.userId?.toInt() == uid) {
                    if (roomSourceBean.ownerInfo?.userId != uid.toString()) {
                        item.roomUserSeatInfo = null
                        logComToFile(TAG, "用户下麦userId=$uid,更新麦位，seatIndex = ${item.id-1}")
                    }
                    if (isSevenRoom()) {
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
                        expandSeat(i, item,false)
                    }
                }
            }
            getPkSeatUserList()
            //refreshSeatRoseInfo()
        }
    }

    private fun expandSeat(position: Int, item: RoomSeatInfo,isOperateScale: Boolean) {
        if (isOwner ) {
            request(
                { agoraRxApi.setExpand(roomId, item.id) },
                object : OnRequestResultListener<String> {
                    override fun onSuccess(data: BaseBean<String>) {
                        startExpandItem(item, position)
                        if (isOperateScale){
                            EmMsgManager.sendCmdMessageToChatRoom(
                                roomSourceBean.uid,
                                position.toString(),
                                ChatConstant.ACTION_EXPAND_SEAT_ITEM
                            )
                        }
                    }
                })
        } else {
            startExpandItem(item, position)
        }
    }

    private fun getExpandPosition(): Int {
        return if (roomType == RoomListBean.TYPE_SEVEN_SONG || roomType == RoomListBean.TYPE_SEVEN_ANGLE || roomType == RoomListBean.TYPE_SEVEN_FRIEND) {
            1
        } else {
            4
        }
    }

    private var scalePosition: Int = -1
    private fun startExpandItem(
        item: RoomSeatInfo, position: Int
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
            } else {
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
        if (isSevenRoom()) {
            if (sevenSongRoomSeatView == null) {
                initSongView()
            } else {
                sevenSongRoomSeatView?.setSeatList(seatList, true)
                ViewUtils.removeViewFormParent(sevenSongRoomSeatView)
                mBinding.seatContainer.removeAllViews()
                mBinding.seatContainer.addView(sevenSongRoomSeatView)
            }
        } else {
            if (nineSongRoomSeatView == null) {
                initSongView()
            } else {
                nineSongRoomSeatView?.setSeatList(seatList, true)
                ViewUtils.removeViewFormParent(nineSongRoomSeatView)
                mBinding.seatContainer.removeAllViews()
                mBinding.seatContainer.addView(nineSongRoomSeatView)

            }
        }
    }

    override fun updatePkResult(roomPkInfo: RoomPkInfo?, isClear: Boolean) {
        for (i in 0 until seatList.size) {
            val seatInfo = seatList[i]
            seatInfo.roomUserSeatInfo ?: continue
            updatePkStatus(roomPkInfo, seatInfo, isClear)
            if (isSevenRoom()) {
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

    private fun updatePkStatus(
        roomPkInfo: RoomPkInfo?, roomSeatInfo: RoomSeatInfo, isClear: Boolean = false
    ) {
        if (isClear || roomPkInfo == null) {
            if (roomSeatInfo.pkStatus != 0) {
                roomSeatInfo.pkStatus = 0
            }
        } else {
            val redMemberList = roomPkInfo.redMemberList
            val blueMemberList = roomPkInfo.blueMemberList
            val result = roomPkInfo.result
            val roomUserSeatInfo = roomSeatInfo.roomUserSeatInfo ?: return
            if (redMemberList.contains(roomUserSeatInfo.userId)) {
                if (result == RoomPkInfo.RESULT_BLUE_SUCCESS) {
                    roomSeatInfo.pkStatus = 2
                } else if (result == RoomPkInfo.RESULT_RED_SUCCESS) {
                    roomSeatInfo.pkStatus = 1
                } else {
                    roomSeatInfo.pkStatus = 3
                }
            } else if (blueMemberList.contains(roomUserSeatInfo.userId)) {
                if (result == RoomPkInfo.RESULT_BLUE_SUCCESS) {
                    roomSeatInfo.pkStatus = 1
                } else if (result == RoomPkInfo.RESULT_RED_SUCCESS) {
                    roomSeatInfo.pkStatus = 2
                } else {
                    roomSeatInfo.pkStatus = 3
                }
            } else {
                roomSeatInfo.pkStatus = 0
            }
        }

    }

    override fun updateSeatRoseInfo() {
        for (i in 0 until seatList.size) {
            val seatInfo = seatList[i]
            val roomUserSeatInfo = seatInfo.roomUserSeatInfo
            if (roomUserSeatInfo != null) {
                if (isSevenRoom()) {
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
        } else if (source == ChatConstant.ACTION_RESET_QUEUE_PRICE) {
            //更换插队玫瑰数成功
            getRoomDetail()
            refreshSeatRoseInfo()
        }
    }

    override fun addHostSurfaceView() {
        if (isSevenRoom()) {
            if (hasExpand) {
                sevenSongRoomScaleView?.setSeatHost(seatList)
            } else {
                sevenSongRoomSeatView?.setSeatHost(seatList)
            }
        } else {
            if (hasExpand) {
                nineSongRoomScaleView?.setSeatHost(seatList)
            } else {
                nineSongRoomSeatView?.setSeatHost(seatList)
            }
        }

    }

    private fun clearSurfaceView() {
//        for (i in 0 until surfaceViewList.size) {
//            val liveRoomSeatBean = surfaceViewList[i]
//            liveRoomSeatBean?.apply {
//                this.uid = 0
//                surfaceViewList[i] = this
//                ViewUtils.removeViewFormParent(this.surfaceView)
//            }
//        }
    }

    private fun showScaleSeatView() {
        if (isSevenRoom()) {
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
                sevenSongRoomScaleView?.setSeatList(seatList, scalePosition == -1)
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
                nineSongRoomScaleView?.setSeatList(seatList, scalePosition == -1)
            }
        }

    }

    override fun refreshAutoSeat() {
        if (isSevenRoom()) {
            rankViewBinding.roomInfo = roomSourceBean
        } else {
            nineRankViewBinding.roomInfo = roomSourceBean
        }
    }

    private lateinit var rankViewBinding: ViewSevenRoomRankViewBinding
    private fun addRankView() {
        if (isSevenRoom()) {
            rankViewBinding.apply {
                bindSevenRankView()
            }
        } else {
            nineRankViewBinding.apply {
                bindNineRankView()
            }
        }
    }

    private lateinit var nineRankViewBinding: ViewNineRoomRankViewBinding
    private fun initRankView() {
        if (isSevenRoom()) {
            val rankView =
                LayoutInflater.from(mContext).inflate(R.layout.view_seven_room_rank_view, null)
            rankViewBinding = DataBindingUtil.bind(rankView)!!
            mBinding.flCustomView.addView(rankView)
        } else {
            val rankView =
                LayoutInflater.from(mContext).inflate(R.layout.view_nine_room_rank_view, null)
            nineRankViewBinding = DataBindingUtil.bind(rankView)!!
            mBinding.flTopView.addView(rankView)
        }
    }

    private fun ViewSevenRoomRankViewBinding.bindSevenRankView() {
        roomInfo = roomSourceBean
        this.isRoomOwner = isOwner
        toggleAutoSeat.setOnSingleClickListener {
            showSetAutoSeat()
        }
        isAngle = roomType == RoomListBean.TYPE_SEVEN_ANGLE
        isSong = roomType == RoomListBean.TYPE_SEVEN_SONG
        rvRank.adapter = rankAdapter

        changeGiftAudioStatus(ivAudio)

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
            PageIntentUtil.url2Page(
                mContext,
                if (roomSourceBean.isSongRoom()) WebUrlManager.SONG_ROOM_RULE else WebUrlManager.ANGLE_ROOM_RULE
            )
        }
        ivCrowned.setOnSingleClickListener {
            showCrownedListPop(if (roomSourceBean.isSongRoom()) CrownedUserListPop.TYPE_SONG else CrownedUserListPop.TYPE_ANGLE)
        }
        vgGiftAudio.setOnSingleClickListener {
            changeGiftAudioStatus(ivAudio, true)
            SVGACache.clearCache()
        }
        vgEnterAnim.setOnSingleClickListener {
            changeEnterAnimStatus(iconEnter, true)
        }
    }

    override fun refreshOnlineUser(onlineNum: Int) {
        if (isSevenRoom()) {
            rankViewBinding.tvOnlineNum.text = onlineNum.toString()
        } else {
            nineRankViewBinding.tvOnlineNum.text = onlineNum.toString()
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
                roomAngleRankPop = RoomAngleRankPop.showDialog(
                    mContext,
                    rankList,
                    if (roomSourceBean.isSongRoom()) RoomAngleRankPop.TYPE_SONG else RoomAngleRankPop.TYPE_ANGLE
                )
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
        if (isSevenRoom()) {
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
        fun getUserSeatBeanById(owenId: String): LiveRoomSeatBean? {
            surfaceViewList.forEach { (_, u) ->
                if (u.uid.toString() == owenId) {
                    return u
                }
            }
            return null
        }

        fun getOwnerSurfaceView(owenId: String): View? {
            var surfaceView: View? = null
            surfaceViewList.forEach { (_, u) ->
                if (u.uid.toString() == owenId) {
                    surfaceView = u.surfaceView
                    return@forEach
                }
            }
            return surfaceView
        }
    }
}




