package cn.yanhu.agora.adapter.statistic

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.bean.LiveRecordResponse
import cn.yanhu.agora.databinding.AdapterMyLiveRecordItemBinding
import cn.yanhu.agora.databinding.AdapterMyLiveStickyHeadItemBinding
import com.chad.library.adapter4.BaseMultiItemAdapter

/**
 * @author: zhengjun
 * created: 2024/12/23
 * desc:
 */
class LiveRecordAdapter :
    BaseMultiItemAdapter<LiveRecordResponse.RecordInfo>() {
    class VH(
        val binding: AdapterMyLiveRecordItemBinding
    ) : RecyclerView.ViewHolder(binding.root)
    class VH2(
        val binding: AdapterMyLiveStickyHeadItemBinding
    ) : RecyclerView.ViewHolder(binding.root)
    init {
        addItemType(TYPE_STICKY_HEAD, object : OnMultiItemAdapterListener<LiveRecordResponse.RecordInfo, VH2> {
            override fun onBind(holder: VH2, position: Int, item: LiveRecordResponse.RecordInfo?) {
                holder.binding.apply {
                    recordInfo = item
                    executePendingBindings()
                }
            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH2 {
                val binding: AdapterMyLiveStickyHeadItemBinding =
                    AdapterMyLiveStickyHeadItemBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                return VH2(binding)
            }
        })
        addItemType(TYPE_CONTENT, object : OnMultiItemAdapterListener<LiveRecordResponse.RecordInfo, VH> {
            override fun onBind(holder: VH, position: Int, item: LiveRecordResponse.RecordInfo?) {
                holder.binding.apply {
                    recordInfo = item
                    executePendingBindings()
                }
            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH {
                val binding: AdapterMyLiveRecordItemBinding =
                    AdapterMyLiveRecordItemBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                return VH(binding)
            }
        }).onItemViewType { position, list ->
            val item = list[position]
            if (TextUtils.isEmpty(item.date)) {
                TYPE_CONTENT
            } else {
                TYPE_STICKY_HEAD
            }
        }

    }

    companion object {
        const val TYPE_STICKY_HEAD = 1
        const val TYPE_CONTENT = 2
    }
}