package cn.yanhu.agora.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.databinding.AdapterFriendSelectItemBinding
import cn.yanhu.baselib.utils.TextViewDrawableUtils
import cn.yanhu.commonres.bean.SameCityUserInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/2/21
 * desc:
 */
class FriendsSelectAdapter: BaseQuickAdapter<SameCityUserInfo, FriendsSelectAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterFriendSelectItemBinding = AdapterFriendSelectItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: SameCityUserInfo?) {
        holder.binding.apply {
            userinfo = item
            if (item?.isAuth == true){
                TextViewDrawableUtils.setDrawableRight(tvNickName,ContextCompat.getDrawable(context,
                    cn.yanhu.commonres.R.drawable.svg_identify_tag))
            }else{
                TextViewDrawableUtils.setDrawableRight(tvNickName,null)
            }
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: SameCityUserInfo?,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            holder.binding.apply {
                if (position==selectPosition){
                    ivCheck.setImageResource(cn.yanhu.commonres.R.drawable.svg_selected_r20)
                }else{
                    ivCheck.setImageResource(cn.yanhu.commonres.R.drawable.svg_unselected_r20)
                }
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    private var selectPosition: Int = -1

    fun getSelectPosition():Int{
        return selectPosition
    }
    fun setSelectPosition(position: Int) {
        val oldPosition = selectPosition
        if (selectPosition != position) {
            selectPosition = position
            notifyItemChanged(oldPosition, true)
            notifyItemChanged(selectPosition, true)
        }
    }
}