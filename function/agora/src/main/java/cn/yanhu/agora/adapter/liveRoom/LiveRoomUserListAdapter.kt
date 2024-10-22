package cn.yanhu.agora.adapter.liveRoom

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.databinding.AdapterLiveRoomUserListItemBinding
import cn.yanhu.commonres.bean.UserDetailInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/17
 * desc:
 */
class LiveRoomUserListAdapter : BaseQuickAdapter<UserDetailInfo, LiveRoomUserListAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterLiveRoomUserListItemBinding = AdapterLiveRoomUserListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: UserDetailInfo?) {
        holder.binding.apply {
            userinfo = item
            if (item?.seatNum!! >0){
                tvInvite.alpha = 0.5f
            }else{
                tvInvite.alpha = 1f
            }
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}