package cn.huanyuan.sweetlove.ui.main.tab_samecity.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterSameCityUserItemBinding
import cn.yanhu.commonres.bean.SameCityUserInfo
import cn.yanhu.commonres.utils.SVGAUtils
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/2/21
 * desc:
 */
class SameCityUserListAdapter : BaseQuickAdapter<SameCityUserInfo, SameCityUserListAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterSameCityUserItemBinding = AdapterSameCityUserItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: SameCityUserInfo?) {
        holder.binding.apply {
            userinfo = item
            if (item?.roomId != 0) {
                SVGAUtils.loadAssetsSVGAAnim(svgaLive, "play_white.svga")
            } else {
                svgaLive.clear()
            }
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}