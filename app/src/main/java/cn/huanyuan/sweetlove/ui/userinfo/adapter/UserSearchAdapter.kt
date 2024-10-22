package cn.huanyuan.sweetlove.ui.userinfo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterSearchUserItemBinding
import cn.yanhu.commonres.bean.UserDetailInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/2/21
 * desc:
 */
class UserSearchAdapter: BaseQuickAdapter<UserDetailInfo, UserSearchAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterSearchUserItemBinding = AdapterSearchUserItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: UserDetailInfo?) {
        holder.binding.apply {
            userinfo = item
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