package cn.yanhu.agora.adapter.liveRoom

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.databinding.AdapterLiveRoomOnlineUserItemBinding
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
            this.isOwner = isRoomOwner
            userinfo = item
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    private var isRoomOwner = false
    fun setIsOwner(isOwner: Boolean) {
        isRoomOwner = isOwner
    }

}