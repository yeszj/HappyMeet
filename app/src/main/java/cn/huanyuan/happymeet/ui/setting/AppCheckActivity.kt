package cn.huanyuan.happymeet.ui.setting

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.view.View
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.bean.AppCheckItemInfo
import cn.huanyuan.happymeet.databinding.ActivityAppCheckBinding
import cn.huanyuan.happymeet.ui.setting.adapter.AppCheckAdapter
import cn.huanyuan.happymeet.ui.setting.adapter.AppCheckHeadAdapter
import cn.huanyuan.happymeet.ui.system.ErrorLogPostActivity
import cn.huanyuan.happymeet.ui.userinfo.auth.RealNameActivity
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.view.TitleBar
import cn.yanhu.commonres.manager.RequestCodeManager
import cn.yanhu.commonres.utils.PermissionXUtils
import cn.zj.netrequest.ext.parseState
import com.blankj.utilcode.util.AppUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.pcl.sdklib.listener.OnAuthResultListener
import com.pcl.sdklib.sdk.wechat.WxAuthUtils
import kotlin.math.roundToInt

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class AppCheckActivity : BaseActivity<ActivityAppCheckBinding, SettingViewModel>(
    R.layout.activity_app_check,
    SettingViewModel::class.java
) {
    private val appCheckHeadAdapter by lazy { AppCheckHeadAdapter() }
    private val appCheckAdapter by lazy { AppCheckAdapter() }
    private lateinit var helper: QuickAdapterHelper
    override fun initData() {
        setFullScreenStatusBar()
        helper = QuickAdapterHelper.Builder(appCheckAdapter)
            .build()
        mBinding.recyclerView.adapter = helper.adapter
        helper.addBeforeAdapter(appCheckHeadAdapter)
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel.getAppCheckInfo(
            PermissionXUtils.hasPermission(mutableListOf(Manifest.permission.CAMERA) as ArrayList<String>),
            PermissionXUtils.hasPermission(mutableListOf(Manifest.permission.RECORD_AUDIO) as ArrayList<String>)
        )
    }


    override fun registerNecessaryObserver() {
        super.registerNecessaryObserver()
        mViewModel.checkInfoObservable.observe(this) { it ->
            parseState(it, {
                val score = getScore(it)
                setRiskBg(score)
                appCheckHeadAdapter.item = score
                appCheckAdapter.submitList(it)
            })
        }
        WxAuthUtils.registerAuthResultListener(mContext,object : OnAuthResultListener{
            override fun onAuthSuccess() {
                val item = appCheckAdapter.getItem(currentPosition)
                item?.hasCheck = true
                item?.btnValue = "已绑定"
                updateScore()
            }
        })
    }

    private var currentPosition = 0
    override fun initListener() {
        super.initListener()
        addItemCliclListener()
        initTitleListener()
    }

    private fun initTitleListener() {
        mBinding.titleBar.setTitleButtonOnClickListener(object :
            TitleBar.TitleButtonOnClickListener {
            override fun leftButtonOnClick(v: View?) {
                finish()
            }

            override fun rightButtonOnClick(v: View?) {
                ErrorLogPostActivity.lunch(mContext)
            }

        })
    }

    private fun addItemCliclListener() {
        appCheckAdapter.addOnItemChildClickListener(R.id.tv_status,
            object : BaseQuickAdapter.OnItemChildClickListener<AppCheckItemInfo> {
                override fun onItemClick(
                    adapter: BaseQuickAdapter<AppCheckItemInfo, *>,
                    view: View,
                    position: Int
                ) {
                    currentPosition = position
                    val item = adapter.getItem(position) ?: return
                    if (!item.hasCheck) {
                        when (item.id) {
                            AppCheckItemInfo.TYPE_VERSION -> {

                            }

                            AppCheckItemInfo.TYPE_CAMERA -> {
                                checkCameraPermission(item)
                            }

                            AppCheckItemInfo.TYPE_MICROPHONE -> {
                                checkMicroPermission(item)
                            }

                            AppCheckItemInfo.TYPE_PHONE -> {

                            }

                            AppCheckItemInfo.TYPE_REAL_NAME -> {
                                RealNameActivity.lunch(mContext)
                            }

                            AppCheckItemInfo.TYPE_WX_BIND -> {
                                WxAuthUtils.weChatAuth(mContext)
                            }
                        }
                    }
                }

            })
    }

    private fun checkMicroPermission(
        item: AppCheckItemInfo,
    ) {
        PermissionXUtils.checkPermission(
            mContext,
            mutableListOf(Manifest.permission.RECORD_AUDIO) as ArrayList<String>,
            "${AppUtils.getAppName()}想访问您的以下权限，用于上麦说话等功能",
            "您拒绝授权权限，将无法体验部分功能", object : PermissionXUtils.PermissionListener {
                override fun onSuccess() {
                    item.hasCheck = true
                    item.btnValue = "已授权"
                    updateScore()
                }

                override fun onFail() {
                }
            }
        )
    }

    private fun checkCameraPermission(
        item: AppCheckItemInfo
    ) {
        PermissionXUtils.checkPermission(
            mContext,
            mutableListOf(Manifest.permission.CAMERA) as ArrayList<String>,
            "${AppUtils.getAppName()}想访问您的以下权限，用于视频上麦等功能",
            "您拒绝授权权限，将无法体验部分功能", object : PermissionXUtils.PermissionListener {
                override fun onSuccess() {
                    item.hasCheck = true
                    item.btnValue = "已授权"
                    updateScore()
                }

                override fun onFail() {
                }
            }
        )
    }

    private fun updateScore() {
        appCheckAdapter.notifyItemChanged(currentPosition)
        val score = getScore(appCheckAdapter.items.toMutableList())
        appCheckHeadAdapter.item = score
        setRiskBg(score)
    }


    private fun setRiskBg(score: Int) {
        if (score < 50) {
            mBinding.riskBg.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_shape_app_check_high_risk)
        } else if (score == 100) {
            mBinding.riskBg.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_shape_app_check_no_risk)
        } else {
            mBinding.riskBg.setBackgroundResource(cn.yanhu.commonres.R.drawable.bg_shape_app_check_low_risk)
        }
    }

    private fun getScore(it: MutableList<AppCheckItemInfo>): Int {
        var hasCheckNum = 0
        it.forEach {
            if (it.hasCheck) {
                hasCheckNum++
            }
        }
        return (hasCheckNum * 100f / it.size).roundToInt()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == RequestCodeManager.REQUEST_CODE_REAL_NAME){
                val item = appCheckAdapter.getItem(currentPosition)
                item?.hasCheck = true
                item?.btnValue = "已认证"
                updateScore()
            }
        }
    }
}