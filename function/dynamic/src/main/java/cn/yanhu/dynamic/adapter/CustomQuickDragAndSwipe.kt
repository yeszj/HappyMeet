package cn.yanhu.dynamic.adapter

import android.text.TextUtils
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.dragswipe.QuickDragAndSwipe

/**
 * @author: zhengjun
 * created: 2024/3/6
 * desc:
 */
class CustomQuickDragAndSwipe : QuickDragAndSwipe() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if (recyclerView.adapter is UploadDynamicPicAdapter) {
            val adapter = recyclerView.adapter as UploadDynamicPicAdapter
            viewHolder.bindingAdapterPosition
            val item = adapter.getItem(viewHolder.bindingAdapterPosition)
            if (item!= null && !TextUtils.isEmpty(item.url)) {
                return super.getMovementFlags(recyclerView, viewHolder)
            }
            return makeMovementFlags(0, 0)
        }
        return makeMovementFlags(0, 0)
    }
}