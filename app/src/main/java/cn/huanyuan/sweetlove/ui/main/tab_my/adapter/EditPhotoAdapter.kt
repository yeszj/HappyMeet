package cn.huanyuan.sweetlove.ui.main.tab_my.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.sweetlove.databinding.AdapterUploadPhotoItemBinding
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.commonres.bean.EditPhotoInfo
import cn.yanhu.commonres.manager.ImageThumbUtils
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * @author: zhengjun
 * created: 2023/6/14
 * desc:
 */
class EditPhotoAdapter(private var isShowDelete:Boolean = false) :
    BaseQuickAdapter<EditPhotoInfo, EditPhotoAdapter.VH>(ArrayList()) {

    class VH(
        parent: ViewGroup,
        val binding: AdapterUploadPhotoItemBinding = AdapterUploadPhotoItemBinding.inflate(
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
                ivDelete.visibility = View.INVISIBLE
                ivPhoto.setImageResource(cn.yanhu.commonres.R.drawable.svg_pic_add)
                ivVideo.visibility = View.INVISIBLE
                tvProgress.visibility = View.INVISIBLE
            }else{
                if (isShowDelete){
                    ivDelete.visibility = View.VISIBLE
                }else{
                    ivDelete.visibility = View.INVISIBLE
                }
                if (item.isNetUrl()){
                    GlideUtils.load(context,ImageThumbUtils.getThumbUrl(item.url),ivPhoto, placeholderId = -1)
                }else{
                    GlideUtils.load(context,item.url,ivPhoto, placeholderId = -1)
                }
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
}