package cn.huanyuan.sweetlove.ui.main.tab_samecity.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterSameCityUserItemBinding
import cn.yanhu.commonres.bean.SameCityUserInfo
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
//            val thumbnail = item?.thumbnail?.toMutableList()
//            val take = thumbnail?.take(3)
//            if (!take.isNullOrEmpty()) {
//                picGridLayout.visibility = View.VISIBLE
//                picGridLayout.setImageUrls(take)
//                picGridLayout.setNineGridImgListener { index, imageViewList ->
//                    val imageView = imageViewList[index]
//                    DialogUtils.showImageViewerDialog(imageView,index, take.toMutableList()
//                    ) { popupView, position ->
//                        try {
//                            val ivBg2 = imageViewList[position]
//                            popupView.updateSrcView(ivBg2)
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//                }
//            } else {
//                picGridLayout.visibility = View.GONE
//            }
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }
}