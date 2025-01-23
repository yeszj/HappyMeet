package cn.huanyuan.sweetlove.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.databinding.AdapterSevenRoomAnchorSeatItemBinding
import cn.yanhu.agora.databinding.AdapterSevenRoomUserSeatItemBinding
import com.chad.library.adapter4.BaseMultiItemAdapter

/**
 * @author: zhengjun
 * created: 2025/1/13
 * desc:
 */
class TestAdapter : BaseMultiItemAdapter<String>() {
    init {
        addItemType(0,object : OnMultiItemAdapterListener<String,VH2>{
            override fun onBind(holder: VH2, position: Int, item: String?) {
                holder.binding.anchorSeatInfo.tvSeatIndex.text = item
                holder.binding.anchorSeatInfo.vgParent.setBackgroundResource(cn.yanhu.commonres.R.drawable.shape_blinddate_room_angle)
            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH2 {
                return VH2(parent)
            }
        })
        addItemType(1,object : OnMultiItemAdapterListener<String,VH>{
            override fun onBind(holder: VH, position: Int, item: String?) {
                holder.binding.tvSeatIndex.text = item
                holder.binding.vgParent.setBackgroundResource(cn.yanhu.commonres.R.drawable.shape_blinddate_room_angle)
            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH {
                return VH(parent)
            }
        }).onItemViewType(object : BaseMultiItemAdapter.OnItemViewTypeListener<String>{
            override fun onItemViewType(position: Int, list: List<String>): Int {
                if (position ==0){
                    return 0
                }
                return 1
            }
        })
    }

    class VH2(
        parent: ViewGroup,
        val binding: AdapterSevenRoomAnchorSeatItemBinding = AdapterSevenRoomAnchorSeatItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)
    class VH(
        parent: ViewGroup,
        val binding: AdapterSevenRoomUserSeatItemBinding = AdapterSevenRoomUserSeatItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)
}