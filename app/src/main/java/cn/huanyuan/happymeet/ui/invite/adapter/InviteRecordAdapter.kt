package cn.huanyuan.happymeet.ui.invite.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.bean.InviteRecordInfo
import cn.huanyuan.happymeet.databinding.AdapterInviteRecordItemBinding
import cn.yanhu.baselib.utils.TextViewDrawableUtils
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class InviteRecordAdapter : BaseQuickAdapter<InviteRecordInfo, InviteRecordAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterInviteRecordItemBinding = AdapterInviteRecordItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: InviteRecordInfo?) {
        holder.binding.apply {
            userinfo = item
            if (item?.isAuth == true){
                TextViewDrawableUtils.setDrawableRight(tvNickName, ContextCompat.getDrawable(context,
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