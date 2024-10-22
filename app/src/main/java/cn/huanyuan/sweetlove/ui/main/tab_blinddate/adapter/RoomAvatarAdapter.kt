package cn.huanyuan.sweetlove.ui.main.tab_blinddate.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterBlindRoomAvatarItemBinding
import cn.yanhu.commonres.bind.loadImage
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/2/19
 * desc:
 */
class RoomAvatarAdapter: BaseQuickAdapter<String, RoomAvatarAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterBlindRoomAvatarItemBinding = AdapterBlindRoomAvatarItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: String?) {
        holder.binding.apply {
            loadImage(ivAvatar,item)
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}