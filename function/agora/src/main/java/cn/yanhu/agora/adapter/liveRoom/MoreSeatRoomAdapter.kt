package cn.yanhu.agora.adapter.liveRoom

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
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
import cn.yanhu.agora.databinding.AdapterSevenRoomAnchorSeatItemBinding
import cn.yanhu.agora.databinding.AdapterSevenRoomUserSeatItemBinding
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.pop.LiveRoomUserRoseRankPop
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.commonres.manager.AppCacheManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.chad.library.adapter4.BaseMultiItemAdapter

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:多人房 座位
 * 7人和9人
 */
class MoreSeatRoomAdapter(val frgType: Int, val roomType: Int) :
    BaseMultiItemAdapter<RoomSeatInfo>() {
    class VH(
        val binding: AdapterSevenRoomAnchorSeatItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class VH2(
        val binding: AdapterSevenRoomUserSeatItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    var roomDetailInfo: RoomDetailInfo? = null

    class EntityDiffCallback : DiffUtil.ItemCallback<RoomSeatInfo>() {
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
            return false
        }
    }

    init {
        bindAnchorSeatItem()
        bindUserSeatItem().onItemViewType { position, _ ->
            if (frgType == RoomListBean.FRG_SEVEN_ROOM) {
                if (position == 0) {
                    TYPE_ANCHOR_SEAT
                } else {
                    TYPE_USER_SEAR
                }
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
                    bindItemInfo(item, position)
                    executePendingBindings()
                }
            }


            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH2 {
                val binding: AdapterSevenRoomUserSeatItemBinding =
                    AdapterSevenRoomUserSeatItemBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                return VH2(binding)
            }

        })

    private fun AdapterSevenRoomUserSeatItemBinding.bindItemInfo(
        item: RoomSeatInfo?,
        position: Int
    ) {
        tvSeatIndex.text = position.toString()
        this.currentRoomType = roomType
        this.isOwner = isRoomOwner
        if (item!!.id == 1) {
            tvOwner.visibility = View.VISIBLE
        } else {
            tvOwner.visibility = View.INVISIBLE
        }
        val isShowNoTopBg: Boolean
        if (frgType == RoomListBean.FRG_NINE_ROOM) {
            isShowNoTopBg = position == 1 || position == 4 || position == 7
            if (isExpandStyle) {
                ViewUtils.setViewHeight(
                    vgParent,
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_80)
                )
            } else {
                ViewUtils.setViewHeight(
                    vgParent,
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_120)
                )
            }
        } else {
            if (isExpandStyle) {
                ViewUtils.setViewHeight(
                    vgParent,
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_120)
                )
            } else {
                ViewUtils.setViewHeight(
                    vgParent,
                    CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_142)
                )
            }
            isShowNoTopBg = position == 2 || position == 5
        }
        if (item.id == 1 && frgType == RoomListBean.FRG_SEVEN_ROOM) {
            vgParent.setBackgroundResource(R.drawable.bg_seat_no_stroke)
        } else {
            if (isShowNoTopBg) {
                vgParent.setBackgroundResource(R.drawable.bg_seat_no_top_stroke)
            } else {
                vgParent.setBackgroundResource(R.drawable.bg_seat_bottom_stroke)
            }
        }

        upDataSeats(position)
        item?.apply {
            if (roomDetailInfo == null) {
                return
            }
            seatInfo = item
            if (item.roomUserSeatInfo == null) {
                setEmptySeatInfo(item)
            }
        }
        viewRank.setOnSingleClickListener {
            showUserReceiveRoseDetailPop(item)
        }
    }

    private fun bindAnchorSeatItem() {
        addItemType(TYPE_ANCHOR_SEAT, object : OnMultiItemAdapterListener<RoomSeatInfo, VH> {
            override fun onBind(holder: VH, position: Int, item: RoomSeatInfo?) {

                holder.binding.apply {
                    if (isExpandStyle) {
                        ViewUtils.setViewHeight(
                            vgAnchorParent,
                            CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_120)
                        )
                    } else {
                        ViewUtils.setViewHeight(
                            vgAnchorParent,
                            CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_142)
                        )
                    }
                    anchorSeatInfo.apply {
                        bindItemInfo(item, position)
                    }
                    executePendingBindings()
                }
            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH {
                val binding: AdapterSevenRoomAnchorSeatItemBinding =
                    AdapterSevenRoomAnchorSeatItemBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                return VH(binding)
            }

            override fun isFullSpanItem(itemType: Int): Boolean {
                return true
            }
        })
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


     var surfaceViewMap: MutableMap<Int, LiveRoomSeatBean?> = mutableMapOf()

    //更新座位状态
    private fun AdapterSevenRoomUserSeatItemBinding.upDataSeats(position: Int) {
        val dto: RoomSeatInfo = getItem(position) ?: return

        if (dto.roomUserSeatInfo != null) {
            this.isSelf = dto.roomUserSeatInfo!!.userId == AppCacheManager.userId

            val currentSurfaceViewMap: MutableMap<Int, LiveRoomSeatBean?> = surfaceViewMap
            val liveRoomSeatBean: LiveRoomSeatBean? =
                currentSurfaceViewMap[position]
            var surfaceView: View?
            if (liveRoomSeatBean == null) {
                surfaceView = TextureView(context)


                this.itemVideoSf.addView(surfaceView)

                addVideoSf(surfaceView, dto, position)

            } else if (dto.roomUserSeatInfo!!.userId.toInt() != liveRoomSeatBean.uid) {
                surfaceView = liveRoomSeatBean.surfaceView
                if (surfaceView == null) {
                    surfaceView = TextureView(context)
                }
                ViewUtils.removeViewFormParent(surfaceView)
                this.itemVideoSf.removeAllViews()
                this.itemVideoSf.addView(surfaceView)
                addVideoSf(surfaceView, dto, position)

            } else {
                surfaceView = liveRoomSeatBean.surfaceView
                if (surfaceView == null) {
                    surfaceView = TextureView(context)
                }
                ViewUtils.removeViewFormParent(surfaceView)
                this.itemVideoSf.removeAllViews()
                this.itemVideoSf.addView(surfaceView)
                addVideoSf(surfaceView, dto, position)
            }
            itemVideoSf.tag = surfaceView
        } else {
            this.isSelf = false
            this.itemVideoSf.removeAllViews()
        }
    }

    private fun addVideoSf(surfaceView: View, dto: RoomSeatInfo, position: Int) {
        surfaceViewMap[position] =
            LiveRoomSeatBean(dto.roomUserSeatInfo!!.userId.toInt(), surfaceView)
        AgoraManager.getInstance().setupVideo(
            dto.roomUserSeatInfo!!.userId.toInt(),
            dto.roomUserSeatInfo!!.userId == AppCacheManager.userId, surfaceView
        )
        if (dto.roomUserSeatInfo!!.userId == AppCacheManager.userId) {
            AgoraManager.getInstance().muteLocalAudioStream(!dto.mikeUser)
        }
    }

    private fun AdapterSevenRoomUserSeatItemBinding.setEmptySeatInfo(
        item: RoomSeatInfo
    ) {
        if (roomDetailInfo!!.ownerInfo?.userId == AppCacheManager.userId) {
            //是房主
            tvJoinSeat.text = "邀请上麦"
        } else {
            tvJoinSeat.text = "申请上麦"
            tvJoinSeat.setBackgroundResource(R.drawable.bg_seat_invite)
        }

    }

    private var isRoomOwner = false
    fun setIsOwner(owner: Boolean) {
        this.isRoomOwner = owner
    }

    private var isExpandStyle = false

    @SuppressLint("NotifyDataSetChanged")
    fun setIsExpandStyle(isExpand: Boolean) {
        isExpandStyle = isExpand
        notifyDataSetChanged()
    }


    companion object {
        const val TYPE_ANCHOR_SEAT = 1
        const val TYPE_USER_SEAR = 2
    }

}