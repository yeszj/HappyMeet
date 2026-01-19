package cn.huanyuan.sweetlove.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import cn.happy.beautyface.ui.utils.BeautyConfigManager
import cn.huanyuan.sweetlove.BaseApplication
import cn.huanyuan.sweetlove.BuildConfig
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.AppStartResponse
import cn.huanyuan.sweetlove.bean.AppVersionInfo
import cn.huanyuan.sweetlove.bean.LiveFloatInfo
import cn.huanyuan.sweetlove.bean.TabEntity
import cn.huanyuan.sweetlove.databinding.ActivityMainBinding
import cn.huanyuan.sweetlove.func.dialog.AppVersionUpdatePop
import cn.huanyuan.sweetlove.func.dialog.DownloadProgressPop
import cn.huanyuan.sweetlove.net.rxApi
import cn.huanyuan.sweetlove.ui.main.tab_blinddate.TabBlindDateFrg
import cn.huanyuan.sweetlove.ui.main.tab_msg.TabMessageFrg
import cn.huanyuan.sweetlove.ui.main.tab_my.TabMineFrg
import cn.huanyuan.sweetlove.ui.main.tab_samecity.TabSameCityFrg
import cn.huanyuan.sweetlove.ui.main.tab_wallet.TabWalletFrg
import cn.huanyuan.sweetlove.ui.recommend.RecommendRoomActivity
import cn.huanyuan.sweetlove.ui.teenage.TeenAgeModeActivity
import cn.yanhu.agora.listener.OnDownloadProgressListener
import cn.yanhu.agora.manager.AgoraSdkDownloadManager
import cn.yanhu.agora.manager.BeautyFaceEffectManager
import cn.yanhu.agora.manager.BeautySDKManager
import cn.yanhu.baselib.adapter.MyFragmentStateAdapter
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.DateUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.baselib.utils.ext.setOnSingleClickListener
import cn.yanhu.commonres.adapter.CircleBannerImageAdapter
import cn.yanhu.commonres.api.commonRxApi
import cn.yanhu.commonres.bean.AppPopResponse
import cn.yanhu.commonres.bean.response.GiftResponse
import cn.yanhu.commonres.bean.response.RoseRechargeResponse
import cn.yanhu.commonres.config.ChatConstant
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.loading.MainLoadingCallBack
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.manager.AppManager
import cn.yanhu.commonres.manager.ServiceConfigKeyManager
import cn.yanhu.commonres.router.RouterPath
import cn.yanhu.commonres.task.AppPopTypeManager
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.custom.message.push.PushManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import cn.zj.netrequest.status.ErrorCode
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.chaychan.library.BottomBarItem
import com.chaychan.library.UIUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback
import com.permissionx.guolindev.PermissionX
import com.youth.banner.listener.OnBannerListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * @author: zhengjun
 * created: 2024/1/30
 * desc:
 */
