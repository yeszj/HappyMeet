package cn.yanhu.agora.adapter.liveRoom

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.databinding.AdapterWishUserItemBinding
import cn.yanhu.commonres.bind.loadImage
import com.chad.library.adapter4.BaseQuickAdapter
import cn.yanhu.agora.R

/**
 * @author: zhengjun
 * created: 2024/2/19
 * desc:
 */
class WishAvatarAdapter: BaseQuickAdapter<String, WishAvatarAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterWishUserItemBinding = AdapterWishUserItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: String?) {
        holder.binding.apply {
            if (TextUtils.isEmpty(item)){
                ivAvatar.setBorderWidth(com.zj.dimens.R.dimen.dp_0)
                ivAvatar.setImageResource(R.drawable.svg_avatar_empty_wish)
            }else{
                ivAvatar.setBorderWidth(com.zj.dimens.R.dimen.dp_1)
                loadImage(ivAvatar,item)
            }
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}