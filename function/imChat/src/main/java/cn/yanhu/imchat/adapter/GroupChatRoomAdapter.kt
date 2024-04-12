package cn.yanhu.imchat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.commonres.bean.RoomListBean
import cn.yanhu.imchat.databinding.AdapterGroupChatRoomItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/4
 * desc:
 */
class GroupChatRoomAdapter : BaseQuickAdapter<RoomListBean, GroupChatRoomAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterGroupChatRoomItemBinding = AdapterGroupChatRoomItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: RoomListBean?) {
        holder.binding.apply {
            roomInfo = item
            svgPlay.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    svgPlay.startAnimation()
                }
                override fun onViewDetachedFromWindow(v: View) {
                    svgPlay.pauseAnimation()
                }
            })
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}