package cn.huanyuan.sweetlove.ui.setting

import android.annotation.SuppressLint
import android.view.View
import androidx.core.content.ContextCompat
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.AppVersionInfo
import cn.huanyuan.sweetlove.databinding.ActivityAppSettingBinding
import cn.huanyuan.sweetlove.func.dialog.AppVersionUpdatePop
import cn.yanhu.baselib.utils.CacheSizeManager
import cn.huanyuan.sweetlove.ui.setting.adapter.AppSettingAdapter
import cn.huanyuan.sweetlove.ui.setting.adapter.AppSettingFootAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DialogUtils
import cn.yanhu.commonres.bean.SettingItemInfo
import cn.yanhu.commonres.manager.WebUrlManager
import cn.yanhu.commonres.router.PageIntentUtil
import cn.yanhu.imchat.manager.ImChatManager
import cn.zj.netrequest.application.ApplicationProxy
import cn.zj.netrequest.ext.parseState
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.util.setOnDebouncedItemClick

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
        helper = QuickAdapterHelper.Builder(appSettingAdapter)
            .build()
        mBinding.rvMenu.adapter = helper.adapter
        appSettingAdapter.submitList(getSettingList())
        helper.addAfterAdapter(appSettingFootAdapter)
        appSettingFootAdapter.item = "退出登录"
        appSettingFootAdapter.setOnItemClickListener { _, _, _ ->
            DialogUtils.showConfirmDialog("确认退出登录吗？", {
                ApplicationProxy.instance.loginInvalid()
            }, cancel = "取消")

        }
        addItemClickListener()
        checkVersion()
    }

    private var appVersionInfo: AppVersionInfo? = null
    private fun checkVersion() {
        mViewModel.checkVersion()
        mViewModel.checkVersionObservable.observe(this) { it ->
            parseState(it, { it ->
                if (it == null) {
                    return@parseState
                }
                appVersionInfo = it
                val indexOfFirst = appSettingAdapter.items.indexOfFirst {
                    it.name == "检查更新"
                }
                val item = appSettingAdapter.getItem(indexOfFirst) ?: return@parseState
                item.desc = "发现新版本"
                appSettingAdapter.notifyItemChanged(indexOfFirst)
            })
        }
    }

    private fun addItemClickListener() {
        appSettingAdapter.setOnDebouncedItemClick(1000, object :
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

                    "blackList" -> {
                        ImChatManager.toBlackListPage(mContext)
                    }

                    "checkVersion" -> {
                        appVersionInfo?.apply {
                            showVersionPop(this)
                        }
                    }

                    else -> {
                        PageIntentUtil.url2Page(mContext, item.pageUrl)
                    }
                }

            }

        })
    }

    private var appVersionUpdatePop: AppVersionUpdatePop? = null
    private fun showVersionPop(it: AppVersionInfo) {
        if (CommonUtils.isPopShow(appVersionUpdatePop)) {
            return
        }
        appVersionUpdatePop = AppVersionUpdatePop.showDialog(mContext, it)
    }

    private fun getSettingList(): MutableList<SettingItemInfo> {
        val list = mutableListOf<SettingItemInfo>()
        list.add(
            SettingItemInfo(
                ContextCompat.getDrawable(
                    mContext,
                    cn.yanhu.commonres.R.drawable.white_top_corner_10
                ),
                name = "价格设置", pageUrl = PageIntentUtil.PAGE_PRICE_SET
            )
        )
        list.add(
            SettingItemInfo(
                name = "美颜设置",
                pageUrl = PageIntentUtil.PAGE_BEAUTY_SET
            )
        )
        PageIntentUtil
        list.add(
            SettingItemInfo(
                name = "黑名单",
                pageUrl = PageIntentUtil.PAGE_BLACK_LIST
            )
        )

        list.add(
            SettingItemInfo(
                name = "安全指南", showDivider = false, pageUrl = WebUrlManager.SECURITY_AGREEMENT
            )
        )

        list.add(
            SettingItemInfo(
                name = "安全中心", showDivider = false, pageUrl = PageIntentUtil.PAGE_SECURITY_CENTER
            )
        )
        list.add(
            SettingItemInfo(
                ContextCompat.getDrawable(
                    mContext,
                    cn.yanhu.commonres.R.drawable.white_bottom_corner_10
                ),
                name = "青少年模式", showDivider = true, pageUrl = PageIntentUtil.PAGE_TEENAGE_MODE
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
                name = "检查更新", desc = "当前已是最新版本", pageUrl = "checkVersion"
            )
        )
        list.add(
            SettingItemInfo(
                name = "清理缓存",
                desc = CacheSizeManager.getCacheSize(mContext),
                pageUrl = "clearCache"
            )
        )
        list.add(
            SettingItemInfo(
                ContextCompat.getDrawable(
                    mContext,
                    cn.yanhu.commonres.R.drawable.white_bottom_corner_10
                ),
                name = "关于我们", showDivider = true, pageUrl = PageIntentUtil.PAGE_ABOUT_US
            )
        )
        return list
    }
}