package cn.huanyuan.happymeet.ui.main.tab_my.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.databinding.AdapterMineMenuItemBinding
import cn.huanyuan.happymeet.databinding.AdapterMyMenuDividerBinding
import cn.yanhu.commonres.bean.MineMenuBean
import com.chad.library.adapter4.BaseMultiItemAdapter

/**
 * @author: zhengjun
 * created: 2024/2/18
 * desc:
 */
class MineMenuAdapter : BaseMultiItemAdapter<MineMenuBean>(mutableListOf()) {

    class VH(
        val binding: AdapterMineMenuItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class VH2(
        val binding: AdapterMyMenuDividerBinding
    ) : RecyclerView.ViewHolder(binding.root)

    init {
        addItemType(TYPE_MENU, object : OnMultiItemAdapterListener<MineMenuBean, VH> {
            override fun onBind(holder: VH, position: Int, item: MineMenuBean?) {
                holder.binding.apply {
                    this.item = item
                    executePendingBindings()
                }
            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH {
                val binding: AdapterMineMenuItemBinding = AdapterMineMenuItemBinding.inflate(
                    LayoutInflater.from(context), parent, false
                )
                return VH(binding)
            }
        })
        addItemType(TYPE_DIVIDER, object : OnMultiItemAdapterListener<MineMenuBean, VH2> {
            override fun onBind(holder: VH2, position: Int, item: MineMenuBean?) {
            }

            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int): VH2 {
                val binding: AdapterMyMenuDividerBinding = AdapterMyMenuDividerBinding.inflate(
                    LayoutInflater.from(context), parent, false
                )
                return VH2(binding)
            }

            override fun isFullSpanItem(itemType: Int): Boolean {
                return true
            }
        }).onItemViewType { position, list ->
                val menuBean = list[position]
                if (menuBean.type == MineMenuBean.TYPE_DIVIDER) {
                    TYPE_DIVIDER
                } else {
                    TYPE_MENU
                }
            }
    }

    companion object {
        const val TYPE_MENU = 1
        const val TYPE_DIVIDER = 2
    }

}