package cn.huanyuan.happymeet.ui.main.tab_my

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import android.view.View
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.FrgTabMineBinding
import cn.huanyuan.happymeet.ui.main.MainViewModel
import cn.huanyuan.happymeet.ui.main.tab_my.adapter.EditPhotoAdapter
import cn.huanyuan.happymeet.ui.main.tab_my.adapter.MineMenuAdapter
import cn.yanhu.baselib.base.BaseFragment
import cn.yanhu.baselib.func.photo.ImageSelectUtils
import cn.yanhu.baselib.refresh.IRefreshCallBack
import cn.yanhu.baselib.refresh.RefreshManager
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.commonres.adapter.MyBannerImageAdapter
import cn.yanhu.commonres.bean.BannerBean
import cn.yanhu.commonres.bean.BaseUserInfo
import cn.yanhu.commonres.bean.EditPhotoInfo
import cn.yanhu.commonres.bean.UserDetailInfo
import cn.yanhu.commonres.db.UserInfoDbManager
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.ImageSelectManager
import cn.yanhu.commonres.router.PageIntentUtil
import cn.yanhu.commonres.router.RouteIntent
import cn.yanhu.commonres.utils.VideoUtils
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.upload.UploadFileClient
import cn.zj.netrequest.upload.UploadFileProgressListener
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ThreadUtils
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.youth.banner.indicator.CircleIndicator
import com.youth.banner.listener.OnBannerListener

/**
 * @author: zhengjun
 * created: 2024/2/4
 * desc:
 */
class TabMineFrg : BaseFragment<FrgTabMineBinding, MainViewModel>(
    R.layout.frg_tab_mine,
    MainViewModel::class.java
) {
    private val editPhotoAdapter by lazy {
        EditPhotoAdapter()
    }

    private val mineMenuAdapter by lazy {
        MineMenuAdapter()
    }

    override fun initData() {
        initEditAdapter()
        initMenuAdapter()
        getData()
    }

    override fun lazyLoad() {
    }

    override fun initListener() {
        mBinding.avatarView.setOnSingleClickListener {
            DialogUtils.showImageViewerDialog(
                mBinding.avatarView.getAvatarView(),
                userInfo?.portrait
            )
        }
        mBinding.tvCopyId.setOnSingleClickListener {
            ClipboardUtils.copyText(userInfo?.userId)
            showToast("复制成功")
        }
        mBinding.viewInfo.setOnSingleClickListener {
            RouteIntent.lunchPersonHomePage(AppCacheManager.userId)
        }
    }

    private fun getData() {
        mViewModel.getMyService()
        mViewModel.getMyPageInfo()
    }

    override fun initRefresh() {
        super.initRefresh()
        RefreshManager.getInstance()
            .initRefresh(mContext, false, mBinding.refreshLayout, object : IRefreshCallBack {
                override fun onRefresh() {
                    getData()
                    endRefreshing(mBinding.refreshLayout)
                }

            })
    }

    override fun onReload() {
        super.onReload()
        getData()
    }


    private fun initEditAdapter() {
        mBinding.rvPhoto.adapter = editPhotoAdapter
        bindItemClickListener()
    }

    private fun initMenuAdapter() {
        mBinding.rvMenu.adapter = mineMenuAdapter
        mineMenuAdapter.setOnItemClickListener { _, _, position ->
            val item = mineMenuAdapter.getItem(position)
            item?.apply {
                PageIntentUtil.url2Page(mContext, item.url)
            }
        }
    }

    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        onGetUserInfoListener()
        onGetMyServiceListener()
    }

    private fun onGetMyServiceListener() {
        mViewModel.myServiceObservable.observe(this) { it ->
            parseState(it, {
                mineMenuAdapter.submitList(it)
            })
        }
    }

    private var userInfo: UserDetailInfo? = null

    private fun onGetUserInfoListener() {
        mViewModel.myPageInfoObservable.observe(this) { it ->
            parseState(it, {
                userInfo = it
                AppCacheManager.isAdmin = it.isAdmin
                UserInfoDbManager.saveUserInfo(mContext, it)
                UserInfoDbManager.getUserInfo(mContext,
                    object : UserInfoDbManager.OnGetUserInfoListener {
                        override fun onUserInfo(userInfo: BaseUserInfo?) {
                            if (userInfo != null) {
                                showToast("获取成功")
                            } else {
                                showToast("获取失败")
                            }
                        }
                    })
                mBinding.userinfo = it
                showMyPicData(it)
                bindBanner(it.banners)
            })
        }
    }

    private fun showMyPicData(it: UserDetailInfo) {
        photoList.clear()
        editPhotoAdapter.submitList(mutableListOf())
        val backgrounds = it.thumbnail

        setNoPhotoTips(backgrounds.size)

        for (i in backgrounds.size - 1 downTo 0) {
            val isVideo = VideoUtils.isVideo(backgrounds[i])
            val editPhotoInfo = EditPhotoInfo(backgrounds[i], isVideo = isVideo)
            photoList.add(0, editPhotoInfo)
            editPhotoAdapter.add(0, editPhotoInfo)
        }
        if (photoList.size < 9) {
            setAddIconItem()
        }
    }

    private fun setNoPhotoTips(size: Int) {
        if (size == 0) {
            mBinding.tvAddPhoto.visibility = View.VISIBLE
            mBinding.tvNoPhotoTips.visibility = View.VISIBLE
            mBinding.rvPhoto.visibility = View.INVISIBLE
        } else {
            mBinding.tvAddPhoto.visibility = View.INVISIBLE
            mBinding.tvNoPhotoTips.visibility = View.INVISIBLE
            mBinding.rvPhoto.visibility = View.VISIBLE
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
        mBinding.tvAddPhoto.setOnSingleClickListener {
            toSelectImage()
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


    private fun bindBanner(list: MutableList<BannerBean>) {
        mBinding.banner.addBannerLifecycleObserver(this)
        mBinding.banner.setAdapter(MyBannerImageAdapter(mBinding.banner, list))
        mBinding.banner.indicator = CircleIndicator(context)
        mBinding.banner.setOnBannerListener(object : OnBannerListener<BannerBean> {
            override fun OnBannerClick(data: BannerBean, position: Int) {
                PageIntentUtil.url2Page(ActivityUtils.getTopActivity(), data.pageUrl)
            }
        })
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
                setNoPhotoTips(photoList.size)
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
                    }
                }

                override fun onUploadFail(msg: String) {
                    showToast(msg)
                    photoList.removeIf {
                        it.url == editPhotoInfo.url
                    }
                    editPhotoAdapter.removeAt(editPhotoAdapter.getPositionByItem(editPhotoInfo.url))
                    if (photoList.size == MAX_PIC_NUM-1) {
                        setAddIconItem()
                    }
                    setNoPhotoTips(photoList.size)
                }
            })
    }

    private fun setAddIconItem() {
        editPhotoAdapter.add(EditPhotoInfo(""))
    }

    companion object {
        const val MAX_PIC_NUM = 9
    }
}