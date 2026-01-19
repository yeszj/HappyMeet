package cn.huanyuan.sweetlove.ui.userinfo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterEditUserInfoItemBinding
import cn.huanyuan.sweetlove.ui.userinfo.edit.UserParamType
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.TextViewDrawableUtils
import cn.yanhu.commonres.R
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
            if (item?.type == UserParamType.TYPE_ADDRESS.type){
                tvRefreshIp.visibility = View.VISIBLE
                TextViewDrawableUtils.setDrawableRight(tvValue,null)
            }else{
                tvRefreshIp.visibility = View.INVISIBLE
                TextViewDrawableUtils.setDrawableRight(tvValue, ContextCompat.getDrawable(context,R.drawable.ic_right_arrow))
            }
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