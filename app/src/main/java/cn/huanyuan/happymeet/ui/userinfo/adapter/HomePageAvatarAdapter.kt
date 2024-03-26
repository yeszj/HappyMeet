package cn.huanyuan.happymeet.ui.userinfo.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.databinding.AdapterHomePageAvatarBinding
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2024/3/5
 * desc:
 */
class HomePageAvatarAdapter : BaseQuickAdapter<String, HomePageAvatarAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterHomePageAvatarBinding = AdapterHomePageAvatarBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int, item: String?) {
        holder.binding.apply {
            GlideUtils.load(context, item, ivAvatar)
            if (selectPosition == position){
                ivAvatar.setBorderColor(ColorStateList.valueOf(CommonUtils.getColor(cn.yanhu.baselib.R.color.white)))
            }else{
                ivAvatar.setBorderColor(ColorStateList.valueOf(CommonUtils.getColor(cn.yanhu.baselib.R.color.transparent)))
            }
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int, item: String?, payloads: List<Any>) {
        if (payloads.isNotEmpty()){
            holder.binding.apply {
                if (selectPosition == position){
                    ivAvatar.setBorderColor(ColorStateList.valueOf(CommonUtils.getColor(cn.yanhu.baselib.R.color.white)))
                }else{
                    ivAvatar.setBorderColor(ColorStateList.valueOf(CommonUtils.getColor(cn.yanhu.baselib.R.color.transparent)))
                }
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    private var selectPosition: Int = 0
    fun setSelectPosition(position:Int) {
        val oldPosition = selectPosition
        if (selectPosition!=position){
            selectPosition = position
            notifyItemChanged(oldPosition,true)
            notifyItemChanged(selectPosition,true)
        }

    }
}