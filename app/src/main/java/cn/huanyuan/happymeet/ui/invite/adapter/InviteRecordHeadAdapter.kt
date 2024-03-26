package cn.huanyuan.happymeet.ui.invite.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.bean.InviteRecordResponse
import cn.huanyuan.happymeet.databinding.AdapterInviteRecordHeadItemBinding
import com.chad.library.adapter4.BaseSingleItemAdapter

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class InviteRecordHeadAdapter : BaseSingleItemAdapter<InviteRecordResponse, InviteRecordHeadAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterInviteRecordHeadItemBinding = AdapterInviteRecordHeadItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, @SuppressLint("RecyclerView") item: InviteRecordResponse?) {
        if (item == null) {
            return
        }
        holder.binding.apply {
            tvTotalCount.text = item.totalInviteCount.toString()
            tvTotalIncome.text = item.totalIncome
        }
    }


    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}