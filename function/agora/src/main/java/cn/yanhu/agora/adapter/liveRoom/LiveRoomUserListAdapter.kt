package cn.yanhu.agora.adapter.liveRoom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.databinding.AdapterLiveRoomUserListItemBinding
import cn.yanhu.baselib.utils.CommonUtils
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
            if (CommonUtils.compareZero(item?.roseNum)){
                ivRose.visibility = View.VISIBLE
                tvRoseNum.visibility = View.VISIBLE
            }else{
                ivRose.visibility = View.GONE
                tvRoseNum.visibility = View.GONE
            }
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

}