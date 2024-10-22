package cn.huanyuan.sweetlove.ui.userinfo.dressUp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterMyDressUpItemBinding
import cn.yanhu.commonres.bean.DressUpInfo
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/19
 * desc:
 */
class MyDressUpItemAdapter : BaseQuickAdapter<DressUpInfo, MyDressUpItemAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterMyDressUpItemBinding = AdapterMyDressUpItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: DressUpInfo?) {
        holder.binding.apply {
            itemInfo = item
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: DressUpInfo?,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()){
            holder.binding.apply {
                if (item?.isWear == true){
                    tvStatus.visibility = View.VISIBLE
                }else{
                    tvStatus.visibility = View.INVISIBLE
                }
            }

        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}