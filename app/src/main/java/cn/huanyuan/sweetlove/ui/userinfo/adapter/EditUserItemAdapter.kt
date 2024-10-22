package cn.huanyuan.sweetlove.ui.userinfo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterEditUserInfoItemBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.commonres.bean.UserInfoItem
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/11
 * desc:
 */
class EditUserItemAdapter : BaseQuickAdapter<UserInfoItem, EditUserItemAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterEditUserInfoItemBinding = AdapterEditUserInfoItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: UserInfoItem?) {
        holder.binding.apply {
            itemInfo = item
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: UserInfoItem?,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()){
            holder.binding.apply {
                tvValue.text = item?.value
                tvValue.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.fontGrayColor))
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}