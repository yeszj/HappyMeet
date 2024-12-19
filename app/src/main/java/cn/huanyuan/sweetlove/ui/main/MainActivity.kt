package cn.huanyuan.sweetlove.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import cn.huanyuan.sweetlove.BaseApplication
import cn.huanyuan.sweetlove.R
import cn.huanyuan.sweetlove.bean.AppVersionInfo
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
import cn.huanyuan.sweetlove.ui.teenage.TeenAgeModeActivity
import cn.yanhu.agora.listener.OnDownloadProgressListener
import cn.yanhu.agora.manager.AgoraSdkDownloadManager
import cn.yanhu.agora.manager.BeautySDKManager
import cn.yanhu.agora.manager.RtcEngineInit
import cn.yanhu.agora.manager.dbCache.AgoraSdkCacheManager
import cn.yanhu.agora.manager.dbCache.BeautyCacheManager
import cn.yanhu.baselib.base.BaseActivity
import cn.yanhu.baselib.base.BaseTabAdapter
import cn.yanhu.baselib.utils.CommonUtils
import cn.yanhu.baselib.utils.GlideUtils
import cn.yanhu.baselib.utils.ext.logcom
import cn.yanhu.commonres.bean.response.GiftResponse
import cn.yanhu.commonres.bean.response.RoseRechargeResponse
import cn.yanhu.commonres.config.EventBusKeyConfig
import cn.yanhu.commonres.config.IntentKeyConfig
import cn.yanhu.commonres.loading.MainLoadingCallBack
import cn.yanhu.commonres.manager.AppCacheManager
import cn.yanhu.commonres.router.RouterPath
import cn.yanhu.commonres.task.AppPopTypeManager
import cn.yanhu.imchat.api.imChatRxApi
import cn.yanhu.imchat.custom.message.push.PushManager
import cn.zj.netrequest.ext.OnRequestResultListener
import cn.zj.netrequest.ext.parseState
import cn.zj.netrequest.ext.request
import cn.zj.netrequest.status.BaseBean
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ThreadUtils
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.chaychan.library.BottomBarItem
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.SimpleCallback
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
    R.layout.activity_main,
    MainViewModel::class.java
) {
    private var appVersionInfo: AppVersionInfo? =null
    private var isOpen:Boolean = false
    private var titleList = mutableListOf<String>()
    private var mFragmentList = mutableListOf<Fragment>()
    private var tabList: MutableList<TabEntity> = mutableListOf()
    override fun initData() {
        setFullScreenStatusBar(true)
        setStatusBarStyle(false)
        BeautySDKManager.sharedInstance().downloadBundle()
        downloadAgoraSdk()
        getRechargeInfo()
        getGiftInfo()
        mViewModel.getMainTabInfo()
        initRtcEngine()
        checkInit()
    }

    private fun checkInit() {
        mContext.lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val one = async {
                        rxApi.checkVersion()
                    }
                    val two = async { rxApi.checkIsOpenJuvenileMode() }
                    val await = one.await()
                    val await1 = two.await()
                    appVersionInfo = await.data
                    isOpen = await1.data == true
                }
            }.onFailure {
            }.onSuccess {
                if (appVersionInfo == null) {
                    if (isOpen){
                        //跳转到青少年模式页面
                        TeenAgeModeActivity.lunch(mContext,true)
                    }else{
                        if (!AppCacheManager.hasShowTeenApp){
                            BaseApplication.addPopTask(AppPopTypeManager.TYPE_TEE_POP,"")
                            AppCacheManager.hasShowTeenApp = true
                        }
                    }
                }else{
                    showVersionPop(appVersionInfo!!)
                }
            }


        }
    }

    private fun initRtcEngine() {
        if (AgoraSdkCacheManager.hasLoadAgoraSdk() && BeautyCacheManager.hasLoadBeautySdk()) {
            ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<Boolean>() {
                override fun doInBackground(): Boolean {
                    RtcEngineInit.initRtcEngine(mContext)
                    return true
                }

                override fun onSuccess(result: Boolean?) {
                }
            })
        }
    }

    override fun initListener() {
        super.initListener()
        LiveEventBus.get<Boolean>(EventBusKeyConfig.SHOW_AGORA_SDK_DOWNLOAD_PROGRESS)
            .observe(this) {
                showDownloadSdkProgressPop(
                    sdkDownloadProgress
                )
            }
        LiveEventBus.get<Int>(EventBusKeyConfig.UNREAD_COUNT).observe(this) {
            val tabMsgPosition = getTabMsgPosition()
            val bottomItem = mBinding.tabLayout.getBottomItem(tabMsgPosition)
            bottomItem.setUnreadNum(it)
        }

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

            override fun onDownLoadFail() {
                downLoadFail = true
                if (downloadProgressPop != null) {
                    downloadProgressPop?.dismiss()
                }
            }
        })
    }

    private var downLoadFail = false

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


    private var appVersionUpdatePop: AppVersionUpdatePop? = null
    private fun showVersionPop(it: AppVersionInfo) {
        if (CommonUtils.isPopShow(appVersionUpdatePop)) {
            return
        }
        appVersionUpdatePop = AppVersionUpdatePop.showDialog(mContext, it)
    }

    private fun getRechargeInfo() {
        request({ rxApi.getRechargeInfo() },
            object : OnRequestResultListener<RoseRechargeResponse> {
                override fun onSuccess(data: BaseBean<RoseRechargeResponse>) {
                    val rechargeResponse = data.data
                    AppCacheManager.rechargeInfo = GsonUtils.toJson(rechargeResponse)
                }
            })
    }

    private fun getGiftInfo() {
        request({ imChatRxApi.getGiftList() }, object : OnRequestResultListener<GiftResponse> {
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
                if (mFragmentList.size <= 0) {
                    initFrg()
                }
                showContent()
            })
        }
    }

    private fun initFrg() {
        val fragments = supportFragmentManager.fragments
        if (fragments.size>0){
            mFragmentList = fragments
        }
        tabList.forEach {
            if (fragments.size<=0){
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
            }
            mBinding.tabLayout.addItem(createBottomBarItem(it))
        }
        bindTabToVp()
    }

    private fun createBottomBarItem(tabEntity: TabEntity): BottomBarItem {
        val bottomBarItem = BottomBarItem.Builder(this).titleTextSize(12).titleSelectedColor(
            cn.yanhu.baselib.R.color.colorMain
        ).titleNormalColor(cn.yanhu.baselib.R.color.fontTextColor)
            .unreadNumThreshold(99)
            .unreadTextColor(R.color.white)
            .iconHeight(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20))
            .iconWidth(CommonUtils.getDimension(com.zj.dimens.R.dimen.dp_20))
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
                    resource: Drawable,
                    transition: Transition<in Drawable>?
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
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    bottomBarItem.setSelectedIcon(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
        return bottomBarItem
    }


    private fun bindTabToVp() {
        val tabAdapter = BaseTabAdapter(supportFragmentManager, titleList, mFragmentList)
        mBinding.viewPager.adapter = tabAdapter
        mBinding.tabLayout.setViewPager(mBinding.viewPager)
        mBinding.viewPager.offscreenPageLimit = mFragmentList.size
        mBinding.tabLayout.setOnItemSelectedListener { _, _, _ ->
            KeyboardUtils.hideSoftInput(
                mContext
            )
        }
//        mBinding.viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
//            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
//                if (position == getTabSameCityPosition()) {
//                    LiveDataEventManager.sendLiveDataMessage(
//                        LiveDataEventManager.SWITCH_TO_SAME_CITY,
//                        position
//                    )
//                }
//            }
//        })
        // mBinding.viewPager.currentItem = 2
    }

    private fun getTabSameCityPosition(): Int {
        for (i in 0..<mFragmentList.size) {
            val fragment = mFragmentList[i]
            if (fragment is TabSameCityFrg) {
                return i
            }
        }
        return -1
    }

    private fun getTabMsgPosition(): Int {
        for (i in 0..<mFragmentList.size) {
            val fragment = mFragmentList[i]
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

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        (application as BaseApplication).reInitImSdk()
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