package cn.yanhu.agora.adapter.liveRoom

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.R
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.LiveRoomSeatBean
import cn.yanhu.agora.bean.UserReceiveRoseInfo
import cn.yanhu.commonres.bean.response.WishResponse
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.RoomSeatInfo
import cn.yanhu.agora.databinding.AdapterThreeRoomAnchorSeatItemBinding
import cn.yanhu.agora.databinding.AdapterThreeRoomUserSeatItemBinding
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.pop.LiveRoomUserRoseRankPop
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.RoomSwitchCacheManager
import cn.yanhu.commonres.manager.SexManager
import cn.yanhu.imchat.manager.ImUserManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.chad.library.adapter4.BaseMultiItemAdapter

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
class ThreeRoomSeatAdapter :
    BaseMultiItemAdapter<RoomSeatInfo>() {
    class VH(
        val binding: AdapterThreeRoomAnchorSeatItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class VH2(
        val binding: AdapterThreeRoomUserSeatItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    var roomDetailInfo: RoomDetailInfo? = null


    init {
        bindAnchorSeatItem()
        bindUserSeatItem().onItemViewType { position, _ ->
            if (position == 0) {
                TYPE_ANCHOR_SEAT
            } else {
                TYPE_USER_SEAR
            }
        }
    }

    private fun bindUserSeatItem() =
        addItemType(TYPE_USER_SEAR, object : OnMultiItemAdapterListener<RoomSeatInfo, VH2> {
            override fun onBind(holder: VH2, position: Int, item: RoomSeatInfo?) {
                //绑定男女嘉宾位置信息
                bindUserSeatInfo(holder, item, position)
            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH2 {
                val binding: AdapterThreeRoomUserSeatItemBinding =
                    AdapterThreeRoomUserSeatItemBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                return VH2(binding)
            }

        })

    private fun bindUserSeatInfo(
        holder: VH2,
        item: RoomSeatInfo?,
        position: Int
    ) {
        holder.binding.apply {
            item?.apply {
                if (roomDetailInfo == null) {
                    return
                }
                isPk = isPkStatus
                setViewMargin(position)
                seatInfo = item
                if (item.roomUserSeatInfo == null) {
                    setEmptySeatInfo(position, item)
                    surfaceViewMap[position] = null
                }

            }
            upDataSeats(position)

            viewRank.setOnSingleClickListener {
                showUserReceiveRoseDetailPop(item)
            }

            executePendingBindings()
        }
    }

    private fun AdapterThreeRoomUserSeatItemBinding.setViewMargin(position: Int) {
        val marginDimen = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_4)
        if (isPkStatus) {
            ViewUtils.setViewPadding(
                tvJoinSeat, CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_6),
                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_3)
            )
            vgParent.setCornerRadius(0)
            ViewUtils.clearVieMargin(vgParent)
            ViewUtils.setViewHeight(
                vgParent,
                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_106)
            )
        } else {
            ViewUtils.setViewPadding(
                tvJoinSeat, CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_12),
                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_6)
            )
            vgParent.setCornerRadius(marginDimen)
            ViewUtils.setViewHeight(
                vgParent,
                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_204)
            )
            if (position == 1) {
                val layoutParams = vgParent.layoutParams
                if (layoutParams is ViewGroup.MarginLayoutParams) {
                    layoutParams.rightMargin = 0
                    layoutParams.leftMargin = marginDimen
                    vgParent.layoutParams = layoutParams
                }
            } else {
                val layoutParams = vgParent.layoutParams
                if (layoutParams is ViewGroup.MarginLayoutParams) {
                    layoutParams.rightMargin = marginDimen
                    layoutParams.leftMargin = marginDimen
                    vgParent.layoutParams = layoutParams
                }
            }
        }
    }

    private var liveRoomUserRoseDetailPop: LiveRoomUserRoseRankPop? = null
    private fun showUserReceiveRoseDetailPop(item: RoomSeatInfo?) {
        request({
            agoraRxApi.getRoomUserRoseList(
                roomDetailInfo?.roomId,
                item?.roomUserSeatInfo?.userId
            )
        }, object : OnRequestResultListener<UserReceiveRoseInfo> {
            override fun onSuccess(data: BaseBean<UserReceiveRoseInfo>) {
                if (CommonUtils.isPopShow(liveRoomUserRoseDetailPop)) {
                    return
                }
                liveRoomUserRoseDetailPop =
                    LiveRoomUserRoseRankPop.showDialog(context, data.data!!)
            }
        })
    }

    private fun bindAnchorSeatItem() {
        addItemType(TYPE_ANCHOR_SEAT, object : OnMultiItemAdapterListener<RoomSeatInfo, VH> {
            override fun onBind(
                holder: VH,
                position: Int,
                item: RoomSeatInfo?,
                payloads: List<Any>
            ) {
                if (payloads.isNotEmpty()) {
                    if (payloads[0] is String) {
                        val switch = payloads[0]
                        if (switch == "showEnterAnim") {
                            if (!isPkStatus) {
                                holder.binding.vgEnterAnim.visibility = View.VISIBLE
                                changeEnterAnimStatus(holder.binding.ivEnter, false)
                            }
                        } else if (switch == "hideEnterAnim") {
                            holder.binding.vgEnterAnim.visibility = View.GONE
                        } else if (switch == "updateToggleAuto") {
                            holder.binding.roomInfo = roomDetailInfo
                        } else if (switch == "0") {
                            holder.binding.tvSwitch.visibility = View.GONE
                        } else if (switch == "refreshPkSuccessCount") {
                            holder.binding.apply {
                                this.anchorSeatInfo.roomInfo = roomDetailInfo
                            }
                        } else {
                            if (isPkStatus) {
                                holder.binding.tvSwitch.visibility = View.GONE
                            } else {
                                holder.binding.tvSwitch.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        holder.binding.apply {
                            this.roomInfo = roomDetailInfo
                            this.anchorSeatInfo.seatInfo = item
                            bindWishInfo()
                            setApplyInfo()
                        }
                    }

                }
            }

            override fun onBind(holder: VH, position: Int, item: RoomSeatInfo?) {
                val anchorSeatInfoView = holder.itemView.findViewById<View>(R.id.anchorSeatInfo)

                holder.binding.apply {
                    val owner = roomDetailInfo?.isOwner() == true
                    this.isPk = isPkStatus
                    this.anchorSeatInfo.isPk = isPkStatus
                    this.anchorSeatInfo.seatIsOwner = true
                    setAnchorViewMargin(holder, anchorSeatInfoView, owner)
                    bindWishInfo()

                    anchorSeatInfo.seatInfo = item
                    this.isOwner = owner

                    this.roomInfo = roomDetailInfo
                    anchorSeatInfo.roomInfo = roomDetailInfo
                    val tag = anchorSeatInfo.itemVideoSf.tag
                    if (tag == null || tag !is SurfaceView) {
                        val surfaceView = TextureView(context)
                        anchorSeatInfo.itemVideoSf.tag = surfaceView
                        anchorSeatInfo.itemVideoSf.addView(surfaceView)
                        addVideoSf(surfaceView, item!!)
                    } else {
                        ViewUtils.removeViewFormParent(tag)
                        anchorSeatInfo.itemVideoSf.removeView(tag)
                        anchorSeatInfo.itemVideoSf.addView(tag)
                        addVideoSf(tag, item!!)
                    }
                    setApplyInfo()
                    anchorSeatInfo.viewRank.setOnSingleClickListener {
                        showUserReceiveRoseDetailPop(item)
                    }
                    changeEnterAnimStatus(ivEnter, false)
                    changeGiftAudioStatus(ivAudio, false)
                    vgGiftAudio.setOnSingleClickListener {
                        changeGiftAudioStatus(ivAudio, true)
                    }
                    vgEnterAnim.setOnSingleClickListener {
                        changeEnterAnimStatus(ivEnter, true)
                    }
                    executePendingBindings()
                }
            }


            fun changeEnterAnimStatus(ivEnterAnim: AppCompatImageView, isSave: Boolean = false) {
                val roomSwitchInfo =
                    RoomSwitchCacheManager.getRoomSwitchInfo(roomDetailInfo!!.roomId!!)
                if (isSave) {
                    roomSwitchInfo.enterAnimOpen = !roomSwitchInfo.enterAnimOpen
                    RoomSwitchCacheManager.saveRoomSwitchInfo(roomSwitchInfo)
                }
                if (roomSwitchInfo.enterAnimOpen) {
                    ivEnterAnim.setImageResource(R.drawable.svg_voice_on)
                } else {
                    ivEnterAnim.setImageResource(R.drawable.svg_voice_off)
                }
            }

            fun changeGiftAudioStatus(ivAudio: ImageView, isSave: Boolean = false) {
                val roomSwitchInfo =
                    RoomSwitchCacheManager.getRoomSwitchInfo(roomDetailInfo!!.roomId!!)
                if (isSave) {
                    roomSwitchInfo.giftVoiceOpen = !roomSwitchInfo.giftVoiceOpen
                    RoomSwitchCacheManager.saveRoomSwitchInfo(roomSwitchInfo)
                }
                if (roomSwitchInfo.giftVoiceOpen) {
                    ivAudio.setImageResource(R.drawable.svg_voice_on)
                } else {
                    ivAudio.setImageResource(R.drawable.svg_voice_off)
                }

            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH {
                val binding: AdapterThreeRoomAnchorSeatItemBinding =
                    AdapterThreeRoomAnchorSeatItemBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                return VH(binding)
            }

            override fun isFullSpanItem(itemType: Int): Boolean {
                return true
            }
        })
    }

    private fun AdapterThreeRoomAnchorSeatItemBinding.setAnchorViewMargin(
        holder: VH,
        anchorSeatInfoView: View,
        owner: Boolean
    ) {
        if (isPkStatus) {
            holder.binding.tvSwitch.visibility = View.GONE
            vgEnterAnim.visibility = View.INVISIBLE
            anchorSeatInfo.vgParent.setCornerRadius(0)
            ViewUtils.clearVieMargin(anchorSeatInfo.vgParent)
            ViewUtils.setViewHeight(
                vgAnchorParent,
                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_198)
            )
            ViewUtils.setViewPadding(vgAnchorParent, 0, 0)
            ViewUtils.setViewWidthMatch(anchorSeatInfoView)
        } else {
            anchorSeatInfo.vgParent.setCornerRadius(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_4))
            ViewUtils.setMarginNoBottom(
                anchorSeatInfo.vgParent,
                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_4)
            )
            ViewUtils.setViewPadding(
                vgAnchorParent,
                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_4),
                0
            )
            ViewUtils.setViewHeight(
                vgAnchorParent,
                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_176)
            )
            ViewUtils.setViewWidth(
                anchorSeatInfoView,
                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_160)
            )
            holder.binding.tvSwitch.visibility = View.VISIBLE
            if (owner) {
                vgEnterAnim.visibility = View.VISIBLE
            }
        }
    }

    private fun AdapterThreeRoomAnchorSeatItemBinding.bindWishInfo() {
        val tag = banner.tag
        val list = wishResponse?.list
        if (tag == null) {
            val wishGiftBannerAdapter = WishGiftBannerAdapter(context, mutableListOf())
            banner.tag = wishGiftBannerAdapter
            banner.setAdapter(wishGiftBannerAdapter)
            banner.setOnBannerListener { _, _ ->
                wishResponse?.apply {
                    onRoomItemClickListener?.onClickWish()
                }
            }
            wishGiftBannerAdapter.setDatas(list)
        } else {
            val wishGiftBannerAdapter = tag as WishGiftBannerAdapter
            wishGiftBannerAdapter.setDatas(list)
        }
        val owner = roomDetailInfo?.isOwner() == true
        if ((!owner && wishResponse?.status == 0) || isPkStatus) {
            vgWish.visibility = View.INVISIBLE
        } else {
            vgWish.visibility = View.VISIBLE
        }
        tvStatus.setOnSingleClickListener {
            onRoomItemClickListener?.onClickWish()
        }
        tvStatus.text = wishResponse?.getStatusDesc()
    }

    var surfaceViewMap: MutableMap<Int, LiveRoomSeatBean?> = mutableMapOf()

    //更新座位状态
    private fun AdapterThreeRoomUserSeatItemBinding.upDataSeats(position: Int) {
        val dto: RoomSeatInfo = getItem(position) ?: return

        if (dto.roomUserSeatInfo != null) {
            val currentSurfaceViewMap: MutableMap<Int, LiveRoomSeatBean?> = surfaceViewMap
            val liveRoomSeatBean: LiveRoomSeatBean? =
                currentSurfaceViewMap[position]
            if (liveRoomSeatBean == null || (liveRoomSeatBean.surfaceView as TextureView?)?.isAvailable == false) {
                val surfaceView = TextureView(context)
                currentSurfaceViewMap[position] =
                    LiveRoomSeatBean(dto.roomUserSeatInfo!!.userId.toInt(), surfaceView)
                this.itemVideoSf.addView(surfaceView)
                addVideoSf(surfaceView, dto)
            } else {
                var surfaceView = liveRoomSeatBean.surfaceView
                if (surfaceView == null) {
                    surfaceView = TextureView(context)
                } else {
                    if (liveRoomSeatBean.uid == dto.roomUserSeatInfo!!.userId.toInt()) {
                        AgoraManager.getInstance().setupVideo(
                            dto.roomUserSeatInfo!!.userId.toInt(),
                            dto.roomUserSeatInfo!!.userId == AppCacheManager.userId, surfaceView
                        )
                        return
                    }
                }
                ViewUtils.removeViewFormParent(surfaceView)
                this.itemVideoSf.removeAllViews()
                this.itemVideoSf.addView(surfaceView)
                addVideoSf(surfaceView, dto)
            }
        } else {
            surfaceViewMap[position] = null
            this.itemVideoSf.removeAllViews()
        }
    }

    private fun addVideoSf(surfaceView: View, dto: RoomSeatInfo) {
        AgoraManager.getInstance().setupVideo(
            dto.roomUserSeatInfo!!.userId.toInt(),
            dto.roomUserSeatInfo!!.userId == AppCacheManager.userId, surfaceView
        )
        if (dto.roomUserSeatInfo!!.userId == AppCacheManager.userId) {
            AgoraManager.getInstance().muteLocalAudioStream(!dto.mikeUser)
        }
    }

    private fun AdapterThreeRoomAnchorSeatItemBinding.setApplyInfo() {
        if (roomDetailInfo != null && roomDetailInfo!!.ownerInfo?.userId == AppCacheManager.userId && !isPkStatus) {
            val manApplyInfo = roomDetailInfo!!.manApplyInfo
            val build = Spans.builder().text("${manApplyInfo.applyNum}申请").color(
                CommonUtils.getColor(
                    cn.yanhu.baselib.R.color.manColor
                )
            ).text(" ${manApplyInfo.onlineNum}在线").build()
            tvManApplyCount.text = build

            val womanApplyInfo = roomDetailInfo!!.womanApplyInfo
            val build2 = Spans.builder().text("${womanApplyInfo.applyNum}申请").color(
                CommonUtils.getColor(
                    cn.yanhu.baselib.R.color.femaleColor
                )
            ).text(" ${womanApplyInfo.onlineNum}在线").build()
            tvWomanApplyCount.text = build2
            tvWomanApplyCount.visibility = View.VISIBLE
            tvManApplyCount.visibility = View.VISIBLE
        } else {
            tvWomanApplyCount.visibility = View.INVISIBLE
            tvManApplyCount.visibility = View.INVISIBLE
        }
    }


    private fun AdapterThreeRoomUserSeatItemBinding.setEmptySeatInfo(
        position: Int,
        item: RoomSeatInfo
    ) {
        if (roomDetailInfo!!.ownerInfo?.userId == AppCacheManager.userId) {
            //是房主
            tvJoinSeatRoseNum.visibility = View.GONE
            if (position == 1) {
                tvJoinSeat.text = "邀请男嘉宾"
            } else {
                tvJoinSeat.text = "邀请女嘉宾"
            }
        } else {
            val selfUserInfo = ImUserManager.getSelfUserInfo()
            val selfIsMan = SexManager.isMan(selfUserInfo.gender)
            if (selfIsMan) {
                if (position == 1) {
                    setEmptySeatInfo(item)
                } else {
                    setEmptyOtherSeatInfo("女嘉宾位")
                }
            } else {
                if (position == 2) {
                    setEmptySeatInfo(item)
                } else {
                    setEmptyOtherSeatInfo("男嘉宾位")
                }
            }
        }

    }

    private fun AdapterThreeRoomUserSeatItemBinding.setEmptyOtherSeatInfo(value: String) {
        tvJoinSeat.text = value
        tvJoinSeat.background = null
        tvJoinSeatRoseNum.visibility = View.GONE
    }

    private fun AdapterThreeRoomUserSeatItemBinding.setEmptySeatInfo(item: RoomSeatInfo) {
        if (item.seatUserRose) {
            tvJoinSeat.text = "申请上麦"
        } else {
            tvJoinSeat.text = "免费上麦"
        }
        tvJoinSeat.setBackgroundResource(R.drawable.bg_seat_invite)
        if (item.seatRoseNum > 0) {
            tvJoinSeatRoseNum.visibility = View.VISIBLE
        } else {
            tvJoinSeatRoseNum.visibility = View.GONE
        }
    }

    private var wishResponse: WishResponse? = null
    fun updateWishInfo(wishRes: WishResponse) {
        wishResponse = wishRes
        notifyItemChanged(0, true)
    }

    var onRoomItemClickListener: OnRoomItemClickListener? = null

    interface OnRoomItemClickListener {
        fun onClickWish()
    }

    private var isPkStatus = false

    @SuppressLint("NotifyDataSetChanged")
    fun setIsPk(isPk: Boolean) {
        isPkStatus = isPk
        notifyDataSetChanged()
    }

    companion object {
        const val TYPE_ANCHOR_SEAT = 1
        const val TYPE_USER_SEAR = 2
    }

}