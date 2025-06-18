package cn.yanhu.agora.ui.beautifyFace.agora

import android.view.View
import android.view.View.OnClickListener
import androidx.core.view.isVisible
import cn.happy.beautyface.ui.widget.BaseControllerView
import cn.yanhu.agora.ui.beautifyFace.BeautyViewModel
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.ActivityAgoraFaceBeautySetBinding
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.manager.RtcEngineInit
import cn.yanhu.agora.manager.dbCache.AgoraSdkCacheManager.hasLoadAgoraSdk
import cn.yanhu.agora.ui.beautifyFace.agora.BeautyConfigManager.OnLoadDefaultBeautyListener
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.view.TitleBar.TitleButtonOnClickListener
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.router.RouterPath
import cn.yanhu.commonres.utils.PermissionXUtils
import com.alibaba.android.arouter.facade.annotation.Route
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlin.jvm.java

/**
 * @author: zhengjun
 * created: 2025/6/17
 * desc:声网美颜设置页面
 */
@Route(path = RouterPath.ROUTER_BEAUTIFUL_FACE)
class AgoraFaceBeautySetActivity : BaseActivity<ActivityAgoraFaceBeautySetBinding, BeautyViewModel>(
    R.layout.activity_agora_face_beauty_set,
    BeautyViewModel::class.java
) {
    private var source: String? = ""
    override fun initData() {
        setFullScreenStatusBar(true)
        setTitleMarginTop(mBinding.titleBar)
        if (!hasLoadAgoraSdk()) {
            finish()
            LiveEventBus.get<Any?>(EventBusKeyConfig.SHOW_AGORA_SDK_DOWNLOAD_PROGRESS).post(true)
            return
        }
        mBinding.beautySetSf.setOnSingleClickListener {
            if (mBinding.controllerContainer.isVisible) {
                hidePanel()
            } else {
                showPanel()
            }
        }
        isRequestPermission()
    }

    override fun initListener() {
        super.initListener()
        mBinding.beautySetSf.setOnSingleClickListener {
            if (mBinding.controllerContainer.isVisible) {
                hidePanel()
            } else {
                showPanel()
            }
        }
        mBinding.titleBar.setTitleButtonOnClickListener(object : TitleButtonOnClickListener {
            override fun leftButtonOnClick(v: View?) {
                showExitSureDialog()
            }

            override fun rightButtonOnClick(v: View?) {
            }
        })
        mBinding.beautySetSave.setOnSingleClickListener {
            saveBeautyConfig()
        }
    }

    private fun showExitSureDialog() {

        DialogUtils.showConfirmDialog("是否保存当前修改？", {
            saveBeautyConfig()
        }, {
            finish()
        }, confirm = "保存", cancel = "不保存")
    }

    private fun saveBeautyConfig() {
        AgoraBeautySDK.saveBeautyConfig(source)
        showToast("保存成功")
        finish()
    }

    override fun back() {
        showExitSureDialog()
    }

    //判断是否授权必要权限
    private fun isRequestPermission() {
        PermissionXUtils.checkBeautyPermission(
            mContext, object : PermissionXUtils.PermissionListener {
                override fun onSuccess() {
                    AgoraManager.getInstance().init(
                        mContext, 1, mBinding.beautySetSf, false
                    )
                    AgoraManager.getInstance().setVideoEncoderConfiguration(720, 1280)
                    setupControllerView(mBinding.controllerContainer)
                }

                override fun onFail() {
                    finish()
                }
            })
    }

    private fun setupControllerView(controllerView: BaseControllerView) {
//        val virtualBgBinding =
//            ShowWidgetBeautyMultiDialogVirtualBgBinding.inflate(LayoutInflater.from(mContext))
        //controllerView.viewBinding.topCustomView.addView(virtualBgBinding.root)
//        virtualBgBinding.mSwitchMaterial.isChecked =
//            AgoraBeautySDK.virtualBackgroundSegmentation.modelType == SegmentationProperty.SEG_MODEL_GREEN
        // Beauty switch
        controllerView.beautyOpenIsActivated = false
        controllerView.beautyOpenClickListener =
            OnClickListener {
                AgoraBeautySDK.enable(!AgoraBeautySDK.isEnable)
                it.isActivated = AgoraBeautySDK.isEnable
            }
        controllerView.beautyDefaultClickListener =
            OnClickListener {
                DialogUtils.showLoading()
                BeautyConfigManager.getNetBeautyConfig(object : OnLoadDefaultBeautyListener {
                    override fun onLoadResult(isSuccess: Boolean) {
                        DialogUtils.dismissLoading()
                        AgoraBeautySDK.setDefaultConfig(mContext)
                        //virtualBgBinding.mSwitchMaterial.isChecked = false
                        controllerView.viewBinding.ivCompare.isVisible = false
                       // resetVirtualDefault(controllerView, virtualBgBinding)
                        controllerView.resetPageList()
                    }
                })
            }
        // Virtual background configuration
//        controllerView.pageList = ArrayList(controllerView.pageList).apply {
//            add(
//                BaseControllerView.PageInfo(
//                    cn.happy.beautyface.R.string.show_beauty_group_virtual_bg,
//                    listOf(
//                        BaseControllerView.ItemInfo(
//                            cn.happy.beautyface.R.string.show_beauty_item_none,
//                            cn.happy.beautyface.R.mipmap.show_beauty_ic_none,
//                            isSelected = AgoraBeautySDK.virtualBackgroundSource.backgroundSourceType == VirtualBackgroundSource.BACKGROUND_COLOR,
//                            onValueChanged = { _ ->
//                                AgoraBeautySDK.virtualBackgroundSource.backgroundSourceType =
//                                    VirtualBackgroundSource.BACKGROUND_COLOR
//                                AgoraBeautySDK.virtualBackgroundSegmentation.modelType =
//                                    SegmentationProperty.SEG_MODEL_AI
//                                AgoraBeautySDK.virtualBackgroundSegmentation.greenCapacity = 0.5f
//                                controllerView.updateItemInfo {
//                                    if (it.name == cn.happy.beautyface.R.string.show_beauty_item_virtual_bg_mitao
//                                        || it.name == cn.happy.beautyface.R.string.show_beauty_item_virtual_bg_blur
//                                    ) {
//                                        it.value = 0.5f
//                                    }
//                                    false
//                                }
//                                source = ""
//                                virtualBgBinding.mSwitchMaterial.isChecked = false
//                                RtcEngineInit.mRtcEngine?.enableVirtualBackground(
//                                    false,
//                                    AgoraBeautySDK.virtualBackgroundSource,
//                                    AgoraBeautySDK.virtualBackgroundSegmentation
//                                )
//                            }
//                        ),
//                        BaseControllerView.ItemInfo(
//                            cn.happy.beautyface.R.string.show_beauty_item_virtual_bg_blur,
//                            cn.happy.beautyface.R.mipmap.show_beauty_ic_virtual_bg_blur,
//                            value = AgoraBeautySDK.virtualBackgroundSegmentation.greenCapacity,
//                            isSelected = AgoraBeautySDK.virtualBackgroundSource.backgroundSourceType == VirtualBackgroundSource.BACKGROUND_BLUR,
//                            onValueChanged = { value ->
//                                AgoraBeautySDK.virtualBackgroundSource.backgroundSourceType =
//                                    VirtualBackgroundSource.BACKGROUND_BLUR
//                                AgoraBeautySDK.virtualBackgroundSegmentation.greenCapacity =
//                                    value
//                                source = ""
//                                AgoraBeautySDK.rtcEngine?.enableVirtualBackground(
//                                    true,
//                                    AgoraBeautySDK.virtualBackgroundSource,
//                                    AgoraBeautySDK.virtualBackgroundSegmentation
//                                )
//                            }
//                        ),
//                        BaseControllerView.ItemInfo(
//                            cn.happy.beautyface.R.string.show_beauty_item_virtual_bg_mitao,
//                            cn.happy.beautyface.R.mipmap.show_beauty_ic_virtual_bg_mitao,
//                            value = AgoraBeautySDK.virtualBackgroundSegmentation.greenCapacity,
//                            isSelected = AgoraBeautySDK.virtualBackgroundSource.backgroundSourceType == VirtualBackgroundSource.BACKGROUND_IMG,
//                            onValueChanged = { value ->
//                                AgoraBeautySDK.virtualBackgroundSource.backgroundSourceType =
//                                    VirtualBackgroundSource.BACKGROUND_IMG
//                                AgoraBeautySDK.virtualBackgroundSource.source =
//                                    FileUtils.copyFileFromAssets(
//                                        mContext,
//                                        "virtualbackgroud_mitao.jpg",
//                                        mContext.externalCacheDir!!.absolutePath
//                                    )
//                                source = "virtualbackgroud_mitao.jpg"
//                                AgoraBeautySDK.virtualBackgroundSegmentation.greenCapacity =
//                                    value
//                                AgoraBeautySDK.rtcEngine?.enableVirtualBackground(
//                                    true,
//                                    AgoraBeautySDK.virtualBackgroundSource,
//                                    AgoraBeautySDK.virtualBackgroundSegmentation
//                                )
//                            }
//                        )
//                    )
//                )
//            )
//        }

//        virtualBgBinding.mSwitchMaterial.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                controllerView.viewBinding.slider.visibility = View.VISIBLE
//                changeVirtualBGMode(SegmentationProperty.SEG_MODEL_GREEN)
//                return@setOnCheckedChangeListener
//            } else {
//                controllerView.viewBinding.slider.visibility = View.INVISIBLE
//                changeVirtualBGMode(SegmentationProperty.SEG_MODEL_AI)
//            }
//        }

//        controllerView.onSelectedChangeListener = { pageIndex, itemIndex ->
//            val pageInfo = controllerView.pageList[pageIndex]
//            val itemInfo = pageInfo.itemList[itemIndex]
//            if (pageInfo.name == cn.happy.beautyface.R.string.show_beauty_group_virtual_bg) {
//                controllerView.viewBinding.ivCompare.isVisible = false
//                if (itemInfo.name == cn.happy.beautyface.R.string.show_beauty_item_none) {
//                    controllerView.viewBinding.topCustomView.isVisible = false
//                    controllerView.viewBinding.slider.visibility = View.INVISIBLE
//                } else {
//                    controllerView.viewBinding.topCustomView.isVisible = true
//                    controllerView.viewBinding.slider.visibility =
//                        if (virtualBgBinding.mSwitchMaterial.isChecked) View.VISIBLE else View.INVISIBLE
//                }
//            } else {
//                controllerView.viewBinding.topCustomView.isVisible = false
//                controllerView.viewBinding.ivCompare.isVisible = true
//            }
//        }
    }

    private fun changeVirtualBGMode(modelType: Int) {
        AgoraBeautySDK.virtualBackgroundSegmentation.modelType = modelType
        RtcEngineInit.mRtcEngine?.enableVirtualBackground(
            true,
            AgoraBeautySDK.virtualBackgroundSource,
            AgoraBeautySDK.virtualBackgroundSegmentation
        )
    }


    fun hidePanel() {
        mBinding.controllerContainer.animate()
            ?.translationY(mBinding.controllerContainer.height.toFloat())?.setDuration(250)
            ?.withEndAction {
                mBinding.controllerContainer.visibility = View.GONE
            }?.start()
    }

    private fun showPanel() {
        mBinding.controllerContainer.translationY = 0f
        mBinding.controllerContainer.alpha = 0.4f
        mBinding.controllerContainer.visibility = View.VISIBLE
        mBinding.controllerContainer?.animate()?.translationY(0f)?.alpha(1f)?.setDuration(200)
            ?.start()
    }
}