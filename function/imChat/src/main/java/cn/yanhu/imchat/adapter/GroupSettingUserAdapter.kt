package cn.yanhu.imchat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.imchat.bean.GroupUserInfo
import cn.yanhu.imchat.databinding.AdapterGroupSettingUserItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/4
 * desc:
 */
class GroupSettingUserAdapter:BaseQuickAdapter<GroupUserInfo,GroupSettingUserAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterGroupSettingUserItemBinding = AdapterGroupSettingUserItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: GroupUserInfo?) {
        holder.binding.apply {
            groupUserInfo = item
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}