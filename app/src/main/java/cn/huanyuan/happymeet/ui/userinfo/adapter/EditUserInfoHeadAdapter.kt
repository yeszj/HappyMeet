package cn.huanyuan.happymeet.ui.userinfo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import cn.huanyuan.happymeet.databinding.AdapterEditUserInfoHeadBinding
import cn.huanyuan.happymeet.ui.main.tab_my.adapter.EditPhotoAdapter
import cn.huanyuan.happymeet.ui.userinfo.edit.EditUserInfoActivity
import cn.yanhu.baselib.func.photo.ImageSelectUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.EditPhotoInfo
import cn.yanhu.commonres.bean.EditUserInfo
import cn.yanhu.commonres.manager.ImageSelectManager
import cn.yanhu.commonres.utils.VideoUtils
import cn.zj.netrequest.upload.UploadFileClient
import cn.zj.netrequest.upload.UploadFileProgressListener
import com.blankj.utilcode.util.ThreadUtils
import com.chad.library.adapter4.BaseSingleItemAdapter
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener

/**
 * @author: zhengjun
 * created: 2024/3/11
 * desc:
 */
class EditUserInfoHeadAdapter(var activity: EditUserInfoActivity) :
    BaseSingleItemAdapter<EditUserInfo, EditUserInfoHeadAdapter.VH>() {
    class VH(
        parent: ViewGroup,
        val binding: AdapterEditUserInfoHeadBinding = AdapterEditUserInfoHeadBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, item: EditUserInfo?) {
        holder.binding.apply {
            if (item == null) {
                return
            }
            var editPhotoAdapter = rvPhoto.tag as EditPhotoAdapter?
            if (editPhotoAdapter == null) {
                editPhotoAdapter = EditPhotoAdapter()
                rvPhoto.tag = editPhotoAdapter
                rvPhoto.adapter = editPhotoAdapter
                editPhotoAdapter.setOnItemClickListener { adapter, _, position ->
                    val editPhotoInfo = adapter.getItem(position)
                    editPhotoInfo?.apply {
                        if (TextUtils.isEmpty(this.url)) {
                            toSelectImage(editPhotoAdapter)
                        } else {
                            preview(this, editPhotoAdapter)
                        }
                    }
                }
            }
            showMyPicData(item.thumbnail, editPhotoAdapter)
            editUserInfo = item
            executePendingBindings()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VH, item: EditUserInfo?, payloads: List<Any>) {
        if (payloads.isNotEmpty()) {
            holder.binding.apply {
                val any = payloads[0] as Int?
                if (any == TYPE_UPDATE_AVATAR) {
                    if (item == null) {
                        return
                    }
                    val portraitEditInfo = item.getPortraitEditInfo()
                    GlideUtils.load(context, portraitEditInfo.url, ivUserPortrait)
                    if (portraitEditInfo.isUploadFinish()) {
                        tvProgress.visibility = View.INVISIBLE
                    } else {
                        tvProgress.visibility = View.VISIBLE
                        tvProgress.text = "${portraitEditInfo.progress}%"
                    }
                }
                executePendingBindings()
            }
        }
    }

    private fun preview(item: EditPhotoInfo, editPhotoAdapter: EditPhotoAdapter) {
        val url = item.url
        val bgList: MutableList<String> = mutableListOf()
        var selectPosition = 0
        for (i in 0 until editPhotoAdapter.itemCount) {
            val editPhotoInfo = editPhotoAdapter.getItem(i) ?: continue
            if (url == editPhotoInfo.url) {
                selectPosition = i
            }
            bgList.add(editPhotoInfo.url)
        }
        val findViewHolderForAdapterPosition =
            editPhotoAdapter.recyclerView.findViewHolderForAdapterPosition(selectPosition) as EditPhotoAdapter.VH
        DialogUtils.showImageViewerDialog(
            findViewHolderForAdapterPosition.binding.ivPhoto,
            selectPosition,
            bgList
        ) { popupView, _ ->
            try {
                val viewHolder =
                    editPhotoAdapter.recyclerView.findViewHolderForAdapterPosition(selectPosition) as EditPhotoAdapter.VH
                popupView.updateSrcView(viewHolder.binding.ivPhoto)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun toSelectImage(editPhotoAdapter: EditPhotoAdapter) {
        ImageSelectManager.selectPic(
            context as FragmentActivity,
            isCrop = false,
            maxSelectNum = MAX_PIC_NUM - photoList.size,
            type = ImageSelectUtils.TYPE_IMAGE,
            call = object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: java.util.ArrayList<LocalMedia>?) {
                    selectBgCallBack(result, editPhotoAdapter)
                }

                override fun onCancel() {
                }

            }
        )
    }

    @SuppressLint("SetTextI18n")
    private fun selectBgCallBack(
        selectList: ArrayList<LocalMedia>?,
        editPhotoAdapter: EditPhotoAdapter
    ) {
        if (selectList.isNullOrEmpty()) {
            return
        }
        selectList.forEach {
            if (PictureMimeType.isHasImage(it.mimeType) || PictureMimeType.isHasVideo(it.mimeType)) {
                val availablePath = it.availablePath
                val editPhotoInfo =
                    EditPhotoInfo(availablePath, 0, PictureMimeType.isHasVideo(it.mimeType))
                if (photoList.size >= MAX_PIC_NUM) {
                    photoList.removeAt(0)
                    photoList.add(0, editPhotoInfo)
                    editPhotoAdapter[0] = editPhotoInfo
                } else {
                    photoList.add(editPhotoInfo)
                    if (photoList.size >= MAX_PIC_NUM) {
                        editPhotoAdapter.removeAt(editPhotoAdapter.itemCount - 1)
                        editPhotoAdapter.add(editPhotoInfo)
                    } else {
                        editPhotoAdapter.add(editPhotoAdapter.itemCount - 1, editPhotoInfo)
                    }
                }
                uploadFile(editPhotoInfo, editPhotoAdapter)
                Log.i("selectPhoto", "文件名: " + it.fileName)
                Log.i("selectPhoto", "文件大小: " + it.size)
                Log.i("selectPhoto", "绝对路径: " + it.availablePath)
            }
        }
    }

    private fun uploadFile(editPhotoInfo: EditPhotoInfo, editPhotoAdapter: EditPhotoAdapter) {
        activity.hasChange = true
        UploadFileClient.uploadFile(
            editPhotoInfo.url,
            object : UploadFileProgressListener {
                override fun onProgress(hasWrittenLen: Long, totalLen: Long) {
                    ThreadUtils.getMainHandler().post {
                        var progress = (hasWrittenLen * 100 / totalLen).toInt()
                        if (progress == 100) {
                            progress = 99
                        }
                        editPhotoInfo.progress = progress
                        val positionByItem = editPhotoAdapter.getPositionByItem(editPhotoInfo.url)
                        if (positionByItem != -1) {
                            editPhotoAdapter.notifyItemChanged(positionByItem)
                        }
                    }
                }

                override fun onUploadSuccess(url: String) {
                    ThreadUtils.getMainHandler().post {
                        editPhotoInfo.progress = 100
                        val positionByItem = editPhotoAdapter.getPositionByItem(editPhotoInfo.url)
                        if (positionByItem != -1) {
                            editPhotoAdapter.notifyItemChanged(positionByItem)
                        }
                        ThreadUtils.getMainHandler().postDelayed({
                            editPhotoInfo.url = url
                        }, 150)
                    }
                }

                override fun onUploadFail(msg: String) {
                    showToast(msg)
                    photoList.removeIf {
                        it.url == editPhotoInfo.url
                    }
                    editPhotoAdapter.removeAt(editPhotoAdapter.getPositionByItem(editPhotoInfo.url))
                    if (photoList.size == MAX_PIC_NUM-1) {
                        setAddIconItem(editPhotoAdapter)
                    }
                }
            })
    }

    private val photoList: MutableList<EditPhotoInfo> = mutableListOf()
    private fun showMyPicData(backgrounds: List<String>, editPhotoAdapter: EditPhotoAdapter) {
        photoList.clear()

        editPhotoAdapter.submitList(mutableListOf())

        for (i in backgrounds.size - 1 downTo 0) {
            val isVideo = VideoUtils.isVideo(backgrounds[i])
            val editPhotoInfo = EditPhotoInfo(backgrounds[i], isVideo = isVideo)
            photoList.add(0, editPhotoInfo)
            editPhotoAdapter.add(0, editPhotoInfo)
        }
        if (photoList.size < MAX_PIC_NUM) {
            setAddIconItem(editPhotoAdapter)
        }
    }

    private fun setAddIconItem(editPhotoAdapter: EditPhotoAdapter) {
        editPhotoAdapter.add(EditPhotoInfo(""))
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    companion object {
        const val TYPE_UPDATE_DESC = 1
        const val TYPE_UPDATE_AVATAR = 2
        const val MAX_PIC_NUM = 9
    }
}