package cn.huanyuan.happymeet.ui.userinfo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.bean.LevelPrivilegeInfo
import cn.huanyuan.happymeet.databinding.AdapterUserLevelItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/18
 * desc:
 */
class UserLevelPrivilegeAdapter : BaseQuickAdapter<LevelPrivilegeInfo, UserLevelPrivilegeAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterUserLevelItemBinding = AdapterUserLevelItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: LevelPrivilegeInfo?) {
        holder.binding.apply {
            levelInfo = item
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}