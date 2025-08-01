package cn.yanhu.agora.adapter.song

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.bean.SongInfo
import cn.yanhu.agora.databinding.AdapterSongListItemBinding
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2025/1/15
 * desc:
 */
class SongListAdapter : BaseQuickAdapter<SongInfo, SongListAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterSongListItemBinding = AdapterSongListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: SongInfo?) {
        holder.binding.apply {
            tvIndex.text = (position+1).toString()
            songInfo = item
            if (position==0 && isShowStartBtn){
                ivStart.visibility = View.VISIBLE
            }else{
                ivStart.visibility = View.INVISIBLE
            }
            executePendingBindings()
        }
    }


    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    private var isShowStartBtn = false
    fun setIsShowStartBtn(bool: Boolean) {
        isShowStartBtn = bool
    }

}