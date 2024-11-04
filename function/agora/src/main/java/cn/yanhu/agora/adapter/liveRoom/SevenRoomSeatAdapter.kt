package cn.yanhu.agora.adapter.liveRoom

import android.content.Context
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
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
import cn.yanhu.agora.pop.LiveRoomUserRoseDetailPop
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.manager.AppCacheManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.chad.library.adapter4.BaseMultiItemAdapter

/**
 * @author: zhengjun
 * created: 2024/4/1
 * desc:
 */
class SevenRoomSeatAdapter :
    BaseMultiItemAdapter<RoomSeatInfo>() {
    class VH(
        val binding: AdapterSevenRoomAnchorSeatItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class VH2(
        val binding: AdapterSevenRoomUserSeatItemBinding
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
                holder.binding.apply {
                    if (position==2 || position == 5){
                        vgParent.setBackgroundResource(R.drawable.bg_seat_no_top_stroke)
                    }else{
                        vgParent.setBackgroundResource(R.drawable.bg_seat_bottom_stroke)
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

    private fun bindAnchorSeatItem() {
        addItemType(TYPE_ANCHOR_SEAT, object : OnMultiItemAdapterListener<RoomSeatInfo, VH> {
            override fun onBind(holder: VH, position: Int, item: RoomSeatInfo?) {
                holder.binding.apply {
                    anchorSeatInfo.seatInfo = item
                    anchorSeatInfo.vgParent.setBackgroundResource(R.drawable.bg_seat_no_stroke)
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
                    anchorSeatInfo.viewRank.setOnSingleClickListener {
                        showUserReceiveRoseDetailPop(item)
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

    private var liveRoomUserRoseDetailPop: LiveRoomUserRoseDetailPop? = null
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
                    LiveRoomUserRoseDetailPop.showDialog(context, data.data!!)
            }
        })
    }


    private var surfaceViewMap: MutableMap<Int, LiveRoomSeatBean?> = mutableMapOf()

    //更新座位状态
    private fun AdapterSevenRoomUserSeatItemBinding.upDataSeats(position: Int) {
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

            } else if (dto.roomUserSeatInfo!!.userId.toInt() != liveRoomSeatBean.uid) {
                var surfaceView = liveRoomSeatBean.surfaceView
                if (surfaceView == null) {
                    surfaceView = TextureView(context)
                }
                ViewUtils.removeViewFormParent(surfaceView)
                this.itemVideoSf.removeAllViews()
                this.itemVideoSf.addView(surfaceView)
                addVideoSf(surfaceView, dto)

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
        AgoraManager.getInstence().setupVideo(
            dto.roomUserSeatInfo!!.userId.toInt(),
            dto.roomUserSeatInfo!!.userId == AppCacheManager.userId, surfaceView
        )
        if (dto.roomUserSeatInfo!!.userId == AppCacheManager.userId) {
            AgoraManager.getInstence().muteLocalAudioStream(!dto.mikeUser)
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


    companion object {
        const val TYPE_ANCHOR_SEAT = 1
        const val TYPE_USER_SEAR = 2
    }

}