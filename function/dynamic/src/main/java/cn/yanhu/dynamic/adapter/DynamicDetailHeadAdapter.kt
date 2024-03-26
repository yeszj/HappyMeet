package cn.yanhu.dynamic.adapter

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.dynamic.bean.DynamicInfo
import cn.yanhu.dynamic.databinding.AdapterDynamicDetailHeadBinding
import com.chad.library.adapter4.BaseSingleItemAdapter

/**
 * @author: zhengjun
 * created: 2024/3/6
 * desc:
 */
class DynamicDetailHeadAdapter : BaseSingleItemAdapter<DynamicInfo, DynamicDetailHeadAdapter.VH>() {
    class VH(var binding: AdapterDynamicDetailHeadBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, item: DynamicInfo?) {
        holder.binding.apply {
            momentInfo = item
            var likeUserAvatarAdapter = rvAvatar.tag as LikeUserAvatarAdapter?
            if (likeUserAvatarAdapter == null) {
                likeUserAvatarAdapter = LikeUserAvatarAdapter()
                rvAvatar.tag = likeUserAvatarAdapter
                val linearLayoutManager = LinearLayoutManager(context)
                linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                linearLayoutManager.stackFromEnd = true//列表再底部开始展示
                linearLayoutManager.reverseLayout = true//列表翻转
                rvAvatar.layoutManager = linearLayoutManager
                rvAvatar.addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect,
                        view: View,
                        parent: RecyclerView,
                        state: RecyclerView.State,
                    ) {
                        super.getItemOffsets(outRect, view, parent, state)
                        if (parent.getChildLayoutPosition(view) != likeUserAvatarAdapter.items.size - 1) {
                            outRect.left = CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_m_5)
                        }
                    }
                })
                rvAvatar.adapter = likeUserAvatarAdapter
            }
            likeUserAvatarAdapter.submitList(item?.sectionInfRes)
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        val binding =
            AdapterDynamicDetailHeadBinding.inflate(LayoutInflater.from(context), parent, false)
        return VH(binding)
    }
}