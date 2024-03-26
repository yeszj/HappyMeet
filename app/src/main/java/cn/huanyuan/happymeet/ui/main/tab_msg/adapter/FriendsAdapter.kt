package cn.huanyuan.happymeet.ui.main.tab_msg.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.databinding.AdapterFriendItemBinding
import cn.yanhu.baselib.utils.TextViewDrawableUtils
import cn.yanhu.commonres.bean.SameCityUserInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/2/21
 * desc:
 */
class FriendsAdapter: BaseQuickAdapter<SameCityUserInfo, FriendsAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterFriendItemBinding = AdapterFriendItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: SameCityUserInfo?) {
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
            if (item?.isAuth == true){
                TextViewDrawableUtils.setDrawableRight(tvNickName,ContextCompat.getDrawable(context,
                    cn.yanhu.commonres.R.drawable.svg_identify_tag))
            }else{
                TextViewDrawableUtils.setDrawableRight(tvNickName,null)
            }
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}