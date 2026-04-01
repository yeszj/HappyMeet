package cn.huanyuan.sweetlove.ui.event.cp

import android.R.attr.data
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterHeartCpUserInviteItemBinding
import cn.huanyuan.sweetlove.databinding.AdapterRoseRechargeItemBinding
import cn.yanhu.agora.databinding.AdapterLiveRoomOnlineUserItemBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.commonres.R
import cn.yanhu.commonres.bean.RoomDetailInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/10/17
 * desc:
 */
class InviteUserAdapter : BaseQuickAdapter<UserDetailInfo, InviteUserAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterHeartCpUserInviteItemBinding = AdapterHeartCpUserInviteItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: UserDetailInfo?) {
        holder.binding.apply {
            userinfo = item
            changeSelect(position)
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(
        holder: VH, position: Int, item: UserDetailInfo?, payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            holder.binding.apply {
                changeSelect(position)
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    private fun AdapterHeartCpUserInviteItemBinding.changeSelect(position: Int) {
        if (selectPosition == position) {
            ivSelect.setImageResource(R.drawable.svg_selected_r20)
        } else {
            ivSelect.setImageResource(R.drawable.svg_unselected_r20)
        }
    }
    private var selectPosition: Int = -1
    fun setSelectPosition(position: Int) {
        if (selectPosition != position) {
            val oldPosition = selectPosition
            selectPosition = position
            notifyItemChanged(oldPosition, true)
            notifyItemChanged(selectPosition, true)
        }
    }

    fun getSelectUser(): UserDetailInfo? {
        return if (selectPosition >= 0 && selectPosition < itemCount) {
            getItem(selectPosition)
        } else {
            null
        }
    }

}