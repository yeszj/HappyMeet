package cn.yanhu.agora.ui.beautifyFace.sensetime

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import androidx.core.view.isVisible
import cn.happy.beautyface.databinding.ShowWidgetBeautyMultiDialogVirtualBgBinding
import cn.happy.beautyface.ui.utils.BeautyConfigManager
import cn.happy.beautyface.ui.utils.BeautyConfigManager.OnLoadDefaultBeautyListener
import cn.happy.beautyface.ui.utils.BeautyManager
import cn.happy.beautyface.ui.widget.BaseControllerView
import cn.yanhu.agora.R
import cn.yanhu.agora.databinding.ActivityAgoraFaceBeautySetBinding
import cn.yanhu.agora.manager.AgoraManager
import cn.yanhu.agora.manager.dbCache.AgoraSdkCacheManager.hasLoadAgoraSdk
import cn.yanhu.agora.manager.dbCache.BeautyCacheManager
import cn.yanhu.agora.ui.beautifyFace.BeautyViewModel
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.baselib.utils.ext.showToast
import cn.yanhu.baselib.view.TitleBar.TitleButtonOnClickListener
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.router.RouterPath
import cn.yanhu.commonres.utils.PermissionXUtils
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ThreadUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlin.jvm.java

/**
 * @author: zhengjun
 * created: 2025/6/17
 * desc:商汤美颜
 */
@Route(path = RouterPath.ROUTER_BEAUTIFUL_FACE)
class SenseTimeBeautySetActivity :
    BaseActivity<ActivityAgoraFaceBeautySetBinding, BeautyViewModel>(
        R.layout.activity_agora_face_beauty_set,
        BeautyViewModel::class.java
    ) {
    override fun initData() {
        setFullScreenStatusBar(true)
        setTitleMarginTop(mBinding.titleBar)
        if (!hasLoadAgoraSdk()) {
            finish()
            LiveEventBus.get<Any?>(EventBusKeyConfig.SHOW_AGORA_SDK_DOWNLOAD_PROGRESS).post(true)
            return
        } else if (!BeautyCacheManager.hasLoadBeautySdk()) {
            finish()
            LiveEventBus.get<Any?>(EventBusKeyConfig.SHOW_BEAUTY_SDK_DOWNLOAD_PROGRESS).post(true)
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
        BeautyManager.saveBeautyConfig()
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
                        mContext, 1, mBinding.beautySetSf
                    )
                    AgoraManager.getInstance().setVideoEncoderConfiguration(720, 1280)
                    setupControllerView(mBinding.controllerContainer)
                    ThreadUtils.getMainHandler().postDelayed({
                        mBinding.beautySetSf.visibility = View.VISIBLE
                    }, 1500)
                }

                override fun onFail() {
                    finish()
                }
            })
    }

    private fun setupControllerView(controllerView: BaseControllerView) {
        val virtualBgBinding =
            ShowWidgetBeautyMultiDialogVirtualBgBinding.inflate(LayoutInflater.from(mContext))
        controllerView.viewBinding.topCustomView.addView(virtualBgBinding.root)
        controllerView.beautyOpenIsActivated = false
        controllerView.beautyOpenClickListener = OnClickListener {
            BeautyManager.enable = !BeautyManager.enable
            it.isActivated = BeautyManager.enable
        }
        controllerView.beautyDefaultClickListener = OnClickListener {
            DialogUtils.showLoading()
            BeautyConfigManager.getNetBeautyConfig(object : OnLoadDefaultBeautyListener {
                override fun onLoadResult(isSuccess: Boolean) {
                    DialogUtils.dismissLoading()
                    BeautyManager.setDefaultConfig(mContext)
                    virtualBgBinding.mSwitchMaterial.isChecked = false
                    controllerView.viewBinding.ivCompare.isVisible = false
                    controllerView.resetPageList()
                }
            })
        }
        controllerView.viewBinding.topCustomView.isVisible = false
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
        mBinding.controllerContainer.animate()?.translationY(0f)?.alpha(1f)?.setDuration(200)
            ?.start()
    }

    override fun exactDestroy() {
        super.exactDestroy()
        BeautyManager.destroy()
    }
}