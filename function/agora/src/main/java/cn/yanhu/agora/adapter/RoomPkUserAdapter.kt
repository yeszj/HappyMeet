package cn.yanhu.agora.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.bean.PkSeatUserInfo
import cn.yanhu.agora.databinding.AdapterHasSelectPkUserItemBinding
import cn.yanhu.baselib.utils.GlideUtils
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2025/9/4
 * desc:
 */
class RoomPkUserAdapter : BaseQuickAdapter<PkSeatUserInfo, RoomPkUserAdapter.VH>() {
    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): VH {
        return VH(parent)
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: PkSeatUserInfo?
    ) {
        holder.binding.apply {
            userInfo = item
            if (TextUtils.isEmpty(item?.portrait)){
                ivAvatar.setImageResource(cn.yanhu.commonres.R.drawable.svg_circle_add)
            }else{
                GlideUtils.load(context, item?.portrait, ivAvatar)
            }
            executePendingBindings()
        }

    }

    fun hasAddItem(): Boolean {
        items.forEach {
            if (TextUtils.isEmpty(it.portrait)){
                return true
            }
        }
        return false
    }

    class VH(
        parent: ViewGroup,
        val binding: AdapterHasSelectPkUserItemBinding = AdapterHasSelectPkUserItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)
}