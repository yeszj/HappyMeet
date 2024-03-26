package cn.yanhu.dynamic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.dynamic.databinding.AdapterLikeAvatarItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: witness
 * created: 2022/12/12
 * desc:
 */
class LikeUserAvatarAdapter :
    BaseQuickAdapter<BaseUserInfo, LikeUserAvatarAdapter.VH>() {

    class VH(
        parent: ViewGroup,
        val binding: AdapterLikeAvatarItemBinding = AdapterLikeAvatarItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)


    override fun onBindViewHolder(holder: VH, position: Int, item: BaseUserInfo?) {
        holder.binding.apply {
            if (item == null) {
                return
            }
            GlideUtils.load(context, item.portrait, ivAvatar)
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}