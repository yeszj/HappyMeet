package cn.yanhu.agora.adapter.liveRoom

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.bean.PkUserInfo
import cn.yanhu.agora.databinding.AdapterPkUserListItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/9/10
 * desc:
 */
class PkUserListAdapter : BaseQuickAdapter<PkUserInfo, PkUserListAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterPkUserListItemBinding = AdapterPkUserListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    private var isRecently = false
    fun setIsRecently(isRecently: Boolean) {
        this.isRecently = isRecently
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: PkUserInfo?) {
        holder.binding.apply {
            this.isRecent = isRecently
            userInfo = item
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}