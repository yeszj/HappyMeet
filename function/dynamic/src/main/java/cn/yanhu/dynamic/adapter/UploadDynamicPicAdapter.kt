package cn.yanhu.dynamic.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.commonres.bean.EditPhotoInfo
import cn.yanhu.dynamic.databinding.AdapterUploadDynamicPicItemBinding
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.dragswipe.listener.DragAndSwipeDataCallback

/**
 * @author: zhengjun
 * created: 2023/6/14
 * desc:
 */
class UploadDynamicPicAdapter :
    BaseQuickAdapter<EditPhotoInfo, UploadDynamicPicAdapter.VH>(ArrayList()),
    DragAndSwipeDataCallback {

    class VH(
        parent: ViewGroup,
        val binding: AdapterUploadDynamicPicItemBinding = AdapterUploadDynamicPicItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, position: Int, item: EditPhotoInfo?) {
        holder.binding.apply {
            if (item==null){
                return
            }
            val url = item.url
            if (TextUtils.isEmpty(url)){
                ivPhoto.setImageResource(cn.yanhu.commonres.R.drawable.svg_pic_add)
                ivVideo.visibility = View.INVISIBLE
                tvProgress.visibility = View.INVISIBLE
                ivDelete.visibility = View.INVISIBLE
            }else{
                ivDelete.visibility = View.VISIBLE
                GlideUtils.load(context,item.url,ivPhoto, placeholderId = -1)
                if (item.isUploadFinish()){
                    tvProgress.visibility = View.INVISIBLE
                    if (item.isVideo){
                        ivVideo.visibility = View.VISIBLE
                    }else{
                        ivVideo.visibility = View.INVISIBLE
                    }
                }else{
                    ivVideo.visibility = View.INVISIBLE
                    tvProgress.visibility = View.VISIBLE
                    tvProgress.text = "${item.progress}%"
                }
            }
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    fun getPositionByItem(imgPath:String):Int{
        for (i in 0 until itemCount){
            val editPhotoInfo = getItem(i)
            if (editPhotoInfo?.url == imgPath){
                return i
            }
        }
        return -1
    }


    override fun dataMove(fromPosition: Int, toPosition: Int) {
        if (toPosition != itemCount-1){
            move(fromPosition, toPosition)
        }
    }

    override fun dataRemoveAt(position: Int) {
        removeAt(position)
    }

}