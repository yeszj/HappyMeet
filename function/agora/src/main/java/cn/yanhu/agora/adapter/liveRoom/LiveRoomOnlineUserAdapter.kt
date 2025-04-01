package cn.yanhu.agora.adapter.liveRoom

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.databinding.AdapterLiveRoomOnlineUserItemBinding
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/17
 * desc:
 */
class LiveRoomOnlineUserAdapter : BaseQuickAdapter<UserDetailInfo, LiveRoomOnlineUserAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterLiveRoomOnlineUserItemBinding = AdapterLiveRoomOnlineUserItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: UserDetailInfo?) {
        holder.binding.apply {
            this.isSetAdmin = roomAdminSwitch
            userinfo = item
            this.roomDetailInfo = currentRoomDetailInfo
            this.isCanOperate =
                currentRoomDetailInfo?.isOwner() == true || currentRoomDetailInfo?.isAdmin() == true || currentRoomDetailInfo?.roomAdmin == true
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(
        holder: VH, position: Int, item: UserDetailInfo?, payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            holder.binding.apply {
                this.isSetAdmin = roomAdminSwitch
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    var roomAdminSwitch = false
    fun changeAdminSetSwitch() {
        roomAdminSwitch = !roomAdminSwitch
        notifyItemRangeChanged(0, itemCount, true)
    }

    private var currentRoomDetailInfo: RoomDetailInfo? = null

    @SuppressLint("NotifyDataSetChanged")
    fun refreshRoomInfo(roomDetailInfo: RoomDetailInfo, isRefresh: Boolean = false) {
        this.currentRoomDetailInfo = roomDetailInfo
        if (isRefresh) {
            notifyDataSetChanged()
        }
    }
}