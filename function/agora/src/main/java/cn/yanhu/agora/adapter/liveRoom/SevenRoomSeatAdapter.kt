package cn.yanhu.agora.adapter.liveRoom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.R
import cn.yanhu.agora.bean.RoomDetailInfo
import cn.yanhu.agora.bean.RoomSeatInfo
import cn.yanhu.agora.databinding.AdapterSevenRoomAnchorSeatItemBinding
import cn.yanhu.agora.databinding.AdapterSevenRoomUserSeatItemBinding
import cn.yanhu.commonres.manager.AppCacheManager
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
                    item?.apply {
                        if (roomDetailInfo == null) {
                            return
                        }
                        seatInfo = item
                        if (item.roomUserSeatInfo == null) {
                            setEmptySeatInfo(item)
                        }

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


    private fun AdapterSevenRoomUserSeatItemBinding.setEmptySeatInfo(
        item: RoomSeatInfo
    ) {
        if (roomDetailInfo!!.ownerInfo?.userId == AppCacheManager.userId) {
            //是房主
            tvJoinSeatRoseNum.visibility = View.GONE
            tvJoinSeat.text = "邀请上麦"
        } else {
            tvJoinSeat.text = "申请上麦"
            tvJoinSeat.setBackgroundResource(R.drawable.bg_seat_invite)
            if (item.seatRoseNum > 0) {
                tvJoinSeatRoseNum.visibility = View.VISIBLE
            } else {
                tvJoinSeatRoseNum.visibility = View.GONE
            }
        }

    }


    companion object {
        const val TYPE_ANCHOR_SEAT = 1
        const val TYPE_USER_SEAR = 2
    }

}