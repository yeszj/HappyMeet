package cn.huanyuan.sweetlove.ui.history.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterSeenMeHistoryItemBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.widget.spans.Spans
import cn.yanhu.commonres.bean.SeenMeHistoryInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/4
 * desc:
 */
class SeenMeHistoryAdapter : BaseQuickAdapter<SeenMeHistoryInfo, SeenMeHistoryAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterSeenMeHistoryItemBinding = AdapterSeenMeHistoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: SeenMeHistoryInfo?) {
        holder.binding.apply {
            userinfo = item
            isLock = !hasPermission
            if (hasPermission) {
                GlideUtils.load(context, item!!.portrait, imageView = avatarView)
            } else {
                GlideUtils.loadBlurTransPic(context, item!!.portrait, imageView = avatarView)
            }
            val build = Spans.builder().text("来访")
                .text(item.viewCount.toString())
                .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.colorMain))
                .text("次").build()
            tvCount.text = build
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }


    private var hasPermission:Boolean = false
    fun setIsLock(lock: Boolean) {
        hasPermission = lock
    }
}