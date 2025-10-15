package cn.yanhu.agora.adapter.liveRoom

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.databinding.AdapterRoomGroupMemberItemBinding
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.manager.AppCacheManager
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/17
 * desc:
 */
class RoomGroupMemberAdapter : BaseQuickAdapter<UserDetailInfo, RoomGroupMemberAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterRoomGroupMemberItemBinding = AdapterRoomGroupMemberItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: UserDetailInfo?) {
        holder.binding.apply {
            rankInfo = item
            if (item?.userId== AppCacheManager.userId){
                tvExit.visibility = View.VISIBLE
            }else{
                tvExit.visibility = View.INVISIBLE
            }
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}