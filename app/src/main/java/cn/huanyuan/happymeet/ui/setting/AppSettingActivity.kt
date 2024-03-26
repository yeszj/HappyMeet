package cn.huanyuan.happymeet.ui.setting

import android.annotation.SuppressLint
import android.view.View
import androidx.core.content.ContextCompat
import cn.huanyuan.happymeet.BuildConfig
import cn.huanyuan.happymeet.R
import cn.huanyuan.happymeet.databinding.ActivityAppSettingBinding
import cn.yanhu.baselib.utils.CacheSizeManager
import cn.huanyuan.happymeet.ui.setting.adapter.AppSettingAdapter
import cn.huanyuan.happymeet.ui.setting.adapter.AppSettingFootAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.commonres.bean.SettingItemInfo
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.router.PageIntentUtil
import cn.zj.netrequest.application.ApplicationProxy
import com.blankj.utilcode.util.AppUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper

/**
 * @author: zhengjun
 * created: 2024/3/12
 * desc:
 */
class AppSettingActivity : BaseActivity<ActivityAppSettingBinding, SettingViewModel>(
    R.layout.activity_app_setting,
    SettingViewModel::class.java
) {
    private lateinit var helper: QuickAdapterHelper
    private val appSettingAdapter by lazy { AppSettingAdapter() }
    private val appSettingFootAdapter by lazy { AppSettingFootAdapter() }
    @SuppressLint("SetTextI18n")
    override fun initData() {
        setFullScreenStatusBar()
        setStatusBarStyle(false)
        mBinding.tvAppInfo.text =
            "${CommonUtils.getString(R.string.app_name)}v${AppUtils.getAppVersionName()}-${AppUtils.getAppVersionCode()}-${BuildConfig.FLAVOR}"
        helper = QuickAdapterHelper.Builder(appSettingAdapter)
            .build()
        mBinding.rvMenu.adapter = helper.adapter
        appSettingAdapter.submitList(getSettingList())
        helper.addAfterAdapter(appSettingFootAdapter)
        appSettingFootAdapter.item = "退出登录"
        appSettingFootAdapter.setOnItemClickListener { _, _, _ ->
            DialogUtils.showConfirmDialog("确认退出登录吗？",{
                ApplicationProxy.instance.loginInvalid()
            })

        }
        addItemClickListener()
    }

    private fun addItemClickListener() {
        appSettingAdapter.setOnItemClickListener(object :
            BaseQuickAdapter.OnItemClickListener<SettingItemInfo> {
            override fun onClick(
                adapter: BaseQuickAdapter<SettingItemInfo, *>,
                view: View,
                position: Int
            ) {
                val item = adapter.getItem(position) ?: return
                when (item.pageUrl) {
                    "clearCache" -> {
                        CacheSizeManager.clearAllCache(mContext,
                            object : CacheSizeManager.OnClearCacheListener {
                                override fun onClearSuccess() {
                                    item.desc = CacheSizeManager.getCacheSize(mContext)
                                    appSettingAdapter.notifyItemChanged(position, true)
                                }
                            })

                    }

                    else -> {
                        PageIntentUtil.url2Page(mContext, item.pageUrl)
                    }
                }

            }

        })
    }

    private fun getSettingList(): MutableList<SettingItemInfo> {
        val list = mutableListOf<SettingItemInfo>()
        list.add(
            SettingItemInfo(
                ContextCompat.getDrawable(
                    mContext,
                    cn.yanhu.commonres.R.drawable.white_top_corner_10
                ),
                name = "隐私开关设置"
            )
        )
        list.add(
            SettingItemInfo(
                name = "美颜设置"
            )
        )
        list.add(
            SettingItemInfo(
                name = "黑名单"
            )
        )
        list.add(
            SettingItemInfo(
                ContextCompat.getDrawable(
                    mContext,
                    cn.yanhu.commonres.R.drawable.white_bottom_corner_10
                ),
                name = "安全中心", showDivider = true
            )
        )

        list.add(
            SettingItemInfo(
                ContextCompat.getDrawable(
                    mContext,
                    cn.yanhu.commonres.R.drawable.white_top_corner_10
                ),
                name = "用户协议", pageUrl = WebUrlManager.USER_AGREEMENT
            )
        )
        list.add(
            SettingItemInfo(
                name = "隐私协议", pageUrl = WebUrlManager.PRIVACY_AGREEMENT
            )
        )
        list.add(
            SettingItemInfo(
                name = "检查更新", desc = "当前已是最新版本"
            )
        )
        list.add(
            SettingItemInfo(
                name = "清理缓存", desc = CacheSizeManager.getCacheSize(mContext), pageUrl = "clearCache"
            )
        )
        list.add(
            SettingItemInfo(
                ContextCompat.getDrawable(
                    mContext,
                    cn.yanhu.commonres.R.drawable.white_bottom_corner_10
                ),
                name = "关于我们", showDivider = true
            )
        )
        return list
    }
}