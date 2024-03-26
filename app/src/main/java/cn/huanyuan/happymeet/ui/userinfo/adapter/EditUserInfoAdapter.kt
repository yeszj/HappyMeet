package cn.huanyuan.happymeet.ui.userinfo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.databinding.AdapterEditUserTypeItemBinding
import cn.yanhu.commonres.bean.EditIUserItemInfo
import cn.yanhu.commonres.bean.UserInfoItem
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.BaseSingleItemAdapter

/**
 * @author: zhengjun
 * created: 2024/3/11
 * desc:
 */
class EditUserInfoAdapter(var isShowLoadMore:Boolean = true) : BaseSingleItemAdapter<EditIUserItemInfo, EditUserInfoAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterEditUserTypeItemBinding = AdapterEditUserTypeItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, item: EditIUserItemInfo?) {
        holder.binding.apply {
            if (item == null) {
                return
            }
            if (isShowLoadMore){
                ivMore.visibility = View.VISIBLE
            }else{
                ivMore.visibility = View.GONE
            }
            var editInfoAdapter = rvInfo.tag as EditUserItemAdapter?
            if (editInfoAdapter == null) {
                editInfoAdapter = EditUserItemAdapter()
                editInfoAdapter.setOnItemClickListener(OnItemClickListener { adapter, view, position -> onEditItemClickListener?.onItemClick(adapter,view,position) })
                rvInfo.adapter = editInfoAdapter
                rvInfo.tag = editInfoAdapter
            }
            editInfoAdapter.submitList(item.list)
            tvTitle.text = item.key
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: VH, item: EditIUserItemInfo?, payloads: List<Any>) {
        if (payloads.isNotEmpty()){
            holder.binding.apply {
                ivMore.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    private var onEditItemClickListener:OnEditItemClickListener?=null
    fun registerOnEdiItemClickListener(editItemClickListener: OnEditItemClickListener){
        onEditItemClickListener = editItemClickListener
    }

    interface OnEditItemClickListener{
        fun onItemClick(adapter: BaseQuickAdapter<UserInfoItem, *>,
                        view: View,
                        position: Int)
    }
}