@Route(path = RouterPath.ROUTER_MAIN)
class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(
    R.layout.activity_main, MainViewModel::class.java
) {
    private var appVersionInfo: AppVersionInfo? = null
    private var isOpen: Boolean = false
    private var mFragmentList = mutableListOf<Fragment>()
    private var tabList: MutableList<TabEntity> = mutableListOf()
    private var selectItem = 2
    override fun initData() {

        setFullScreenStatusBar(true)
        setStatusBarStyle(false)
        AppManager.setAppState(
            AppManager.STATE_FOREGROUND, PermissionX.areNotificationsEnabled(mContext)
        )
        downloadBeautySdk()
        downloadAgoraSdk()
        getRechargeInfo()
        getGiftInfo()
        mViewModel.getMainTabInfo()
        BaseApplication.clearTask()
        if (!BuildConfig.DEBUG){
            checkOaId()
        }
        getSkinMaskConfig()
        checkInit()
        appStart()
        BeautyConfigManager.getNetBeautyConfig()
        clearOldLogFile()
    }

    private fun getSkinMaskConfig(){
        request({ commonRxApi.getConfigInfo(ServiceConfigKeyManager.OPEN_WHITEN_SKIN_MASK) },object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {
                AppCacheManager.openWhitenSkinMask = data.data == "1"
            }
        })
    }

    private fun clearOldLogFile() {
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<Boolean>() {
            override fun onSuccess(result: Boolean?) {
            }

            override fun doInBackground(): Boolean? {
                val files = LogUtils.getLogFiles()
                files.forEach {
                    val currentLogFilePath = LogUtils.getCurrentLogFilePath()
                    val todayStr = DateUtils.getYestodyStr(0, "yyyy_MM_dd")
                    val yesTodayStr = DateUtils.getYestodyStr(-1, "yyyy_MM_dd")
                    logcom("logTime","todayStr=$todayStr,yesTodayStr=$yesTodayStr")
                    val yestodayLogFilePath = currentLogFilePath.replace(todayStr, yesTodayStr)
                    if (it.absolutePath != LogUtils.getCurrentLogFilePath() && it.absolutePath != yestodayLogFilePath) {
                        FileUtils.delete(it)
                    }
                }
                return true
            }
        })
    }

    private fun getRecommendLiveFloat() {
        mViewModel.getRecommendLiveFloating(object : OnRequestResultListener<LiveFloatInfo> {
            @SuppressLint("SetTextI18n")
            override fun onSuccess(data: BaseBean<LiveFloatInfo>) {
                val response = data.data
                if (response == null) {
                    mBinding.vgLiveFloat.visibility = View.GONE
                } else {
                    val portraitList = response.portraitList
                    if (portraitList.isNotEmpty()) {
                        mBinding.vgLiveFloat.visibility = View.VISIBLE
                        mBinding.tvFloatLiveCount.text = "${response.count}人正在直播"
                        bindBanner(portraitList)
                    } else {
                        mBinding.vgLiveFloat.visibility = View.GONE
                    }
                }

            }

        })
    }

    private var bannerAdapter: CircleBannerImageAdapter? = null
    private fun bindBanner(list: MutableList<String>) {
        if (bannerAdapter == null) {
            mBinding.liveBanner.addBannerLifecycleObserver(this)
            bannerAdapter = CircleBannerImageAdapter(mBinding.liveBanner, list)
            mBinding.liveBanner.setAdapter(bannerAdapter)
            mBinding.vgLiveFloat.setOnSingleClickListener {
                startActivity(Intent(mContext, RecommendRoomActivity::class.java))
            }
            //  mBinding.banner.indicator = CircleIndicator(context)
            mBinding.liveBanner.setOnBannerListener(object : OnBannerListener<String> {
                override fun OnBannerClick(data: String, position: Int) {
                    startActivity(Intent(mContext, RecommendRoomActivity::class.java))
                }
            })
        } else {
            bannerAdapter?.setDatas(list)
        }
    }

    private fun checkOaId() {
        request({ rxApi.checkOaid() }, object : OnRequestResultListener<String?> {
            override fun onSuccess(data: BaseBean<String?>) {
            }

            override fun onFail(code: Int?, msg: String?) {
                if (code == ErrorCode.CHANGE_DEVICE && !TextUtils.isEmpty(msg)) {
                    logcom("checkOaid", "addPopTask")
                    BaseApplication.addPopTask(
                        ChatConstant.ACTION_CHANGE_DEVICE, msg!!
                    )
                }
            }
        }, isShowToast = false)
    }

    override fun getSavedInstanceState(savedInstanceState: Bundle?) {
        super.getSavedInstanceState(savedInstanceState)
        savedInstanceState?.apply {
            selectItem = this.getInt(IntentKeyConfig.POSITION, 2)
        }
    }

    private fun appStart() {
        request({ rxApi.appStart() }, object : OnRequestResultListener<AppStartResponse> {
            override fun onSuccess(data: BaseBean<AppStartResponse>) {
                val appStartResponse = data.data ?: return
                AppCacheManager.agoraAppId = appStartResponse.agoraAppId
            }

        }, false, activity = mContext)
    }

    private var checkAuthInfo: BaseBean<Int>? = null
    private var appPopInfo: AppPopResponse? = null
    private fun checkInit() {
        mContext.lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val one = async {
                        rxApi.checkVersion()
                    }
                    val two = async { rxApi.checkIsOpenJuvenileMode() }
                    val three = async { rxApi.checkAuthTip() }
                    val four = async { rxApi.getToastInfo() }
                    val await = one.await()
                    val await1 = two.await()
                    checkAuthInfo = three.await()
                    appPopInfo = four.await().data
                    appVersionInfo = await.data
                    isOpen = await1.data == true
                }
            }.onFailure {
            }.onSuccess {
                if (appVersionInfo == null) {
                    if (isOpen) {
                        //跳转到青少年模式页面
                        TeenAgeModeActivity.lunch(mContext, true)
                    } else {
                        if (checkAuthInfo != null && checkAuthInfo?.code == 200 && checkAuthInfo?.data != 0) {
                            BaseApplication.addPopTask(
                                ChatConstant.ACTION_FORCE_AUTH, checkAuthInfo!!.data.toString()
                            )
                        }
                        if (!AppCacheManager.hasShowTeenApp) {
                            BaseApplication.addPopTask(AppPopTypeManager.TYPE_TEE_POP, "")
                            AppCacheManager.hasShowTeenApp = true
                        }
                        if (appPopInfo != null && appPopInfo!!.common != null) {
                            BaseApplication.addPopTask(
                                ChatConstant.ACTION_EVENT_POP,
                                GsonUtils.toJson(appPopInfo!!.common)
                            )
                        }
                        getPop()
                    }
                } else {
                    showVersionPop(appVersionInfo!!)
                }
            }


        }
    }

    private fun getPop(){
        request({rxApi.getPop()},object : OnRequestResultListener<String>{
            override fun onSuccess(data: BaseBean<String>) {
            }
        })
    }

    override fun initListener() {
        super.initListener()
        LiveEventBus.get<Boolean>(EventBusKeyConfig.SHOW_AGORA_SDK_DOWNLOAD_PROGRESS)
            .observe(this) {
                showDownloadSdkProgressPop(
                    sdkDownloadProgress
                )
            }
        LiveEventBus.get<Boolean>(EventBusKeyConfig.SHOW_BEAUTY_SDK_DOWNLOAD_PROGRESS)
            .observe(this) {
                showDownloadBeautySdkProgressPop(beautySdkDownloadProgress)
            }
        LiveEventBus.get<Int>(EventBusKeyConfig.UNREAD_COUNT).observe(this) {
            val tabMsgPosition = getTabMsgPosition()
            val bottomItem = mBinding.tabLayout.getBottomItem(tabMsgPosition)
            bottomItem.setUnreadNum(it)
        }
    }

    private var beautySdkDownloadProgress = 0
    private fun downloadBeautySdk() {
        downloadFaceEffect()
        BeautySDKManager.sharedInstance().downloadBundle(object : OnDownloadProgressListener {
            override fun onProgress(progress: Int) {
                if (progress >= beautySdkDownloadProgress) {
                    beautySdkDownloadProgress = progress
                    logcom("downloadBeautySdk", "progress=\$progress")
                    if (CommonUtils.isPopShow(beautyDownloadProgressPop)) {
                        beautyDownloadProgressPop?.setProgress(beautySdkDownloadProgress)
                    }
                }
            }

            override fun onDownLoadFail() {
                beautySdkDownloadProgress = 0
                beautyDownLoadFail = true
                beautyDownloadProgressPop?.dismiss()
            }

        })
    }

    private fun downloadFaceEffect() {
        BeautyFaceEffectManager.sharedInstance()
            .downloadBundle(object : OnDownloadProgressListener {
                override fun onProgress(progress: Int) {
                }

                override fun onDownLoadFail() {
                }
            })
    }


    private var sdkDownloadProgress = 0
    private fun downloadAgoraSdk() {
        AgoraSdkDownloadManager.downloadAgoraSdkInfo(object : OnDownloadProgressListener {
            override fun onProgress(progress: Int) {
                if (progress >= sdkDownloadProgress) {
                    sdkDownloadProgress = progress
                    logcom("downloadAgoraSdk", "progress=\$progress")
                    if (CommonUtils.isPopShow(downloadProgressPop)) {
                        downloadProgressPop?.setProgress(sdkDownloadProgress)
                    }
                }
            }

            override fun onInitRtc() {
            }

            override fun onDownLoadFail() {
                sdkDownloadProgress = 0
                downLoadFail = true
                if (downloadProgressPop != null) {
                    downloadProgressPop?.dismiss()
                }
            }
        })
    }

    private var downLoadFail = false
    private var beautyDownLoadFail = false

    private var downloadProgressPop: DownloadProgressPop? = null

    private fun showDownloadSdkProgressPop(progress: Int) {
        if (!CommonUtils.isPopShow(downloadProgressPop)) {
            if (downLoadFail) {
                downloadAgoraSdk()
            }
            downloadProgressPop = DownloadProgressPop.showDialog(
                ActivityUtils.getTopActivity(),
                progress,
                object : SimpleCallback() {
                    override fun onDismiss(popupView: BasePopupView) {
                        downloadProgressPop = null
                    }
                })
        } else {
            downloadProgressPop?.setProgress(progress)
        }
    }


    private var beautyDownloadProgressPop: DownloadProgressPop? = null

    private fun showDownloadBeautySdkProgressPop(progress: Int) {
        if (!CommonUtils.isPopShow(beautyDownloadProgressPop)) {
            if (beautyDownLoadFail) {
                downloadBeautySdk()
            }
            beautyDownloadProgressPop =
                DownloadProgressPop.showDialog(
                    ActivityUtils.getTopActivity(),
                    progress,
                    object : SimpleCallback() {
                        override fun onDismiss(popupView: BasePopupView) {
                            beautyDownloadProgressPop = null
                        }
                    })
        } else {
            beautyDownloadProgressPop?.setProgress(progress)
        }
    }

    private var appVersionUpdatePop: AppVersionUpdatePop? = null
    private fun showVersionPop(it: AppVersionInfo) {
        if (CommonUtils.isPopShow(appVersionUpdatePop)) {
            return
        }
        appVersionUpdatePop = AppVersionUpdatePop.showDialog(mContext, it)
    }

    private fun getRechargeInfo() {
        request(
            { rxApi.getRechargeInfo() },
            object : OnRequestResultListener<RoseRechargeResponse> {
                override fun onSuccess(data: BaseBean<RoseRechargeResponse>) {
                    val rechargeResponse = data.data
                    AppCacheManager.rechargeInfo = GsonUtils.toJson(rechargeResponse)
                }
            })
    }

    private fun getGiftInfo() {
        request({ imChatRxApi.getGiftList(1) }, object : OnRequestResultListener<GiftResponse> {
            override fun onSuccess(data: BaseBean<GiftResponse>) {
                val giftInfo = data.data
                AppCacheManager.giftInfo = GsonUtils.toJson(giftInfo)
            }
        })
    }

    override fun initLoadService() {
        val loadSir = initCustomLoadingLoad(MainLoadingCallBack())
        loadService = loadSir.register(mBinding.root) {
            onReload()
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        clickOfflinePush(intent)
        val position = intent.getStringExtra(IntentKeyConfig.POSITION)
        position?.apply {
            mBinding.viewPager.currentItem = this.toInt()
        }
    }

    private fun clickOfflinePush(intent: Intent?) {
        val extras = intent?.extras
        if (extras != null) {
            val sendUserId = extras.getString("f")
            val groupId = extras.getString("g")
            PushManager.clickOfflinePush(sendUserId, groupId)
        }
    }

    override fun registerNecessaryObserver() {
        onGetTabSuccess()
    }


    private fun onGetTabSuccess() {
        mViewModel.tabInfoObservable.observe(this) { it ->
            parseState(it, {
                tabList = it
                if (mFragmentList.isEmpty()) {
                    initFrg()
                }
                showContent()
            })
        }
    }

    private fun initFrg() {
        val fragments = supportFragmentManager.fragments
        tabList.forEach {
            when (it.id) {
                1 -> {
                    mFragmentList.add(TabSameCityFrg())
                }

                2 -> {
                    mFragmentList.add(TabMessageFrg())
                }

                3 -> {
                    mFragmentList.add(TabBlindDateFrg())
                }

                4 -> {
                    mFragmentList.add(TabWalletFrg())
                }

                5 -> {
                    mFragmentList.add(TabMineFrg())
                }
            }
            mBinding.tabLayout.addItem(createBottomBarItem(it))
        }
        bindTabToVp()
        if (fragments.isNotEmpty()) {
            if (selectItem > 0) {
                mBinding.tabLayout.currentItem = selectItem - 1
            } else {
                mBinding.tabLayout.currentItem = 1
            }
        }
        mBinding.tabLayout.currentItem = selectItem
    }

    private fun clearAllFrgManager() {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        val fragments = supportFragmentManager.fragments
        if (fragments.size > 0) {
            for (fragment in fragments) {
                ft.remove(fragment)
            }
            ft.commitNow()
            mFragmentList.clear()
            mBinding.tabLayout.removeAllViews()
        }
    }

    private fun createBottomBarItem(tabEntity: TabEntity): BottomBarItem {
        val bottomBarItem = BottomBarItem.Builder(this).titleTextSize(UIUtils.sp2px(mContext, 12f))
            .iconHeight(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_32))
            .iconWidth(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_32)).titleSelectedColor(
                CommonUtils.getColor(cn.yanhu.baselib.R.color.colorMain)
            ).titleNormalColor(CommonUtils.getColor(cn.yanhu.baselib.R.color.color6))
            //还有很多属性，详情请查看Builder里面的方法
            .create(
                cn.yanhu.commonres.R.drawable.tab_default_bg,
                cn.yanhu.commonres.R.drawable.tab_default_bg,
                tabEntity.name
            )
        GlideUtils.loadAsDrawable(
            mContext,
            tabEntity.normalIcon,
            object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable, transition: Transition<in Drawable>?
                ) {
                    bottomBarItem.setNormalIcon(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
        GlideUtils.loadAsDrawable(
            mContext,
            tabEntity.selectIcon,
            object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable, transition: Transition<in Drawable>?
                ) {
                    bottomBarItem.setSelectedIcon(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
        return bottomBarItem
    }


    private fun bindTabToVp() {
        val tabAdapter = MyFragmentStateAdapter(mContext, mFragmentList)
        mBinding.viewPager.offscreenPageLimit = mFragmentList.size
        mBinding.viewPager.adapter = tabAdapter
        mBinding.tabLayout.setViewPager2(mBinding.viewPager)
        mBinding.tabLayout.setOnItemSelectedListener { _, _, _ ->
            KeyboardUtils.hideSoftInput(
                mContext
            )
        }

    }

    private fun getTabSameCityPosition(): Int {
        val fragments = mFragmentList
        for (i in 0..<fragments.size) {
            val fragment = fragments[i]
            if (fragment is TabSameCityFrg) {
                return i
            }
        }
        return -1
    }

    private fun getTabMsgPosition(): Int {
        val fragments = mFragmentList
        for (i in 0..<fragments.size) {
            val fragment = fragments[i]
            if (fragment is TabMessageFrg) {
                return i
            }
        }
        return -1
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            sendHomeKeyEvent()
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    private fun sendHomeKeyEvent() {
        moveTaskToBack(true)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(IntentKeyConfig.POSITION, mBinding.viewPager.currentItem)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        (application as BaseApplication).reInitImSdk()
    }

    override fun onResume() {
        super.onResume()
        getRecommendLiveFloat()
    }

    companion object {
        fun lunch(context: Activity, bundle: Bundle? = null) {
            val intent = Intent(context, MainActivity::class.java)
            if (bundle != null) {
                intent.putExtras(bundle)
            }
            context.startActivity(intent)
        }
    }
}