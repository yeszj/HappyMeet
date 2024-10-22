package cn.huanyuan.sweetlove.ui.wallet.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.bean.WalletRecordInfo
import cn.huanyuan.sweetlove.databinding.AdapterWalletDetailItemBinding
import cn.huanyuan.sweetlove.databinding.AdapterWalletDetailStickyHeadItemBinding
import com.chad.library.adapter4.BaseMultiItemAdapter

/**
 * @author: zhengjun
 * created: 2024/3/8
 * desc:
 */
class WalletDetailAdapter : BaseMultiItemAdapter<WalletRecordInfo>() {
    class VH(
        val binding: AdapterWalletDetailItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class VH2(
        val binding: AdapterWalletDetailStickyHeadItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    init {
        addItemType(TYPE_STICKY_HEAD, object : OnMultiItemAdapterListener<WalletRecordInfo, VH2> {
            override fun onBind(holder: VH2, position: Int, item: WalletRecordInfo?) {
                holder.binding.apply {
                    recordInfo = item
                    executePendingBindings()
                }
            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH2 {
                val binding: AdapterWalletDetailStickyHeadItemBinding =
                    AdapterWalletDetailStickyHeadItemBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                return VH2(binding)
            }
        })
        addItemType(TYPE_CONTENT, object : OnMultiItemAdapterListener<WalletRecordInfo, VH> {
            override fun onBind(holder: VH, position: Int, item: WalletRecordInfo?) {
                holder.binding.apply {
                    recordInfo = item
                    executePendingBindings()
                }
            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH {
                val binding: AdapterWalletDetailItemBinding =
                    AdapterWalletDetailItemBinding.inflate(
                        LayoutInflater.from(context), parent, false
                    )
                return VH(binding)
            }
        }).onItemViewType { position, list ->
            val item = list[position]
            if (TextUtils.isEmpty(item.desc)) {
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