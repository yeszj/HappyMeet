package cn.yanhu.dynamic.ui.activity

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.func.photo.ImageSelectUtils
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.widget.SimpleTextWatcher
import cn.yanhu.commonres.bean.EditPhotoInfo
import cn.yanhu.commonres.manager.ImageSelectManager
import cn.yanhu.commonres.router.RouterPath
import cn.yanhu.dynamic.R
import cn.yanhu.dynamic.adapter.CustomQuickDragAndSwipe
import cn.yanhu.dynamic.adapter.UploadDynamicPicAdapter
import cn.yanhu.dynamic.databinding.ActivityPubDynamicBinding
import cn.yanhu.dynamic.ui.DynamicViewModel
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.upload.UploadFileClient
import cn.zj.netrequest.upload.UploadFileProgressListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ThreadUtils
import com.chad.library.adapter4.dragswipe.listener.OnItemDragListener
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.pcl.sdklib.sdk.location.LocationUtils
import com.tencent.map.geolocation.TencentLocation


/**
 * @author: zhengjun
 * created: 2024/3/6
 * desc:
 */
@Route(path = RouterPath.ROUTER_PUB_DYNAMIC)
class PubDynamicActivity : BaseActivity<ActivityPubDynamicBinding, DynamicViewModel>(
    R.layout.activity_pub_dynamic,
    DynamicViewModel::class.java
) {
    private var currentLocation: TencentLocation? = null
    private val editPhotoAdapter by lazy {
        UploadDynamicPicAdapter()
    }
    private val photoList: MutableList<EditPhotoInfo> = mutableListOf()
    override fun initData() {
        setStatusBarStyle(false)
        initPhotoAdapter()
        bindItemClickListener()
        bindItemChildClickListener()
        addInputChangeListener()
    }

    private fun initPhotoAdapter() {
        mBinding.rvPhoto.adapter = editPhotoAdapter
        setAddIconItem()
        val quickDragAndSwipe = CustomQuickDragAndSwipe()
            .setItemViewSwipeEnabled(false)
            .setDragMoveFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) //可进行上下拖动，交换位置。 ItemTouchHelper.LEFT 允许向左拖动，ItemTouchHelper.RIGHT 允许向右拖动
        quickDragAndSwipe.attachToRecyclerView(mBinding.rvPhoto)
            .setItemDragListener(onItemDragListener)
            .setDataCallback(editPhotoAdapter)
    }

    private val onItemDragListener = object : OnItemDragListener {
        override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder?, pos: Int) {
            viewHolder?.apply {
                viewHolder.itemView.scaleX = 1.05f
                viewHolder.itemView.scaleY = 1.05f
            }
        }

        override fun onItemDragMoving(
            source: RecyclerView.ViewHolder,
            from: Int,
            target: RecyclerView.ViewHolder,
            to: Int
        ) {
        }

        override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder, pos: Int) {
            viewHolder.itemView.scaleX = 1.0f
            viewHolder.itemView.scaleY = 1.0f
        }
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
    }

    private fun bindItemChildClickListener() {
        editPhotoAdapter.addOnItemChildClickListener(
            R.id.iv_delete
        ) { _, view, position ->
            if (view.id == R.id.iv_delete) {
                val size: Int = if (hasVideo()) {
                    0
                } else {
                    8
                }
                photoList.removeAt(position)
                editPhotoAdapter.removeAt(position)
                setPubEnable(
                    photoList.size > 0 || !TextUtils.isEmpty(
                        mBinding.etContent.text.toString().trim()
                    )
                )
                if (photoList.size == size) {
                    setAddIconItem()
                }
            }
        }
    }


    private fun hasVideo(): Boolean {
        photoList.forEach {
            if (it.isVideo) {
                return true
            }
        }
        return false
    }


    private fun addInputChangeListener() {
        mBinding.etContent.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!TextUtils.isEmpty(s)) {
                    setPubEnable(true)
                } else {
                    setPubEnable(photoList.size > 0)
                }
            }

            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                if (TextUtils.isEmpty(s)) {
                    mBinding.tvSize.text = "0/400"
                } else {
                    mBinding.tvSize.text = "${s.toString().trim().length}/400"
                }
            }
        })
    }

    private fun toSelectImage() {
        ImageSelectManager.selectPic(
            mContext,
            isCrop = false,
            maxSelectNum = 9 - photoList.size,
            type = if (photoList.size <= 0) ImageSelectUtils.TYPE_ALL else ImageSelectUtils.TYPE_IMAGE,
            call = object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: java.util.ArrayList<LocalMedia>?) {
                    selectBgCallBack(result)
                }

                override fun onCancel() {
                }

            }
        )
    }

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
                photoList.add(editPhotoInfo)
                if (photoList.size >= 9 || PictureMimeType.isHasVideo(it.mimeType)) {
                    editPhotoAdapter.removeAt(editPhotoAdapter.itemCount - 1)
                    editPhotoAdapter.add(editPhotoInfo)
                } else {
                    editPhotoAdapter.add(editPhotoAdapter.itemCount - 1, editPhotoInfo)
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
                        }, 150)
                        setPubEnable(true)
                    }
                }

                override fun onUploadFail(msg: String) {
                    showToast(msg)
                    photoList.removeIf {
                        it.url == editPhotoInfo.url
                    }
                    editPhotoAdapter.removeAt(editPhotoAdapter.getPositionByItem(editPhotoInfo.url))
                    if (photoList.size == 8) {
                        setAddIconItem()
                    }
                }
            })
    }

    private fun setPubEnable(isEnable: Boolean) {
        mBinding.btnPub.isEnabled = isEnable
        if (isEnable) {
            mBinding.btnPub.alpha = 1f
        } else {
            mBinding.btnPub.alpha = 0.5f
        }
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
            if (editPhotoInfo.isNetUrl()) {
                if (url == editPhotoInfo.url) {
                    selectPosition = i
                }
                bgList.add(editPhotoInfo.url)
            }
        }
        if (item.isNetUrl()) {
            //PreViewActivity.lunch(mContext, bgList, selectPosition, AppCacheManager.userId)
        }
    }

    override fun initListener() {
        super.initListener()
        mBinding.ivBack.setOnSingleClickListener {
            DialogUtils.showConfirmDialog("确定要放弃编辑吗?", onCancelListener = {
                finish()
            }, cancel = "放弃", confirm = "继续编辑")
        }
        mBinding.btnPub.setOnSingleClickListener {
            startPup()
        }
        mBinding.ivDelete.setOnSingleClickListener {
            clearLocation()
        }
        mBinding.tvLocation.setOnSingleClickListener {
            getLocation()
        }
        mBinding.ivLocation.setOnSingleClickListener { getLocation() }
    }

    private fun startPup() {
        val content = mBinding.etContent.text.toString().trim()
        val list = mutableListOf<String>()
        editPhotoAdapter.items.forEach {
            if (!it.isNetUrl()) {
                showToast("图片正在上传，请稍后重试")
                return
            }
            list.add(it.url)
        }
        mViewModel.pubDynamic(currentLocation?.city, content,GsonUtils.toJson(list))
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.pubDynamicObserver.observe(this){
            parseState(it,{
                showToast("发布成功")
                finish()
            })
        }
    }

    private fun clearLocation() {
        View.INVISIBLE.also { mBinding.ivDelete.visibility = it }
        mBinding.tvLocation.text = "添加位置"
        mBinding.tvLocation.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.color6))
        mBinding.ivLocation.setImageResource(cn.yanhu.commonres.R.drawable.svg_location_gray)
    }

    private fun getLocation() {
        LocationUtils.getTencentLocation(mContext, object : LocationUtils.OnLocationResultListener {
            override fun onLocationResult(aMapLocation: TencentLocation?) {
                if (aMapLocation != null) {
                    currentLocation = aMapLocation
                    mBinding.ivDelete.visibility = View.VISIBLE
                    mBinding.tvLocation.text = aMapLocation.city
                    mBinding.tvLocation.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.locationTagColor))
                    mBinding.ivLocation.setImageResource(cn.yanhu.commonres.R.drawable.svg_location_blue)
                }
            }
        })
    }
}