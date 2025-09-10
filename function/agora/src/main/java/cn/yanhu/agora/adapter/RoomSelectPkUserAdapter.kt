package cn.yanhu.agora.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.agora.bean.PkSeatUserInfo
import cn.yanhu.agora.databinding.AdapterHasSelectPkUserItemBinding
import cn.yanhu.agora.databinding.AdapterSelectPkUserItemBinding
import com.chad.library.adapter4.BaseQuickAdapter
import androidx.core.graphics.toColorInt
import cn.yanhu.baselib.utils.CommonUtils
import com.zj.dimens.R

/**
 * @author: zhengjun
 * created: 2025/9/4
 * desc:
 */
class RoomSelectPkUserAdapter(val isSelectRedUser: Boolean) : BaseQuickAdapter<PkSeatUserInfo, RoomSelectPkUserAdapter.VH>() {
    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): VH {
        return VH(parent)
    }

    override fun onBindViewHolder(
        holder: VH,
        position: Int,
        item: PkSeatUserInfo?
    ) {
        holder.binding.apply {
            userInfo = item
            if (item?.selectStatus == 1){
                if (isSelectRedUser){
                    ivAvatar.setBorderWidth(R.dimen.dp_2)
                    ivAvatar.setBorderColor(ColorStateList.valueOf("#FF2A68".toColorInt()))
                }else{
                    ivAvatar.setBorderWidth(0f)
                }
            }else if (item?.selectStatus==2){
                if (isSelectRedUser){
                    ivAvatar.setBorderWidth(0f)
                }else{
                    ivAvatar.setBorderWidth(R.dimen.dp_2)
                    ivAvatar.setBorderColor(ColorStateList.valueOf("#288DFF".toColorInt()))
                }
            }else{
                ivAvatar.setBorderWidth(0f)
            }
            executePendingBindings()
        }
    }


    class VH(
        parent: ViewGroup,
        val binding: AdapterSelectPkUserItemBinding = AdapterSelectPkUserItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    fun getSelectUser(): MutableList<PkSeatUserInfo>{
        return if (isSelectRedUser){
            getSelectRedUser()
        }else{
            getSelectBlueUser()
        }
    }

    fun getSelectRedUser(): MutableList<PkSeatUserInfo>{
        var list = mutableListOf<PkSeatUserInfo>()
        items.forEach {
            if (it.selectStatus==1){
                list.add(it)
            }
        }
        return list
    }
    fun getSelectBlueUser(): MutableList<PkSeatUserInfo>{
        var list = mutableListOf<PkSeatUserInfo>()
        items.forEach {
            if (it.selectStatus==2){
                list.add(it)
            }
        }
        return list
    }


    fun getSelectCount(): Int{
        return if (isSelectRedUser){
            getSelectRedUserCount()
        }else{
            getSelectBlueUserCount()
        }
    }

    fun getSelectRedUserCount(): Int{
        var count = 0
        items.forEach {
            if (it.selectStatus==1){
                count++
            }
        }
        return count
    }

    fun getSelectBlueUserCount(): Int{
        var count = 0
        items.forEach {
            if (it.selectStatus==2){
                count++
            }
        }
        return count
    }
}