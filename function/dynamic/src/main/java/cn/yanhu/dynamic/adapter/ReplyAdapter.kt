package cn.yanhu.dynamic.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.dynamic.MomentContentBindUtil
import cn.yanhu.dynamic.bean.DiscussInfo
import cn.yanhu.dynamic.databinding.AdapterReplyItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2023/6/30
 * desc:
 */
class ReplyAdapter : BaseQuickAdapter<DiscussInfo, ReplyAdapter.VH>(
) {

    class VH(
        parent: ViewGroup,
        val binding: AdapterReplyItemBinding = AdapterReplyItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)


    private fun AdapterReplyItemBinding.bindContent(item: DiscussInfo) {
        if (tvContent.tag == null || tvContent.tag != item.info) {
            tvContent.tag = item.info
            MomentContentBindUtil.setTextEndTextSpan(
                tvContent,
                item.info,
                tvMore,
                user = item.commentUser
            )
        }
    }


    private fun AdapterReplyItemBinding.bindUserInfo(item: DiscussInfo) {
        val user = BaseUserInfo()
        user.portrait = item.portrait
        user.userId = item.userId
        avatarView.setUserAvatar(user)
        tvName.text = item.nickName
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: DiscussInfo?) {
        holder.binding.apply {
            if (item==null){
                return
            }
            if (item.isAuthorComment) {
                tvAuthor.visibility = View.VISIBLE
            } else {
                tvAuthor.visibility = View.GONE
            }
            userAge.setUserAge(item.age,item.gender)
            bindUserInfo(item)
            if (TextUtils.isEmpty(item.province)){
                tvTime.text = item.commentTime
            }else{
                tvTime.text = "${item.commentTime} · ${item.province}"
            }
            bindContent(item)

            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}