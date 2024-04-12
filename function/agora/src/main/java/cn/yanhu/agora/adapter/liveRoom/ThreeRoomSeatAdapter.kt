package cn.yanhu.agora.adapter.liveRoom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.R
import cn.yanhu.agora.bean.RoomDetailInfo
import cn.yanhu.agora.bean.RoomSeatInfo
import cn.yanhu.agora.databinding.AdapterThreeRoomAnchorSeatItemBinding
import cn.yanhu.agora.databinding.AdapterThreeRoomUserSeatItemBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.ViewUtils
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.SexManager
import cn.yanhu.imchat.manager.ImUserManager
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
                    if (position == 1) {
                        ViewUtils.setMarginRight(vgParent, 0)
                    } else {
                        ViewUtils.setMarginRight(
                            vgParent,
                            CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_4)
                        )
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

    private fun bindAnchorSeatItem() {
        addItemType(TYPE_ANCHOR_SEAT, object : OnMultiItemAdapterListener<RoomSeatInfo, VH> {
            override fun onBind(holder: VH, position: Int, item: RoomSeatInfo?) {
                holder.binding.apply {
                    anchorSeatInfo.seatInfo = item
                    setApplyInfo()
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

    private fun AdapterThreeRoomAnchorSeatItemBinding.setApplyInfo() {
        if (roomDetailInfo != null) {
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
                    tvJoinSeat.text = "申请上麦"
                    tvJoinSeat.setBackgroundResource(R.drawable.bg_seat_invite)
                    if (item.seatRoseNum > 0) {
                        tvJoinSeatRoseNum.visibility = View.VISIBLE
                    } else {
                        tvJoinSeatRoseNum.visibility = View.GONE
                    }
                } else {
                    tvJoinSeat.text = "女嘉宾位"
                    tvJoinSeat.background = null
                    tvJoinSeatRoseNum.visibility = View.GONE
                }
            } else {
                if (position == 2) {
                    tvJoinSeat.text = "申请上麦"
                    tvJoinSeat.setBackgroundResource(R.drawable.bg_seat_invite)
                    if (item.seatRoseNum > 0) {
                        tvJoinSeatRoseNum.visibility = View.VISIBLE
                    } else {
                        tvJoinSeatRoseNum.visibility = View.GONE
                    }
                } else {
                    tvJoinSeat.text = "男嘉宾位"
                    tvJoinSeat.background = null
                    tvJoinSeatRoseNum.visibility = View.GONE
                }
            }
        }

    }


    companion object {
        const val TYPE_ANCHOR_SEAT = 1
        const val TYPE_USER_SEAR = 2
    }

}