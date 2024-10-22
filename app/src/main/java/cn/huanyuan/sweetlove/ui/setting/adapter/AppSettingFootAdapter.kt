package cn.huanyuan.sweetlove.ui.setting.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterAppSettingFootBinding
import com.chad.library.adapter4.BaseSingleItemAdapter

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class AppSettingFootAdapter:BaseSingleItemAdapter<String,AppSettingFootAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterAppSettingFootBinding = AdapterAppSettingFootBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, item: String?) {
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}