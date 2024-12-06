package cn.huanyuan.sweetlove.ui.main.tab_msg.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterFriendsTitleItemBinding
import cn.huanyuan.sweetlove.ui.main.tab_msg.friend.FriendRequestRecordActivity
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import com.chad.library.adapter4.BaseSingleItemAdapter

/**
 * @author: zhengjun
 * created: 2024/12/4
 * desc:
 */
class FriendTitleAdapter(private val isFriendList: Boolean = true) :
    BaseSingleItemAdapter<String, FriendTitleAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterFriendsTitleItemBinding = AdapterFriendsTitleItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, item: String?) {
        holder.binding.apply {
            if (isFriendList) {
                tvAll.visibility = View.INVISIBLE
                tvTitle.text = "我的好友(${item})"
            }else{
                tvAll.visibility = View.VISIBLE
                tvAll.setOnSingleClickListener {
                    FriendRequestRecordActivity.lunch(context)
                }
                tvTitle.text = "好友请求(${item})"
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}