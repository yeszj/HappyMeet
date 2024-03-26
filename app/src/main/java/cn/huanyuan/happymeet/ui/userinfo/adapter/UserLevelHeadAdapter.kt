package cn.huanyuan.happymeet.ui.userinfo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.bean.UserLevelResponse
import cn.huanyuan.happymeet.databinding.AdapterUserLevelHeadBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.widget.spans.Spans
import com.chad.library.adapter4.BaseSingleItemAdapter

/**
 * @author: zhengjun
 * created: 2024/3/5
 * desc:
 */
class UserLevelHeadAdapter :
    BaseSingleItemAdapter<UserLevelResponse, UserLevelHeadAdapter.VH>() {
    class VH(var binding: AdapterUserLevelHeadBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, item: UserLevelResponse?) {
        holder.binding.apply {
            if (item == null) {
                return
            }
            userLevel = item
            val build =
                Spans.builder().text("已解锁").text(item.hasLockCount.toString()).color(CommonUtils.getColor(cn.yanhu.baselib.R.color.white))
                    .text("/${item.totalPrivilegeCount}项")
                    .build()
            tvHasLockDesc.text = build
            executePendingBindings()
        }
    }

    override fun isFullSpanItem(itemType: Int): Boolean {
        return true
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        val binding =
            AdapterUserLevelHeadBinding.inflate(LayoutInflater.from(context), parent, false)
        return VH(binding)
    }
}