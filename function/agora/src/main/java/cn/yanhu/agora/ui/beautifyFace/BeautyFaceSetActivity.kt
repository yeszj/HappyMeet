package cn.yanhu.agora.ui.beautifyFace

import android.Manifest
import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.View
import android.widget.SeekBar
import cn.yanhu.agora.R
import cn.yanhu.agora.adapter.BeautyFaceSetAdapter
import cn.yanhu.agora.bean.BeautyBean
import cn.yanhu.agora.databinding.ActivityBeautyFaceSetBinding
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.manager.BeautySetManager
import cn.yanhu.agora.manager.dbCache.AgoraSdkCacheManager
import cn.yanhu.agora.manager.dbCache.BeautyCacheManager
import cn.yanhu.agora.manager.dbCache.BeautyFaceParamCacheManager
import cn.yanhu.agora.ui.liveRoom.LiveRoomViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.router.RouterPath
import cn.yanhu.commonres.utils.PermissionXUtils
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ThreadUtils
import com.chad.library.adapter4.BaseQuickAdapter


/**
 * 美颜设置页面
 */
@Route(path = RouterPath.ROUTER_BEAUTIFUL_FACE)
class BeautyFaceSetActivity : BaseActivity<ActivityBeautyFaceSetBinding, LiveRoomViewModel>(
    R.layout.activity_beauty_face_set, LiveRoomViewModel::class.java
) {
    private val beautyFaceSetAdapter by lazy { BeautyFaceSetAdapter() }

    private var beautySkinList: MutableList<BeautyBean> = mutableListOf()
    private var beautyTypeList: MutableList<BeautyBean> = mutableListOf()
    private var beautyFilterList: MutableList<BeautyBean> = mutableListOf()

    private var selectFacePosition = -1
    private var selectBeautyTypePosition = -1
    private var selectFilterPosition = 0
    private var type: Int = 1
    override fun initData() {
        setFullScreenStatusBar()
        if (!AgoraSdkCacheManager.hasLoadAgoraSdk() || !BeautyCacheManager.hasLoadBeautySdk()) {
            showToast("请等待插件加载完成")
            finish()
            return
        }
        if (AgoraManager.isLiveRoom){
            showToast("正在通话中,无法进行美颜设置")
            finish()
            return
        }
        mBinding.alertkey = ""
        checkPermission()
        mBinding.rvBeauty.adapter = beautyFaceSetAdapter
        getSkinCareList()
        getSkinTypeList()
    }

    private fun getSkinCareList() {
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<Boolean>() {
            override fun doInBackground(): Boolean {
                beautySkinList = BeautySetManager.getInstance().skinCareList
                return true
            }

            override fun onSuccess(result: Boolean?) {
                beautyFaceSetAdapter.submitList(beautySkinList)
            }
        })
    }

    private fun getSkinTypeList() {
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<Boolean>() {
            override fun doInBackground(): Boolean {
                beautyTypeList = BeautySetManager.getInstance().skinTypeList
                return true
            }

            override fun onSuccess(result: Boolean?) {
            }
        })
    }

    private fun getSkinFilterList() {
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<Boolean>() {
            override fun doInBackground(): Boolean {
                beautyFilterList = BeautySetManager.getInstance().originSkinFilterList
                return true
            }

            override fun onSuccess(result: Boolean?) {
                if (!TextUtils.isEmpty(AppCacheManager.selectBeautyFilter)) {
                    val beautyBean =
                        GsonUtils.fromJson(
                            AppCacheManager.selectBeautyFilter,
                            BeautyBean::class.java
                        )
                    selectFilterPosition = beautyFilterList.indexOfFirst {
                        if (it.filterName == beautyBean.filterName) {
                            it.value = beautyBean.value
                        }
                        it.filterName == beautyBean.filterName
                    }
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initListener() {
        super.initListener()
        addAdapterItemListener()
        addOnSeekBarListener()
        mBinding.beautySetRefresh.setOnSingleClickListener {
            showRefreshDialog()
        }
        mBinding.beautySetSkin.setOnSingleClickListener {
            //美颜
            switchToBeautyFace()
        }
        mBinding.beautySetMking.setOnSingleClickListener {
            //美型
            switchToBeautyType()
        }
        mBinding.beautySetFilter.setOnSingleClickListener {
            //滤镜
            switchToBeautyFilter()
        }
        initTitleListener()
    }

    private fun initTitleListener() {
        mBinding.titleBar.setTitleButtonOnClickListener(object :
            TitleBar.TitleButtonOnClickListener {
            override fun leftButtonOnClick(v: View?) {
                showExitDialog()
            }

            override fun rightButtonOnClick(v: View?) {
                //保存
                beautyTypeList.forEach {
                    BeautyFaceParamCacheManager.saveParams(it)
                }
                beautySkinList.forEach {
                    BeautyFaceParamCacheManager.saveParams(it)
                }
                AppCacheManager.selectBeautyFilter =
                    GsonUtils.toJson(beautyFilterList[selectFilterPosition])
                showToast("保存成功")
                finish()
            }
        })
    }

    private fun showExitDialog() {
        DialogUtils.showConfirmDialog("确定退出美颜设置吗？", {
            finish()
        }, cancel = "取消")
    }

    private fun switchToBeautyType() {
        type = 2
        mBinding.beautySetMking.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.femaleColor))
        mBinding.beautySetSkin.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.white))
        mBinding.beautySetFilter.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.white))

        beautyFaceSetAdapter.submitList(beautyTypeList)
        mBinding.alertkey = ""
        beautyFaceSetAdapter.setSelectPosition(selectBeautyTypePosition)
        if (selectBeautyTypePosition != -1) {
            val beautyBean = beautyTypeList[selectBeautyTypePosition]
            mBinding.rvBeauty.scrollToPosition(selectBeautyTypePosition)
            setSeekProgress(beautyBean)
        }
    }

    private fun switchToBeautyFilter() {
        type = 3
        mBinding.beautySetFilter.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.femaleColor))
        mBinding.beautySetSkin.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.white))
        mBinding.beautySetMking.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.white))

        beautyFaceSetAdapter.submitList(beautyFilterList)
        mBinding.alertkey = ""
        if (selectFilterPosition != -1) {
            beautyFaceSetAdapter.setSelectPosition(selectFilterPosition)
            mBinding.rvBeauty.scrollToPosition(selectFilterPosition)
            val beautyBean = beautyFilterList[selectFilterPosition]
            setSeekProgress(beautyBean)
        }
    }

    private fun switchToBeautyFace() {
        type = 1
        mBinding.beautySetFilter.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.white))
        mBinding.beautySetSkin.setTextColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.femaleColor))
        mBinding.beautySetMking.setTextColor(CommonUtils.getColor(cn.zj.netrequest.R.color.white))
        beautyFaceSetAdapter.submitList(beautySkinList)
        mBinding.alertkey = ""
        if (selectFacePosition != -1) {
            beautyFaceSetAdapter.setSelectPosition(selectFacePosition)
            mBinding.rvBeauty.scrollToPosition(selectFacePosition)
            val selectItem = beautySkinList[selectFacePosition]
            setSeekProgress(selectItem)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showRefreshDialog() = DialogUtils.showConfirmDialog("是否恢复所有参数？", {
        beautySkinList = BeautySetManager.getInstance().originSkinCareList
        for (beautyBean in beautySkinList) {
            if (mBinding.alertkey.equals(beautyBean.key)) {
                mBinding.tvProgressValue.text = beautyBean.value.toString()
                mBinding.seekBar.progress = beautyBean.value
            }
            BeautySetManager.getInstance().setBeautyProperty(beautyBean.key, beautyBean.value)
        }
        beautyTypeList = BeautySetManager.getInstance().originSkinTypList
        for (beautyBean in beautyTypeList) {
            if (mBinding.alertkey.equals(beautyBean.key)) {
                mBinding.tvProgressValue.text = beautyBean.value.toString()
                mBinding.seekBar.progress = beautyBean.value
            }
            BeautySetManager.getInstance().setBeautyProperty(beautyBean.key, beautyBean.value)
        }
        setDefaultFilter()

        when (type) {
            1 -> {
                beautyFaceSetAdapter.submitList(beautySkinList)
            }

            2 -> {
                beautyFaceSetAdapter.submitList(beautyTypeList)
            }

            else -> {
                selectFilterPosition = 2
                switchToBeautyFilter()
            }
        }
    }, cancel = "取消")

    private fun setDefaultFilter() {
        val beautyBean = beautyFilterList[2]
        AppCacheManager.selectBeautyFilter = GsonUtils.toJson(beautyBean)
        BeautySetManager.getInstance().setBeautyFilter(beautyBean.value, beautyBean.filterName)
    }

    private fun addOnSeekBarListener() {
        mBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mBinding.tvProgressValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                selectItem?.apply {
                    if ("filter_level" == this.key) {
                        BeautySetManager.getInstance()
                            .setBeautyFilter(seekBar.progress, this.filterName)
                    } else {
                        BeautySetManager.getInstance().setBeautyProperty(this.key, seekBar.progress)
                    }
                    this.value = seekBar.progress
                }

            }

        })
    }

    private var selectItem: BeautyBean? = null
    private fun addAdapterItemListener() {
        beautyFaceSetAdapter.setOnItemClickListener(object :
            BaseQuickAdapter.OnItemClickListener<BeautyBean> {
            override fun onClick(
                adapter: BaseQuickAdapter<BeautyBean, *>, view: View, position: Int
            ) {
                when (type) {
                    1 -> {
                        selectFacePosition = position
                    }

                    2 -> {
                        selectBeautyTypePosition = position
                    }

                    else -> {
                        selectFilterPosition = position
                    }
                }
                selectItem = beautyFaceSetAdapter.getItem(position) ?: return
                beautyFaceSetAdapter.setSelectPosition(position)
                setSeekProgress(selectItem!!)
                selectItem?.apply {
                    if ("filter_level" == this.key) {
                        BeautySetManager.getInstance().setBeautyFilter(this.value, this.filterName)
                    } else {
                        BeautySetManager.getInstance().setBeautyProperty(this.key, this.value)
                    }
                }
            }
        })
    }

    private fun setSeekProgress(item: BeautyBean) {
        selectItem = item
        if (item.filterName == "origin") {
            mBinding.alertkey = ""
        } else {
            mBinding.alertkey = item.key
        }
        mBinding.seekBar.progress = item.value
        mBinding.tvProgressValue.text = item.value.toString()
    }

    private fun checkPermission() {
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.CAMERA)
        permissions.add(Manifest.permission.RECORD_AUDIO)
        PermissionXUtils.checkPermission(mContext,
            permissions,
            "对爱交友想访问您的以下权限，用于美颜设置",
            "您拒绝授权权限，将无法体验部分功能",
            object : PermissionXUtils.PermissionListener {
                override fun onSuccess() {
                    AgoraManager.getInstence().init(mContext, 1, mBinding.beautySetSf)
                    AgoraManager.getInstence().setVideoEncoderConfiguration(360, 800)
                    getSkinFilterList()
                }

                override fun onFail() {
                }

            })
    }


    override fun back() {
        showExitDialog()
    }


    override fun exactDestroy() {
        super.exactDestroy()
        AgoraManager.getInstence().onDestory()
    }

}