package cn.huanyuan.sweetlove.ui.system

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.ComplaintInfo
import cn.huanyuan.sweetlove.databinding.ActivityFeedbackBinding
import cn.huanyuan.sweetlove.ui.main.tab_my.adapter.EditPhotoAdapter
import cn.huanyuan.sweetlove.ui.system.adapter.ComplaintTypeAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.func.photo.ImageSelectUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.bean.EditPhotoInfo
import cn.yanhu.commonres.manager.ImageSelectManager
import cn.yanhu.commonres.router.RouterPath
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.upload.UploadFileClient
import cn.zj.netrequest.upload.UploadFileProgressListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ThreadUtils
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:投诉与反馈
 */
@Route(path =  RouterPath.ROUTER_REPORT)
class FeedbackActivity : BaseActivity<ActivityFeedbackBinding, SystemViewModel>(
    R.layout.activity_feedback,
    SystemViewModel::class.java
) {

    private val editPhotoAdapter by lazy {
        EditPhotoAdapter(true)
    }

    private val complaintTypeAdapter by lazy { ComplaintTypeAdapter() }

    override fun initData() {
        setStatusBarStyle(false)
        mViewModel.complaintInfo.value = ComplaintInfo()
        mBinding.complaintInfo = mViewModel.complaintInfo.value
        initEditAdapter()
        mBinding.rvType.adapter = complaintTypeAdapter
        val stringArray =
            StringUtils.getStringArray(cn.yanhu.commonres.R.array.complaint_type)
        complaintTypeAdapter.submitList(stringArray.toMutableList())
        complaintTypeAdapter.setOnItemClickListener { _, _, position ->
            val item = complaintTypeAdapter.getItem(position)
            mViewModel.complaintInfo.value?.complaintType = item!!
            complaintTypeAdapter.setSelectPosition(position)
        }
    }

    override fun initListener() {
        super.initListener()
        mBinding.btnCommit.setOnSingleClickListener {
            val complaintInfo = mViewModel.complaintInfo.value
            if (TextUtils.isEmpty(complaintInfo?.complaintType)) {
                showToast("请选择违规类型")
            } else if (TextUtils.isEmpty(complaintInfo?.complaintUserId)) {
                showToast("请输入被投诉人ID")
                mBinding.etId.setShakeAnimation()
            } else if (TextUtils.isEmpty(complaintInfo?.complaintReason)) {
                showToast("请输入投诉原因")
                mBinding.etReason.setShakeAnimation()
            } else if (complaintInfo?.complaintReason!!.length < 5) {
                showToast("投诉原因至少5个字")
                mBinding.etReason.setShakeAnimation()
            } else {
                mViewModel.complaintUser()
            }
        }
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.complaintResultObservable.observe(this) {
            parseState(it, {
                showToast("提交成功")
                finish()
            })
        }
    }

    private fun initEditAdapter() {
        mBinding.rvPhoto.adapter = editPhotoAdapter
        bindItemClickListener()
        setAddIconItem()
    }

    private fun bindItemClickListener() {
        editPhotoAdapter.setOnItemClickListener { adapter, _, position ->
            val item = adapter.getItem(position)
            item?.apply {
                if (TextUtils.isEmpty(this.url)) {
                    toSelectImage()
                } else {
                    preview(item)
                }
            }
        }
        editPhotoAdapter.addOnItemChildClickListener(
            cn.yanhu.dynamic.R.id.iv_delete
        ) { _, view, position ->
            if (view.id == cn.yanhu.dynamic.R.id.iv_delete) {
                photoList.removeAt(position)
                editPhotoAdapter.removeAt(position)
                if (photoList.size == MAX_PIC_NUM - 1) {
                    setAddIconItem()
                }
            }
        }
    }

    private fun toSelectImage() {
        ImageSelectManager.selectPic(
            mContext,
            isCrop = false,
            maxSelectNum = MAX_PIC_NUM - photoList.size,
            type = ImageSelectUtils.TYPE_IMAGE,
            call = object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: java.util.ArrayList<LocalMedia>?) {
                    selectBgCallBack(result)
                }

                override fun onCancel() {
                }

            }
        )
    }


    private val photoList: MutableList<EditPhotoInfo> = mutableListOf()

    @SuppressLint("SetTextI18n")
    private fun selectBgCallBack(selectList: ArrayList<LocalMedia>?) {
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
                uploadFile(editPhotoInfo)
                Log.i("selectPhoto", "文件名: " + it.fileName)
                Log.i("selectPhoto", "文件大小: " + it.size)
                Log.i("selectPhoto", "绝对路径: " + it.availablePath)
            }
        }
    }

    private fun uploadFile(editPhotoInfo: EditPhotoInfo) {
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
                            mViewModel.complaintInfo.value?.picList?.add(url)
                        }, 150)
                    }
                }

                override fun onUploadFail(msg: String) {
                    showToast(msg)
                    photoList.removeIf {
                        it.url == editPhotoInfo.url
                    }
                    editPhotoAdapter.removeAt(editPhotoAdapter.getPositionByItem(editPhotoInfo.url))
                    if (photoList.size == MAX_PIC_NUM - 1) {
                        setAddIconItem()
                    }
                }
            })
    }

    private fun setAddIconItem() {
        editPhotoAdapter.add(EditPhotoInfo(""))
    }

    private fun preview(item: EditPhotoInfo) {
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

    companion object {
        const val MAX_PIC_NUM = 3
    }


}