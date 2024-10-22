package cn.huanyuan.sweetlove.ui.setting.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterAppCheckHeadItemBinding
import cn.yanhu.baselib.utils.SystemUtils
import com.chad.library.adapter4.BaseSingleItemAdapter

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class AppCheckHeadAdapter : BaseSingleItemAdapter<Int, AppCheckHeadAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterAppCheckHeadItemBinding = AdapterAppCheckHeadItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    private var oldScore:Int = 0
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, @SuppressLint("RecyclerView") item: Int?) {
        if (item == null) {
            return
        }
        holder.binding.apply {
            tvDeviceName.text = "当前设备 ${SystemUtils.getSystemModel()} ${
                SystemUtils.encodeHeadInfo(
                    SystemUtils.getDeviceName(context)
                )
            }"
            tvScore.setNumberString(oldScore.toString(),item.toString())
            oldScore = item
            if (item < 50) {
                tvScoreDesc.text = "急需优化"
            } else if (item == 100) {
                tvScoreDesc.text = "无需优化"
            } else {
                tvScoreDesc.text = "建议优化"
            }
        }
    }


    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}