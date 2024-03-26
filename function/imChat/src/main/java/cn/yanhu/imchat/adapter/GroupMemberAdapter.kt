package cn.yanhu.imchat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.baselib.R
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.imchat.bean.GroupUserInfo
import cn.yanhu.imchat.databinding.AdapterGroupMemberItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/4
 * desc:
 */
class GroupMemberAdapter : BaseQuickAdapter<GroupUserInfo, GroupMemberAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterGroupMemberItemBinding = AdapterGroupMemberItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: GroupUserInfo?) {
        holder.binding.apply {
            userinfo = item
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: GroupUserInfo?,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val get = payloads[0] as String?

            item?.apply {
                if (TYPE_ADMIN_CHANGE == get) {
                    if (this.isGroupAdmin()) {
                        holder.binding.tvTag.visibility = View.VISIBLE
                        holder.binding.tvTag.text = "管理员"
                    } else {
                        holder.binding.tvTag.visibility = View.INVISIBLE
                    }
                } else if (TYPE_ITEM_LONG_CLICK == get) {
                    if (this.longClick) {
                        holder.binding.vgParent.setBackgroundColor(CommonUtils.getColor(R.color.blackAlpha90))
                    }else{
                        holder.binding.vgParent.setBackgroundColor(CommonUtils.getColor(R.color.white))
                    }
                }

            }

        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    companion object {
        const val TYPE_ADMIN_CHANGE = "adminChange"
        const val TYPE_ITEM_LONG_CLICK = "itemLongClick"
    }
}