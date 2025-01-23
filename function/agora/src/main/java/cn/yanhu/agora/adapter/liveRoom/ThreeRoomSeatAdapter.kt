package cn.yanhu.agora.adapter.liveRoom

import android.content.Context
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.R
import cn.yanhu.agora.api.agoraRxApi
import cn.yanhu.agora.bean.LiveRoomSeatBean
import cn.yanhu.agora.bean.UserReceiveRoseInfo
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

    class EntityDiffCallback: DiffUtil.ItemCallback<RoomSeatInfo>() {
        override fun areItemsTheSame(oldItem: RoomSeatInfo, newItem: RoomSeatInfo): Boolean {
            // 判断是否是同一个 item（通常使用id字段判断）
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RoomSeatInfo, newItem: RoomSeatInfo): Boolean {
            // 如果是同一个item，则判断item内的数据内容是否有变化
            return oldItem.roomUserSeatInfo?.userId == newItem.roomUserSeatInfo?.userId
        }

        override fun getChangePayload(oldItem: RoomSeatInfo, newItem: RoomSeatInfo): Any? {
            // 可选实现
            return true
        }
    }

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
                holder.binding.apply {
                    item?.apply {
                        if (roomDetailInfo == null) {
                            return
                        }
                        seatInfo = item
                        if (item.roomUserSeatInfo == null) {
                            setEmptySeatInfo(position, item)
                        }

                    }
                    upDataSeats(position)
                    if (position == 1) {
                        ViewUtils.setMarginRight(vgParent, 0)
                    } else {
                        ViewUtils.setMarginRight(
                            vgParent,
                            CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_4)
                        )
                    }
                    viewRank.setOnSingleClickListener {
                        showUserReceiveRoseDetailPop(item)
                    }

                    executePendingBindings()
                }
            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH2 {
                val binding: AdapterThreeRoomUserSeatItemBinding =
                    AdapterThreeRoomUserSeatItemBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                return VH2(binding)
            }

        })

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
                    holder.binding.apply {
                        this.roomInfo = roomDetailInfo
                        setApplyInfo()
                    }
                }
            }

            override fun onBind(holder: VH, position: Int, item: RoomSeatInfo?) {
                holder.binding.apply {
                    anchorSeatInfo.seatInfo = item
                    this.isOwner = roomDetailInfo!!.ownerInfo!!.userId == AppCacheManager.userId
                    this.roomInfo = roomDetailInfo
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
                    executePendingBindings()
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

    private var surfaceViewMap: MutableMap<Int, LiveRoomSeatBean?> = mutableMapOf()

    //更新座位状态
    private fun AdapterThreeRoomUserSeatItemBinding.upDataSeats(position: Int) {
        val dto: RoomSeatInfo = getItem(position) ?: return

        if (dto.roomUserSeatInfo != null) {
            val currentSurfaceViewMap: MutableMap<Int, LiveRoomSeatBean?> = surfaceViewMap
            val liveRoomSeatBean: LiveRoomSeatBean? =
                currentSurfaceViewMap[position]
            if (liveRoomSeatBean == null) {
                val surfaceView = TextureView(context)
                currentSurfaceViewMap[position] =
                    LiveRoomSeatBean(dto.roomUserSeatInfo!!.userId.toInt(), surfaceView)

                this.itemVideoSf.addView(surfaceView)

                addVideoSf(surfaceView, dto)

                //  RoundUtils.bgBlur(this.anchorSeatInfo.wheetLeaveAlertImg, dto.roomUserSeatInfo.portrait, 240)
            } else if (dto.roomUserSeatInfo!!.userId.toInt() != liveRoomSeatBean.uid) {
                var surfaceView = liveRoomSeatBean.surfaceView
                if (surfaceView == null) {
                    surfaceView = TextureView(context)
                }
                ViewUtils.removeViewFormParent(surfaceView)
                this.itemVideoSf.removeAllViews()
                this.itemVideoSf.addView(surfaceView)
                addVideoSf(surfaceView, dto)

                // RoundUtils.bgBlur(binding.wheetLeaveAlertImg, dto.roomUserSeatInfo.portrait, 240)
            } else {
                var surfaceView = liveRoomSeatBean.surfaceView
                if (surfaceView == null) {
                    surfaceView = TextureView(context)
                }
                ViewUtils.removeViewFormParent(surfaceView)
                this.itemVideoSf.removeAllViews()
                this.itemVideoSf.addView(surfaceView)
                addVideoSf(surfaceView, dto)
            }
        } else {
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
        if (roomDetailInfo != null && roomDetailInfo!!.ownerInfo?.userId == AppCacheManager.userId) {
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


    interface OnRoomItemClickListener {
        fun onClickRose(roomSeatInfo: RoomSeatInfo)
    }

    companion object {
        const val TYPE_ANCHOR_SEAT = 1
        const val TYPE_USER_SEAR = 2
    }

}