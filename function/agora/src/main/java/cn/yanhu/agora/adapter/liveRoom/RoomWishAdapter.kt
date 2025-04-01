package cn.yanhu.agora.adapter.liveRoom

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.commonres.bean.WishInfo
import cn.yanhu.agora.databinding.AdapterWishItemBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.widget.spans.Spans
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2025/3/27
 * desc:
 */
class RoomWishAdapter(private val owner: Boolean) :
    BaseQuickAdapter<WishInfo, RoomWishAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterWishItemBinding = AdapterWishItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: WishInfo?) {
        holder.binding.apply {
            this.wishInfo = item ?: return
            this.isOwner = owner
            val build =
                Spans.builder().text(item.finishNum.toString())
                    .color(CommonUtils.getColor(cn.yanhu.baselib.R.color.colorMain))
                    .text("/" + item.sunNum.toString())
                    .build()
            tvCount.text = build
            val portraitList = item.portraitList
            if (portraitList.size < 5) {
                var total = item.sunNum
                if (total > 5) {
                    total = 5
                }
                for (i in 0 until total - portraitList.size) {
                    portraitList.add("")
                }
            }

            if (rvAvatar.tag == null) {
                val wishAvatarAdapter = WishAvatarAdapter()
                rvAvatar.adapter = wishAvatarAdapter
                rvAvatar.tag = wishAvatarAdapter
                val linearLayoutManager = LinearLayoutManager(context)
                linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                linearLayoutManager.stackFromEnd = true//列表再底部开始展示
                //linearLayoutManager.reverseLayout = true//列表翻转
                rvAvatar.layoutManager = linearLayoutManager
                rvAvatar.addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect,
                        view: View,
                        parent: RecyclerView,
                        state: RecyclerView.State,
                    ) {
                        super.getItemOffsets(outRect, view, parent, state)
                        if (parent.getChildLayoutPosition(view) != wishAvatarAdapter.itemCount - 1) {
                            outRect.right =
                                CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_m_8)
                        }
                    }
                })
                wishAvatarAdapter.submitList(portraitList)
            } else {
                val wishAvatarAdapter = rvAvatar.tag as WishAvatarAdapter
                wishAvatarAdapter.submitList(portraitList)
            }
            rvAvatar.scrollToPosition(0)
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}